/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.ashwork.dynamicregistries.core.registry;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ashwork.dynamicregistries.core.AbstractDynamicRegistryManager;
import net.ashwork.dynamicregistries.core.util.IIdentifier;
import net.ashwork.dynamicregistries.core.entry.ICodecEntry;
import net.ashwork.dynamicregistries.core.entry.IDynamicEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * The abstract implementation of {@link IDynamicRegistry}.
 *
 * @param <I> The identifier type
 * @param <V> The dynamic registry entry super type
 * @param <C> The codec registry entry super type
 * @param <R> The dynamic registry type
 */
public abstract class AbstractDynamicRegistry<I extends IIdentifier, V extends IDynamicEntry<I, V>, C extends ICodecEntry<I, V, C>, R extends AbstractDynamicRegistry<I, V, C, R>>
    implements IRegistrableDynamicRegistry<I, V, C>, IModifiableDynamicRegistry<I, V, C> {

    private static final Logger LOGGER = LogManager.getLogger("Dynamic Registry");
    private final AbstractDynamicRegistryManager<I, ?> stage;
    protected final String name;
    protected final AbstractDynamicRegistryBuilder<I, V, C, R, ?> builder;
    private final Class<V> superType;
    private final IRetrievalRegistry<I, C> codecRegistry;
    @Nullable
    private final I defaultKey;
    protected final Codec<V> entryCodec, entryObjectCodec;
    protected final Codec<R> registryCodec;

    protected final BiMap<I, V> entries;
    protected final Map<I, I> aliases;

    @Nullable
    private V defaultValue;
    private boolean locked;

    protected AbstractDynamicRegistry(AbstractDynamicRegistryManager<I, ?> stage, String name, AbstractDynamicRegistryBuilder<I, V, C, R, ?> builder) {
        this.stage = stage;
        if (!VALID_NAMES.asMatchPredicate().test(name))
            throw new IllegalArgumentException(name + " does not match the following regex: ^[a-z][a-z0-9/_]{1,62}[a-z0-9]$");
        this.name = name;
        this.builder = builder;
        this.superType = builder.getSuperType();
        this.codecRegistry = builder.getCodecRegistry();
        this.defaultKey = builder.getDefaultKey();
        this.entryCodec = this.identifierCodec().comapFlatMap(loc -> {
            @Nullable
            V val = this.getValue(loc);
            return val != null ? DataResult.success(val)
                    : DataResult.error("The registry object is not valid within " + this.getName() + ": " + loc);
        }, IDynamicEntry::getEntryName);
        this.entryObjectCodec = this.identifierCodec().comapFlatMap(id -> {
            @Nullable C val = this.codecRegistry.getValue(id);
            return val != null ? DataResult.success(val)
                    : DataResult.error("Not a valid registry object within " + this.codecRegistry.getName() + ": " + id);
        }, ICodecEntry::getEntryName).dispatch(dyn -> (C) dyn.codec(), Function.identity());
        this.entries = HashBiMap.create();
        this.aliases = new HashMap<>();
        this.locked = true;
        this.registryCodec = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.unboundedMap(this.identifierCodec(), this.entryObjectCodec).fieldOf("entries").forGetter(reg -> reg.entries),
                        Codec.unboundedMap(this.identifierCodec(), this.identifierCodec()).fieldOf("aliases").forGetter(reg -> reg.aliases)
                ).apply(instance, (ent, ali) -> this.setData(ent, ali))
        );
    }

    /**
     * @return The current instance
     */
    protected abstract R ret();

    private R setData(final Map<I, V> entries, final Map<I, I> aliases) {
        this.unlock();
        this.clear();
        entries.forEach((id, obj) -> {
            obj.setEntryName(id);
            this.register(obj);
        });
        this.aliases.clear();
        this.aliases.putAll(aliases);
        this.lock();
        return this.ret();
    }

    /**
     * @return The codec used to encode/decode the registry entry's identifier
     */
    protected abstract Codec<I> identifierCodec();

    /**
     * @param stage The stage to copy the registry to
     * @return The copied registry
     */
    public abstract R copy(AbstractDynamicRegistryManager<I, ?> stage);

    public <T> T encodeRegistry(DynamicOps<T> ops) {
        return this.registryCodec.encodeStart(ops, this.ret())
                .getOrThrow(false, LOGGER::error);
    }

    public <T> void decodeRegistry(DynamicOps<T> ops, T input) {
        this.registryCodec.parse(ops, input)
                .getOrThrow(true, LOGGER::error);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Class<V> getEntrySuperType() {
        return this.superType;
    }

    @Override
    public Class<C> getCodecRegistrySuperType() {
        return this.codecRegistry.getSuperType();
    }

    @Override
    public boolean containsKey(I key) {
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
    public V getValue(I key) {
        V ret;
        do {
            ret = this.entries.get(key);
            key = this.aliases.get(key);
        } while (ret == null && key != null);
        return ret == null ? this.defaultValue : ret;
    }

    @Nullable
    @Override
    public I getKey(V value) {
        I ret = this.entries.inverse().get(value);
        return ret == null ? this.defaultKey : ret;
    }

    @Nonnull
    @Override
    public Set<I> keySet() {
        return Collections.unmodifiableSet(this.entries.keySet());
    }

    @Nonnull
    @Override
    public Set<V> values() {
        return Collections.unmodifiableSet(this.entries.values());
    }

    @Nonnull
    @Override
    public Set<Map.Entry<I, V>> entrySet() {
        return Collections.unmodifiableSet(this.entries.entrySet());
    }

    @Override
    public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        return this.entryCodec.encode(input, ops, prefix);
    }

    @Override
    public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        return this.entryCodec.decode(ops, input);
    }

    @Override
    public void clear() {
        if (this.isLocked())
            throw this.constructLockedError("clear");

        this.aliases.clear();
        this.entries.clear();
    }

    @Override
    public V remove(I key) {
        if (this.isLocked())
            throw this.constructLockedError("remove");

        return this.entries.remove(key);
    }

    public void unlock() {
        this.locked = false;
    }

    public void lock() {
        this.locked = true;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public void register(I identifier, JsonElement element) {
        this.parse(JsonOps.INSTANCE, element).result().ifPresentOrElse(registryObject -> {
            registryObject.setEntryName(identifier);
            this.register(registryObject);
        }, () -> LOGGER.error("The following registry object could not be deserialized: {}", identifier));
    }

    public R setFromStage(AbstractDynamicRegistryManager<I, ?> otherStage) {
        AbstractDynamicRegistry<I, V, C, R> otherRegistry = otherStage.getRegistry(this.getName());
        this.unlock();
        this.clear();
        otherRegistry.forEach(this::register);
        return ret();
    }

    @Override
    public void register(V value) {
        if (this.isLocked())
            throw this.constructLockedError("register");

        Preconditions.checkNotNull(value, "Cannot add a null object to the registry.");
        I identifier = value.getEntryName();
        Preconditions.checkNotNull(identifier, "Cannot use a null identifier for entry: " + value);

        V originalEntry = this.getValue(identifier);
        if (originalEntry == value) { // Already registered
            LOGGER.warn("The object {} has already been registered under the same name {} within registry {}, skipping", value, identifier, this.getName());
            return;
        } else if (this.containsValue(value)) { // Value already registered
            throw new IllegalArgumentException("The object " + value + " has already been registered under the name " + this.getKey(value));
        } else if (this.containsKey(identifier)) { // Key already registered
            if (identifier.equals(this.defaultKey) && this.defaultValue != null) // Setting the default key again
                throw new IllegalArgumentException("Cannot override the default entry " + identifier + " within registry " + this.getName());
            LOGGER.debug("Registry {} Override: {} {} -> {}", this.getName(), identifier, originalEntry, value);
        }

        if (identifier.equals(this.defaultKey)) {
            this.defaultValue = value;
        }

        this.entries.put(identifier, value);
    }

    private IllegalStateException constructLockedError(String action) {
        return new IllegalStateException("Attempted to " + action + " from " + this.getName() + " while locked.");
    }
}
