/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.registry;

import net.ashwork.dynamicregistries.DynamicRegistryManager;
import net.ashwork.dynamicregistries.entry.ICodecEntry;
import net.ashwork.dynamicregistries.entry.IDynamicEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

//TODO: Document and implement
public class DynamicRegistryBuilder<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> {

    private final ResourceLocation name;
    private final Class<V> superType;
    private final IForgeRegistry<C> codecRegistry;
    private ResourceLocation defaultKey;
    private boolean sync, save;
    private final Set<ResourceLocation> legacyNames;

    protected DynamicRegistryBuilder(final ResourceLocation name, final Class<V> superType, final IForgeRegistry<C> codecRegistry) {
        if(!IDynamicRegistry.VALID_NAMES.matcher(Objects.requireNonNull(name, "The name of the registry cannot be null").getPath()).matches())
            throw new IllegalArgumentException(name + " is not valid: ^[a-z][a-z0-9_-]{1,63}$");
        this.name = name;
        this.superType = Objects.requireNonNull(superType, "The super type of the dynamic registry entry cannot be null");
        this.codecRegistry = Objects.requireNonNull(codecRegistry, "The codec registry cannot be null");
        this.sync = true;
        this.save = true;
        this.legacyNames = new HashSet<>();
    }

    public DynamicRegistryBuilder<V, C> setDefaultKey(final ResourceLocation defaultKey) {
        this.defaultKey = defaultKey;
        return this;
    }

    public DynamicRegistryBuilder<V, C> doNotSync() {
        this.sync = false;
        return this;
    }

    public DynamicRegistryBuilder<V, C> doNotSave() {
        this.save = false;
        return this;
    }

    public DynamicRegistryBuilder<V, C> legacyName(final ResourceLocation name) {
        this.legacyNames.add(Objects.requireNonNull(name, "A legacy name should not be null"));
        return this;
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public Class<V> getSuperType() {
        return this.superType;
    }

    public IForgeRegistry<C> getCodecRegistry() {
        return this.codecRegistry;
    }

    public ResourceLocation getDefaultKey() {
        return this.defaultKey;
    }

    public boolean shouldSync() {
        return this.sync;
    }

    public boolean shouldSave() {
        return this.save;
    }

    public Set<ResourceLocation> getLegacyNames() {
        return this.legacyNames;
    }

    public IDynamicRegistry<V, C> create() {
        return DynamicRegistryManager.STATIC.createRegistry(this);
    }
}
