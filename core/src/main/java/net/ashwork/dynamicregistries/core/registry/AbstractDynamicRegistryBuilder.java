/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.ashwork.dynamicregistries.core.registry;

import net.ashwork.dynamicregistries.core.entry.ICodecEntry;
import net.ashwork.dynamicregistries.core.entry.IDynamicEntry;
import net.ashwork.dynamicregistries.core.util.IIdentifier;

import java.util.HashSet;
import java.util.Set;

/**
 * A builder used to create a dynamic registry.
 *
 * @param <I> The identifier type
 * @param <V> The dynamic registry entry super type
 * @param <C> The codec registry entry super type
 * @param <R> The dynamic registry type
 * @param <B> The builder type
 */
public abstract class AbstractDynamicRegistryBuilder<I extends IIdentifier, V extends IDynamicEntry<I, V>, C extends ICodecEntry<I, V, C>, R extends AbstractDynamicRegistry<I, V, C, R>, B extends AbstractDynamicRegistryBuilder<I, V, C, R, B>> {

    private final String name;
    private final Class<V> superType;
    private final IRetrievalRegistry<I, C> codecRegistry;
    private I defaultKey;
    private boolean sync;
    private boolean saveToDisk;
    private final Set<String> legacyNames;

    protected AbstractDynamicRegistryBuilder(String name, Class<V> superType, IRetrievalRegistry<I, C> codecRegistry) {
        if (!IDynamicRegistry.VALID_NAMES.asMatchPredicate().test(name))
            throw new IllegalArgumentException(name + " does not match the following regex: ^[a-z][a-z0-9/_]{1,62}[a-z0-9]$");
        this.name = name;
        this.superType = superType;
        this.codecRegistry = codecRegistry;
        this.sync = true;
        this.saveToDisk = true;
        this.legacyNames = new HashSet<>();
    }

    /**
     * @return The current instance
     */
    protected abstract B ret();

    /**
     * @apiNote This method should be given a public counterpart
     *          that references the identifier object directly
     *          instead of using the below wrapper method.
     *
     * @param defaultKey The default entry if the value does not exist
     * @return The builder instance
     */
    protected B setDefaultKey(I defaultKey) {
        this.defaultKey = defaultKey;
        return ret();
    }

    /**
     * Tells the manager that this registry should not be synced.
     *
     * @return The builder instance
     */
    public B disableSync() {
        this.sync = false;
        return ret();
    }

    /**
     * Tells the manager that this registry should not be saved.
     *
     * @return The builder instance
     */
    public B doNotSave() {
        this.saveToDisk = false;
        return ret();
    }

    /**
     * Adds a legacy name this registry might also be called.
     *
     * @param name The legacy name of the registry
     * @return The builder instance
     */
    public B legacyName(String name) {
        this.legacyNames.add(name);
        return ret();
    }

    public String getName() {
        return this.name;
    }

    public Class<V> getSuperType() {
        return this.superType;
    }

    public IRetrievalRegistry<I, C> getCodecRegistry() {
        return this.codecRegistry;
    }

    public I getDefaultKey() {
        return this.defaultKey;
    }

    public boolean shouldSync() {
        return this.sync;
    }

    public boolean shouldSaveToDisk() {
        return this.saveToDisk;
    }

    public Set<String> getLegacyNames() {
        return this.legacyNames;
    }

    /**
     * @return Creates the registry associated with the builder,
     *         should reference the static registry manager by
     *         default.
     */
    public abstract IDynamicRegistry<I, V, C> create();
}
