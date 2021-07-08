/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.registry;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ashwork.dynamicregistries.DynamicRegistries;
import net.ashwork.dynamicregistries.DynamicRegistryManager;
import net.ashwork.dynamicregistries.entry.ICodecEntry;
import net.ashwork.dynamicregistries.entry.IDynamicEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * The base implementation of {@link IDynamicRegistry}. All registries will be
 * an implementation of this class.
 *
 * @param <V> the super type of the dynamic registry entry
 * @param <C> the super type of the codec registry entry
 */
//TODO: Document and implement
public class DynamicRegistry<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> implements ICodecRegistrableDynamicRegistry<V, C>, IModifiableDynamicRegistry<V, C>, ISnapshotDynamicRegistry<V, C> {

    private final ResourceLocation name;
    protected final DynamicRegistryBuilder<V, C> builder;
    private final Class<V> superType;
    private final IForgeRegistry<C> codecRegistry;
    @Nullable
    private final ResourceLocation defaultKey;
    private final Codec<V> registryEntryCodec, explodedEntryCodec;
    private final Codec<DynamicRegistry<V, C>> snapshotCodec;

    protected final BiMap<ResourceLocation, V> entries;
    protected final Map<ResourceLocation, ResourceLocation> aliases;

    @Nullable
    private V defaultValue;
    private boolean locked;

