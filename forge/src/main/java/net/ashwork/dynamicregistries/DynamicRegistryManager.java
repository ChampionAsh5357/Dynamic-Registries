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
import net.ashwork.dynamicregistries.registry.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A manager used to handle the stages of dynamic registries. There are two main stages:
 * {@link #STATIC} and {@link #DYNAMIC}. {@link #STATIC} is responsible for handling registries
 * and entries that are created within the codebase. The entries themselves can be overridden.
 * {@link #DYNAMIC} is responsible for promoting the static entries and registering any dynamic
 * ones to their registry.
 */
public class DynamicRegistryManager {

    /**
     * Responsible for handling registries and entries that are created within the codebase.
     */
    public static final DynamicRegistryManager STATIC = new DynamicRegistryManager("Static");
    /**
     * Responsible for promoting the static entries and registering any dynamic ones to their registry.
     */
    public static final DynamicRegistryManager DYNAMIC = new DynamicRegistryManager("Dynamic");

    /**
     * A marker that represents all logging information while creating a registry.
     */
    private static final Marker CREATE = MarkerManager.getMarker("Create Registry");
    /**
     * A marker that represents all logging information while reloading a registry.
     */
    private static final Marker RELOAD = MarkerManager.getMarker("Reload Registry");

    /**
     * The stage name of the manager.
     */
    private final String stage;
    /**
     * The registries within the manager.
     */
    private final BiMap<ResourceLocation, DynamicRegistry<?, ?>> registries;
    /**
     * The already existing super types of the manager.
     */
    private final BiMap<Class<? extends IDynamicEntry<?>>, ResourceLocation> superTypes;
    /**
     * A set of registry names that will be synced and saved respectively.
     */
    private final Set<ResourceLocation> synced, saved;
    /**
     * The prior names of the registries.
     */
    private final Map<ResourceLocation, ResourceLocation> legacyNames;

    /**
     * Constructs a staged manager.
     *
     * @param stage the stage name of the manager
     */
    private DynamicRegistryManager(final String stage) {
        this.stage = stage;
        this.registries = HashBiMap.create();
        this.superTypes = HashBiMap.create();
        this.synced = new HashSet<>();
        this.saved = new HashSet<>();
        this.legacyNames = new HashMap<>();
    }

    /**
     * Gets the stage name of the manager
     *
     * @return the stage name of the manager
     */
    public String getName() {
        return this.stage;
    }

    /**
     * Gets a {@link DynamicRegistry} from its name.
     *
     * @param name the name of the dynamic registry
     * @param <V> the super type of the dynamic registry entry
     * @param <C> the super type of the codec registry entry
     * @return the dynamic registry, or {@code null} if none exists
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> DynamicRegistry<V, C> getRegistry(final ResourceLocation name) {
        return (DynamicRegistry<V, C>) this.registries.get(name);
    }

    /**
     * Gets a {@link DynamicRegistry} from its entry super class.
     *
     * @param entryClass the entry super class of the dynamic registry
     * @param <V> the super type of the dynamic registry entry
     * @param <C> the super type of the codec registry entry
     * @return the dynamic registry, or {@code null} if none exists
     */
    @Nullable
    public <V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> DynamicRegistry<V, C> getRegistry(final Class<? super V> entryClass) {
        return this.getRegistry(this.superTypes.get(entryClass));
    }

    /**
     * Gets a the name of a registry from its registry instance.
     *
     * @param registry the dynamic registry instance
     * @param <V> the super type of the dynamic registry entry
     * @param <C> the super type of the codec registry entry
     * @return the name of the dynamic registry, or {@code null} if none exists
     */
    @Nullable
    public <V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> ResourceLocation getRegistryName(final DynamicRegistry<V, C> registry) {
        return this.registries.inverse().get(registry);
    }

    /**
     * Updates the legacy name of the registry to the current name.
     *
     * @param legacyName the old name of the registry
     * @return the new name of the registry or {@code legacyName} if none exists
     */
    public ResourceLocation updateLegacyName(ResourceLocation legacyName) {
        final ResourceLocation originalName = legacyName;
        while(this.getRegistry(legacyName) == null) {
            legacyName = this.legacyNames.get(legacyName);
            if (legacyName == null) return originalName;
        }
        return legacyName;
    }

    /**
     * Reloads all dynamic registries with the static data from the {@code currentStage}
     * and then registers the encoded data.
     *
     * @param entries a map of identifiers to encoded registry objects
     * @param ops the operator used to transmute the encoded object
     * @param currentStage the current stage of the registry the data is promoted from
     * @param <T> the type of the encoded object
     */
    public <T> void reload(final Map<ResourceLocation, T> entries, final DynamicOps<T> ops, final DynamicRegistryManager currentStage) {
        final Map<ResourceLocation, Map<ResourceLocation, T>> registryEntries = new HashMap<>();
        final Map<ResourceLocation, Set<T>> missingEntryStrategies = new HashMap<>();
        entries.forEach((id, encodedEntry) -> {
            String[] paths = id.getPath().split("/", 3);
            if (paths[0].equals("missing_mappings")) missingEntryStrategies.computeIfAbsent(this.updateLegacyName(new ResourceLocation(paths[1], paths[2])), u -> new HashSet<>()).add(encodedEntry);
            else registryEntries.computeIfAbsent(this.updateLegacyName(new ResourceLocation(paths[0], paths[1])), u -> new HashMap<>()).put(new ResourceLocation(id.getNamespace(), paths[2]), encodedEntry);
        });
        DynamicRegistries.LOGGER.debug(RELOAD, "Found data for {} registries", registryEntries.size());
        currentStage.registries.keySet().forEach(name -> {
            DynamicRegistries.LOGGER.debug(IRegistrableDynamicRegistry.REGISTER, "Register data to {}", name);
            DynamicRegistry<?, ?> registry = this.promoteFromStage(name, currentStage);
            if (registry != null) {
                Set<ResourceLocation> oldEntries = registry.setAndUnlockFromStage(currentStage);
                registry.registerAll(registryEntries.getOrDefault(registry.getName(), Collections.emptyMap()), ops);
                registry.postReloadedEntries(oldEntries, missingEntryStrategies.getOrDefault(name, Collections.emptySet()), ops);
                registry.lock();
            } else DynamicRegistries.LOGGER.error(IRegistrableDynamicRegistry.REGISTER, "Registry promotion for {} has returned null, skipping", name);
        });
    }

    /**
     * Promotes a registry from some existing {@code stage} to this one. Any
     * data in the existing registry stage is promoted via {@link IStageableDynamicRegistry#copy(DynamicRegistryManager)}.
     *
     * @param name the name of the dynamic registry to promote
     * @param stage the stage being promoted from
     * @param <V> the super type of the dynamic registry entry
     * @param <C> the super type of the codec registry entry
     * @return a new instance of the dynamic registry in this stage
     */
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

    /**
     * Creates a new dynamic registry for the current configuration.
     *
     * @param builder the builder instance containing the registry configurations
     * @param <V> the super type of the dynamic registry entry
     * @param <C> the super type of the codec registry entry
     * @return a new dynamic registry
     * @throws IllegalArgumentException if there already exists an existing super type or parent of a dynamic registry type
     */
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

    /**
     * Gets all the superclasses and superinterfaces of {@code type} and stores them within {@code types}.
     *
     * @param type the class to get all parents of
     * @param types the set holding all types of this class
     */
     protected static void findSuperTypes(final Class<?> type, final Set<Class<?>> types) {
        if (type == null || type == Object.class) return;
        types.add(type);
        for (Class<?> i : type.getInterfaces()) findSuperTypes(i, types);
        findSuperTypes(type.getSuperclass(), types);
    }

    /**
     * Adds a legacy name for a particular registry.
     *
     * @param legacyName the previous name of the registry
     * @param currentName the current name of the registry
     */
    protected void addLegacyName(final ResourceLocation legacyName, final ResourceLocation currentName) {
        if (this.legacyNames.putIfAbsent(legacyName, currentName) != null)
            throw new IllegalArgumentException("Legacy name is already to the existing registry " + this.legacyNames.get(legacyName) + ": " + legacyName + " -> " + currentName);
    }

    /**
     * Returns a stream of entries containing the wanted registries.
     *
     * @param lookup the registry filter
     * @return a stream of entries of name to dynamic registry
     */
    public Stream<Map.Entry<ResourceLocation, DynamicRegistry<?, ?>>> registries(final Lookup lookup) {
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

    /**
     * Sends the syncable registries to the client via {@link DynamicRegistryPacket}.
     */
    public void sendToClient() {
        DynamicRegistries.instance().getChannel().send(PacketDistributor.ALL.noArg(),
                new DynamicRegistryPacket(this.getName(), this.registries(Lookup.SYNC)
                        .map(entry -> Pair.of(entry.getKey(), (CompoundTag) entry.getValue().toSnapshot(NbtOps.INSTANCE)))
                        .filter(pair -> Objects.nonNull(pair.getSecond()))
                        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
                )
        );
    }

    /**
     * An identifier used to determine which registries to get from the manager.
     */
    public enum Lookup {
        /**
         * Gets all registries.
         */
        ALL,
        /**
         * Gets only savable registries.
         */
        SAVE,
        /**
         * Gets only syncable registries.
         */
        SYNC
    }
}
