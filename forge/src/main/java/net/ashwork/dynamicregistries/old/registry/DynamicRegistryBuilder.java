/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old.registry;

import net.ashwork.dynamicregistries.old.DynamicRegistryManager;
import net.ashwork.dynamicregistries.old.entry.ICodecEntry;
import net.ashwork.dynamicregistries.old.entry.IDynamicEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A builder used to hold common configurations of the dynamic registry.
 *
 * @param <V> the super type of the dynamic registry entry
 * @param <C> the super type of the codec registry entry
 */
public class DynamicRegistryBuilder<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> {

    /**
     * The name of the dynamic registry.
     */
    private final ResourceLocation name;
    /**
     * The super type of the dynamic registry.
     */
    private final Class<V> superType;
    /**
     * The codec registry used to encode/decode the dynamic registry.
     */
    private final IForgeRegistry<C> codecRegistry;
    /**
     * The default key of the dynamic registry.
     */
    @Nullable
    private ResourceLocation defaultKey;
    /**
     * {@code true} if the registry should be synced to the client or saved to disk respectively.
     */
    private boolean sync, save;
    /**
     * The prior names of the dynamic registry.
     */
    private final Set<ResourceLocation> legacyNames;
    /**
     * The reload strategy of the dynamic registry.
     */
    private DynamicRegistry.ReloadStrategy reloadStrategy = DynamicRegistry.ReloadStrategy.CLEAR;

    /**
     * Constructs an instance of the builder.
     *
     * @param name the name of the dynamic registry
     * @param superType the super type of the dynamic registry
     * @param codecRegistry the codec registry used to encode/decode the dynamic registry
     */
    public DynamicRegistryBuilder(final ResourceLocation name, final Class<V> superType, final IForgeRegistry<C> codecRegistry) {
        if(!IDynamicRegistry.VALID_NAMES.matcher(Objects.requireNonNull(name, "The name of the registry cannot be null").getPath()).matches())
            throw new IllegalArgumentException(name + " is not valid: ^[a-z][a-z0-9_-]{1,63}$");
        if(name.getNamespace().equals("missing_mappings"))
            throw new IllegalArgumentException("'missing_mappings' is a reserved namespace within dynamic registries");
        this.name = name;
        this.superType = Objects.requireNonNull(superType, "The super type of the dynamic registry entry cannot be null");
        this.codecRegistry = Objects.requireNonNull(codecRegistry, "The codec registry cannot be null");
        this.sync = true;
        this.save = true;
        this.legacyNames = new HashSet<>();
    }

    /**
     * Sets the default key of the dynamic registry.
     *
     * @param defaultKey the default key of the dynamic registry
     * @return the builder instance
     */
    public DynamicRegistryBuilder<V, C> setDefaultKey(final ResourceLocation defaultKey) {
        this.defaultKey = defaultKey;
        return this;
    }

    /**
     * Sets a flag that prevents a dynamic registry from syncing to the client.
     *
     * @return the builder instance
     */
    public DynamicRegistryBuilder<V, C> doNotSync() {
        this.sync = false;
        return this;
    }

    /**
     * Sets a flag that prevents a dynamic registry from saving to disk.
     *
     * @return the builder instance
     */
    public DynamicRegistryBuilder<V, C> doNotSave() {
        this.save = false;
        return this;
    }

    /**
     * Adds a prior name of this dynamic registry.
     *
     * @param name the prior name of this dynamic registry
     * @return the builder instance
     */
    public DynamicRegistryBuilder<V, C> legacyName(final ResourceLocation name) {
        this.legacyNames.add(Objects.requireNonNull(name, "A legacy name should not be null"));
        return this;
    }

    /**
     * Instead of clearing the registry each time the entries are reloaded, it will
     * instead append or replace the already existing entries.
     *
     * @return the builder instance
     */
    public DynamicRegistryBuilder<V, C> appendAndReplaceEntries() {
        this.reloadStrategy = DynamicRegistry.ReloadStrategy.REPLACE;
        return this;
    }

    /**
     * Returns the name of the dynamic registry.
     *
     * @return the name of the dynamic registry
     */
    public ResourceLocation getName() {
        return this.name;
    }

    /**
     * Returns the super type of the dynamic registry.
     *
     * @return the super type of the dynamic registry
     */
    public Class<V> getSuperType() {
        return this.superType;
    }

    /**
     * Returns the codec registry used to encode/decode the dynamic registry.
     *
     * @return the codec registry used to encode/decode the dynamic registry
     */
    public IForgeRegistry<C> getCodecRegistry() {
        return this.codecRegistry;
    }

    /**
     * Returns the defaulted entry key of the dynamic registry, or {@code null}
     * if none is specified.
     *
     * @return the defaulted entry key of the dynamic registry, or {@code null}
     */
    @Nullable
    public ResourceLocation getDefaultKey() {
        return this.defaultKey;
    }

    /**
     * Returns {@code true} if the dynamic registry should be synced to the client.
     *
     * @return {@code true} if the dynamic registry should be synced to the client.
     */
    public boolean shouldSync() {
        return this.sync;
    }

    /**
     * Returns {@code true} if the dynamic registry should be saved to disk.
     *
     * @return {@code true} if the dynamic registry should be saved to disk
     */
    public boolean shouldSave() {
        return this.save;
    }

    /**
     * Gets the legacy names of the dynamic registry.
     *
     * @return the legacy names of the dynamic registry
     */
    public Set<ResourceLocation> getLegacyNames() {
        return this.legacyNames;
    }

    /**
     * Returns the reload strategy of the registry.
     *
     * @return the reload strategy of the registry
     */
    public DynamicRegistry.ReloadStrategy getReloadStrategy() {
        return this.reloadStrategy;
    }

    /**
     * Creates and returns a new dynamic registry within the static stage.
     *
     * @return a new dynamic registry within the static stage
     */
    public IDynamicRegistry<V, C> create() {
        return DynamicRegistryManager.STATIC.createRegistry(this);
    }
}