    public DynamicRegistry(DynamicRegistryBuilder<V, C> builder, DynamicRegistryManager stage) {
        this.name = builder.getName();
        this.builder = builder;
        this.superType = builder.getSuperType();
        this.codecRegistry = builder.getCodecRegistry();
        this.defaultKey = builder.getDefaultKey();
        this.registryEntryCodec = ResourceLocation.CODEC.comapFlatMap(id -> {
            @Nullable
            V val = this.getValue(id);
            return val != null ? DataResult.success(val)
                    : DataResult.error("Not a valid registry object within " + this.getName() + ": " + id);
        }, IDynamicEntry::getRegistryName);
        this.explodedEntryCodec = ResourceLocation.CODEC.comapFlatMap(id -> {
            @Nullable C val = this.codecRegistry.getValue(id);
            return val != null ? DataResult.success(val)
                    : DataResult.error("Not a valid registry object within " + this.codecRegistry.getRegistryName() + ": " + id);
        }, ICodecEntry::getRegistryName).dispatch(dyn -> (C) dyn.codec(), Function.identity());
        this.snapshotCodec = RecordCodecBuilder.create(instance ->
                instance.group(
                        RecordCodecBuilder.point(this),
                        Codec.unboundedMap(ResourceLocation.CODEC, this.explodedEntryCodec).fieldOf("entries").forGetter(reg -> reg.entries),
                        Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).fieldOf("aliases").forGetter(reg -> reg.aliases)
                ).apply(instance, (registry, entries, aliases) -> registry.fromSnapshot(entries, aliases))
        );
        this.entries = HashBiMap.create();
        this.aliases = new HashMap<>();
        this.locked = true;
    }

    public DynamicRegistry<V, C> copy(final DynamicRegistryManager stage) {
        return new DynamicRegistry<>(builder, stage);
    }

    public void setAndUnlockFromStage(final DynamicRegistryManager stage) {
        IDynamicRegistry<V, C> stagedRegistry = stage.getRegistry(this.getName());
        if (stagedRegistry == null)
            throw new IllegalArgumentException("The registry " + this.getName() + " does not exist within " + stage.getName());
        this.unlock();
        this.clear();
        stagedRegistry.forEach(this::register);
    }

    @Override
    public Codec<ISnapshotDynamicRegistry<V, C>> snapshotCodec() {
        return this.snapshotCodec.xmap(Function.identity(), DynamicRegistry.class::cast);
    }

    private DynamicRegistry<V, C> fromSnapshot(final Map<ResourceLocation, V> entries, final Map<ResourceLocation, ResourceLocation> aliases) {
        this.unlock();
        this.clear();
        entries.forEach((id, registryObject) -> {
            registryObject.setRegistryName(id);
            this.register(registryObject);
        });
        aliases.forEach(this.aliases::put);
        this.lock();
        return this;
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public Class<V> getEntrySuperType() {
        return this.superType;
    }

    @Override
    public Class<C> getCodecSuperType() {
        return this.codecRegistry.getRegistrySuperType();
    }

    @Override
    public boolean containsKey(ResourceLocation key){
        while (key != null) {
            if (this.entries.containsKey(key)) return true;
            key = this.aliases.get(key);
        }
        return false;
    }

    @Override
    public boolean containsValue(V value) {
        return this.entries.containsValue(value);
    }

    @Override
    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    @Nullable
    @Override
    public V getValue(ResourceLocation key) {
        V ret;
        do {
            ret = this.entries.get(key);
            key = this.aliases.get(key);
        } while (ret == null && key != null);
        return ret == null ? this.defaultValue : ret;
    }

    @Nullable
    @Override
    public ResourceLocation getKey(V value) {
        ResourceLocation ret = this.entries.inverse().get(value);
        return ret == null ? this.defaultKey : ret;
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.entries.keySet());
    }

    @Override
    public Set<V> values() {
        return Collections.unmodifiableSet(this.entries.values());
    }

    @Override
    public Set<Map.Entry<ResourceLocation, V>> entrySet() {
        return Collections.unmodifiableSet(this.entries.entrySet());
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    public void unlock() {
        this.locked = false;
    }

    public void lock() {
        this.locked = true;
    }

    @Nullable
    @Override
    public V remove(ResourceLocation key) {
        if (this.isLocked())
            throw this.constructLockedError("remove");

        DynamicRegistries.LOGGER.debug(MODIFY, "Registry object {} is being removed from {}", key, this.getName());
        return this.entries.remove(key);
    }

    @Override
    public void clear() {
        if (this.isLocked())
            throw this.constructLockedError("clear");

        DynamicRegistries.LOGGER.debug(MODIFY, "Registry {} is being cleared", this.getName());
        this.aliases.clear();
        this.entries.clear();
    }

    @Override
    public void register(V value) {
        if (this.isLocked())
            throw this.constructLockedError("register");

        Preconditions.checkNotNull(value, "Cannot add a null object to the registry.");
        ResourceLocation identifier = value.getRegistryName();
        Preconditions.checkNotNull(identifier, "Cannot use a null identifier for entry: " + value);

        V originalEntry = this.getValue(identifier);
        if (originalEntry == value) { // Already registered
            DynamicRegistries.LOGGER.warn(REGISTER, "The object {} has already been registered under the same name {} within registry {}, skipping", value, identifier, this.getName());
            return;
        } else if (this.containsValue(value)) { // Value already registered
            throw new IllegalArgumentException("The object " + value + " has already been registered under the name " + this.getKey(value));
        } else if (this.containsKey(identifier)) { // Key already registered
            if (identifier.equals(this.defaultKey) && this.defaultValue != null) // Setting the default key again
                throw new IllegalArgumentException("Cannot override the default entry " + identifier + " within registry " + this.getName());
            DynamicRegistries.LOGGER.debug(REGISTER, "Registry {} Override: {} {} -> {}", this.getName(), identifier, originalEntry, value);
        }

        if (identifier.equals(this.defaultKey)) {
            this.defaultValue = value;
        }

        this.entries.put(identifier, value);
    }

    @Override
    public Codec<V> entryCodec() {
        return this.explodedEntryCodec;
    }

    @Override
    public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        return this.registryEntryCodec.decode(ops, input);
    }

    @Override
    public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        return this.registryEntryCodec.encode(input, ops, prefix);
    }

    private IllegalStateException constructLockedError(String action) {
        return new IllegalStateException("Attempted to " + action + " from " + this.getName() + " while locked");
    }
}
