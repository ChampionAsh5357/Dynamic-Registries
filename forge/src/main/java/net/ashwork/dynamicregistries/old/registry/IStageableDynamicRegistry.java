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

import java.util.Set;

/**
 * A stageable instance of an {@link IDynamicRegistry}.
 *
 * @param <V> the super type of the dynamic registry entry
 * @param <C> the super type of the codec registry entry
 */
public interface IStageableDynamicRegistry<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> extends IDynamicRegistry<V, C> {

    /**
     * Creates a new empty instance of the current registry for the following {@code stage}.
     *
     * @param stage the new registry stage
     * @return a new instance of the current registry.
     */
    IStageableDynamicRegistry<V, C> copy(final DynamicRegistryManager stage);

    /**
     * Sets the data of the registry from that of a different stage and unlocks the registry
     * for later processing.
     *
     * @param stage the registry stage to copy data from
     * @return a set of old registry names
     */
    Set<ResourceLocation> setAndUnlockFromStage(final DynamicRegistryManager stage);
}
