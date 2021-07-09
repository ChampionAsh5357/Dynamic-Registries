/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import net.ashwork.dynamicregistries.entry.ICodecEntry;
import net.ashwork.dynamicregistries.entry.IDynamicEntry;
import net.ashwork.dynamicregistries.network.DynamicRegistryPacket;
import net.ashwork.dynamicregistries.registry.DynamicRegistry;
import net.ashwork.dynamicregistries.registry.DynamicRegistryBuilder;
import net.ashwork.dynamicregistries.registry.IDynamicRegistry;
import net.ashwork.dynamicregistries.registry.IRegistrableDynamicRegistry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: Document and implement
public class DynamicRegistryManager {

    public static final DynamicRegistryManager STATIC = new DynamicRegistryManager("Static");
    public static final DynamicRegistryManager DYNAMIC = new DynamicRegistryManager("Dynamic");

    private static final Marker CREATE = MarkerManager.getMarker("Create Registry");
    private static final Marker RELOAD = MarkerManager.getMarker("Reload Registry");
    private final String stage;
    private final BiMap<ResourceLocation, DynamicRegistry<?, ?>> registries;
    private final BiMap<Class<? extends IDynamicEntry<?>>, ResourceLocation> superTypes;
    private final Set<ResourceLocation> synced, saved;
    private final Map<ResourceLocation, ResourceLocation> legacyNames;

    private DynamicRegistryManager(final String stage) {
        this.stage = stage;
        this.registries = HashBiMap.create();
        this.superTypes = HashBiMap.create();
        this.synced = new HashSet<>();
        this.saved = new HashSet<>();
        this.legacyNames = new HashMap<>();
    }

    public String getName() {
        return this.stage;
    }

    @SuppressWarnings("unchecked")
    public <V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> DynamicRegistry<V, C> getRegistry(final ResourceLocation name) {
        return (DynamicRegistry<V, C>) this.registries.get(name);
    }

    public <V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> DynamicRegistry<V, C> getRegistry(final Class<? super V> entryClass) {
        return this.getRegistry(this.superTypes.get(entryClass));
    }

    public <V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> ResourceLocation getRegistryName(final DynamicRegistry<V, C> registry) {
        return this.registries.inverse().get(registry);
    }

    public <T> void reload(final Map<ResourceLocation, T> entries, final DynamicOps<T> ops, final DynamicRegistryManager currentStage) {
        final Map<ResourceLocation, Map<ResourceLocation, T>> registryEntries = new HashMap<>();
        entries.forEach((id, encodedEntry) -> {
            String[] paths = id.getPath().split("/", 3);
            registryEntries.computeIfAbsent(new ResourceLocation(paths[0], paths[1]), u -> new HashMap<>()).put(new ResourceLocation(id.getNamespace(), paths[2]), encodedEntry);
        });
        DynamicRegistries.LOGGER.debug(RELOAD, "Found data for {} registries", registryEntries.size());
        currentStage.registries.keySet().forEach(name -> {
            DynamicRegistries.LOGGER.debug(IRegistrableDynamicRegistry.REGISTER, "Register data to {}", name);
            DynamicRegistry<?, ?> registry = this.promoteFromStage(name, currentStage);
            if (registry != null) {
                registry.setAndUnlockFromStage(currentStage);
                registry.registerAll(registryEntries.getOrDefault(registry.getName(), Collections.emptyMap()), ops);
                registry.lock();
            } else DynamicRegistries.LOGGER.error(IRegistrableDynamicRegistry.REGISTER, "Registry promotion for {} has returned null, skipping", name);
        });
    }

    @Nullable
    protected <V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> DynamicRegistry<V, C> promoteFromStage(final ResourceLocation name, DynamicRegistryManager stage) {
        if (!this.registries.containsKey(name)) {
            final DynamicRegistry<V, C> stagedRegistry = stage.getRegistry(name);
            if (stagedRegistry == null) return null;
            DynamicRegistries.LOGGER.debug(CREATE, "Promoting {} from {} to {}", name, stage.getName(), this.getName());
            this.registries.put(name, stagedRegistry.copy(this));
            this.superTypes.put(stagedRegistry.getEntrySuperType(), name);
            if (stage.synced.contains(name)) this.synced.add(name);
            if (stage.saved.contains(name)) this.saved.add(name);
            stage.legacyNames.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(name))
                    .forEach(entry -> this.addLegacyName(entry.getKey(), entry.getValue()));
        } else DynamicRegistries.LOGGER.debug(CREATE, "Promotion of {} from {} to {} already occurred, skipping", name, stage.getName(), this.getName());
        return this.getRegistry(name);
    }

    public <V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> IDynamicRegistry<V, C> createRegistry(final DynamicRegistryBuilder<V, C> builder) {
        final ResourceLocation name = builder.getName();
        final Set<Class<?>> parents = new HashSet<>();
        findSuperTypes(builder.getSuperType(), parents);
        final Sets.SetView<Class<?>> overlaps = Sets.intersection(parents, this.superTypes.keySet());
        if (!overlaps.isEmpty()) {
            Class<?> found = overlaps.iterator().next();
            DynamicRegistries.LOGGER.error(CREATE, "Found existing registry of type {} named {}, you cannot create a new registry ({}) with type {}, as {} has a parent of that type", found, this.superTypes.get(found), name, builder.getSuperType(), builder.getSuperType());
            throw new IllegalArgumentException("Found existing registry containing " + found + ": " + this.superTypes.get(found));
        }
        final DynamicRegistry<V, C> registry = new DynamicRegistry<>(builder, this);
        this.registries.put(name, registry);
        this.superTypes.put(builder.getSuperType(), name);
        if (builder.shouldSync()) this.synced.add(name);
        if (builder.shouldSave()) this.saved.add(name);
        builder.getLegacyNames().forEach(legacyName -> this.addLegacyName(legacyName, name));
        return this.getRegistry(name);
    }

     protected static void findSuperTypes(final Class<?> type, final Set<Class<?>> types) {
        if (type == null || type == Object.class) return;
        types.add(type);
        for (Class<?> i : type.getInterfaces()) findSuperTypes(i, types);
        findSuperTypes(type.getSuperclass(), types);
    }

    protected void addLegacyName(final ResourceLocation legacyName, final ResourceLocation currentName) {
        if (this.legacyNames.putIfAbsent(legacyName, currentName) != null)
            throw new IllegalArgumentException("Legacy name is already to the existing registry " + this.legacyNames.get(legacyName) + ": " + legacyName + " -> " + currentName);
    }

    protected Stream<Map.Entry<ResourceLocation, DynamicRegistry<?, ?>>> registries(final Lookup lookup) {
        return this.registries.entrySet().stream().filter(entry -> {
            switch (lookup) {
                case ALL:
                    return true;
                case SAVE:
                    return this.saved.contains(entry.getKey());
                case SYNC:
                    return this.synced.contains(entry.getKey());
                default:
                    return false;
            }
        });
    }

    public void sendToClient() {
        DynamicRegistries.instance().getChannel().send(PacketDistributor.ALL.noArg(),
                new DynamicRegistryPacket(this.getName(), this.registries(Lookup.SYNC)
                        .map(entry -> Pair.of(entry.getKey(), (CompoundNBT) entry.getValue().toSnapshot(NBTDynamicOps.INSTANCE)))
                        .filter(pair -> Objects.nonNull(pair.getSecond()))
                        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
                )
        );
    }

    public enum Lookup {
        ALL,
        SAVE,
        SYNC
    }
}
