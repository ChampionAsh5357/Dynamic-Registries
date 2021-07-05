/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.ashwork.dynamicregistries.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import net.ashwork.dynamicregistries.core.entry.ICodecEntry;
import net.ashwork.dynamicregistries.core.entry.IDynamicEntry;
import net.ashwork.dynamicregistries.core.registry.AbstractDynamicRegistry;
import net.ashwork.dynamicregistries.core.registry.AbstractDynamicRegistryBuilder;
import net.ashwork.dynamicregistries.core.util.IIdentifier;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple abstract manager used to keep track of
 * registry information during particular stages (e.g.
 * static or dynamic).
 *
 * @param <I> The identifier type
 * @param <M> The manager type
 */
public abstract class AbstractDynamicRegistryManager<I extends IIdentifier, M extends AbstractDynamicRegistryManager<I, M>> {

    private final String stage;
    private final BiMap<String, AbstractDynamicRegistry<I, ?, ?, ?>> registries;
    private final BiMap<Class<? extends IDynamicEntry<I, ?>>, String> superTypes;
    protected final Set<String> persisted, synced;
    protected final Map<String, String> legacyNames;
    private boolean needsSync;

    protected AbstractDynamicRegistryManager(String stage) {
        this.stage = stage;
        this.registries = HashBiMap.create();
        this.superTypes = HashBiMap.create();
        this.persisted = new HashSet<>();
        this.synced = new HashSet<>();
        this.legacyNames = new HashMap<>();
        this.needsSync = false;
    }

    public String getStage() {
        return this.stage;
    }

    /**
     * Gets the registry from the name.
     *
     * @param name The name of the registry
     * @param <V> The dynamic registry entry super type
     * @param <C> The codec registry entry super type
     * @param <R> The registry type
     * @return The dynamic registry
     */
    @SuppressWarnings("unchecked")
    public <V extends IDynamicEntry<I, V>, C extends ICodecEntry<I, V, C>, R extends AbstractDynamicRegistry<I, V, C, R>> R getRegistry(String name) {
        return (R) this.registries.get(name);
    }

    /**
     * Gets the registry from the entry class.
     *
     * @param cls The super class of the dynamic registry
     * @param <V> The dynamic registry entry super type
     * @param <C> The codec registry entry super type
     * @param <R> The registry type
     * @return The dynamic registry
     */
    public <V extends IDynamicEntry<I, V>, C extends ICodecEntry<I, V, C>, R extends AbstractDynamicRegistry<I, V, C, R>> R getRegistry(Class<? super V> cls) {
        return this.getRegistry(this.superTypes.get(cls));
    }

    /**
     * Gets the registered name of the registry.
     *
     * @param registry The dynamic registry
     * @param <V> The dynamic registry entry super type
     * @param <C> The codec registry entry super type
     * @param <R> The registry type
     * @return The name of the registry
     */
    public <V extends IDynamicEntry<I, V>, C extends ICodecEntry<I, V, C>, R extends AbstractDynamicRegistry<I, V, C, R>> String getName(R registry) {
        return this.registries.inverse().get(registry);
    }

    @Nullable
    public <V extends IDynamicEntry<I, V>, C extends ICodecEntry<I, V, C>, R extends AbstractDynamicRegistry<I, V, C, R>> R toManager(String name, M manager) {
        if (!this.registries.containsKey(name)) {
            R stagedRegistry = manager.getRegistry(name);
            if (stagedRegistry == null) return null;
            this.registries.put(name, stagedRegistry.copy(this));
            this.superTypes.put(stagedRegistry.getEntrySuperType(), name);
            if (manager.persisted.contains(name)) this.persisted.add(name);
            if (manager.synced.contains(name)) this.synced.add(name);
            manager.legacyNames.entrySet().stream()
                    .filter(e -> e.getValue().equals(name))
                    .forEach(e -> this.addLegacyName(e.getKey(), e.getValue()));
        }
        return this.getRegistry(name);
    }

    public void reload(Map<I, JsonElement> entries, AbstractDynamicRegistryManager<I, ?> stage) {
        this.registries.values().stream().map(registry -> registry.setFromStage(stage));
        this.handleRegistries(entries);
        this.registries.values().forEach(AbstractDynamicRegistry::lock);
        this.needsSync = true;
    }

    protected abstract void handleRegistries(Map<I, JsonElement> entries);

    public boolean needsSync() {
        return this.needsSync;
    }

    protected void sync() {
        this.needsSync = false;
    }

    protected void addLegacyName(String legacyName, String currentName) {
        if (this.legacyNames.putIfAbsent(legacyName, currentName) != null)
            throw new IllegalArgumentException("Legacy name is already linked to an existing registry: " + legacyName + " -> " + currentName);
    }

    public <V extends IDynamicEntry<I, V>, C extends ICodecEntry<I, V, C>, R extends AbstractDynamicRegistry<I, V, C, R>, B extends AbstractDynamicRegistryBuilder<I, V, C, R, B>> R createRegistry(B builder) {
        Set<Class<?>> parents = new HashSet<>();
        this.findSuperTypes(builder.getSuperType(), parents);
        Sets.SetView<Class<?>> overlaps = Sets.intersection(parents, this.superTypes.keySet());
        if (!overlaps.isEmpty()) {
            Class<?> found = overlaps.iterator().next();
            throw new IllegalArgumentException("Found existing registry containing " + found + ": " + this.superTypes.get(found));
        }
        String name = builder.getName();
        R registry = (R) this.constructRegistry(builder);
        this.registries.put(name, registry);
        this.superTypes.put(builder.getSuperType(), name);
        if (builder.shouldSaveToDisk()) this.persisted.add(name);
        if (builder.shouldSync()) this.synced.add(name);
        builder.getLegacyNames().forEach(legacyName -> this.addLegacyName(legacyName, name));
        return this.getRegistry(name);
    }

    /**
     * @param builder The builder the registry is being constructed from
     * @return The constructed dynamic registry
     */
    protected abstract AbstractDynamicRegistry<I, ?, ?, ?> constructRegistry(AbstractDynamicRegistryBuilder<I, ?, ?, ?, ?> builder);

    protected void findSuperTypes(Class<?> type, Set<Class<?>> types) {
        if (type == null || type == Object.class) return;
        types.add(type);
        for (Class<?> i : type.getInterfaces()) this.findSuperTypes(i, types);
        this.findSuperTypes(type.getSuperclass(), types);
    }

    /**
     * @param lookup The lookup type to get the associated registries from
     * @return A map containing the currently wanted registries
     */
    protected Map<String, AbstractDynamicRegistry<I, ?, ?, ?>> filteredRegistries(Lookup lookup) {
        return this.registries.entrySet().stream().filter(entry ->
                switch (lookup) {
                    case ALL -> true;
                    case SAVE -> this.persisted.contains(entry.getKey());
                    case SYNC -> this.synced.contains(entry.getKey());
                }
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public enum Lookup {
        ALL,
        SAVE,
        SYNC
    }
}
