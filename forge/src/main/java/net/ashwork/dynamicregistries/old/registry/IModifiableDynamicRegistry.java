/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old.registry;

import net.ashwork.dynamicregistries.old.entry.ICodecEntry;
import net.ashwork.dynamicregistries.old.entry.IDynamicEntry;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;

/**
 * A modifiable instance of a dynamic registry.
 *
 * @param <V> the super type of the dynamic registry entry
 * @param <C> the super type of the codec registry entry
 */
public interface IModifiableDynamicRegistry<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> extends IDynamicRegistry<V, C> {

    /**
     * A marker that represents all logging information while modifying existing registry data.
     */
    Marker MODIFY = MarkerManager.getMarker("Modify Registry");

    /**
     * Returns whether the registry is unable to be modified.
     *
     * @return {@code true} if the registry is unable to be modified, otherwise
     *         {@code false}
     */
    boolean isLocked();

    /**
     * Removes a registry object from the registry if present. Otherwise,
     * nothing occurs.
     *
     * @param key the identifier of the registry object
     * @return the removed registry object, or {@code null} if no entry was present.
     */
    @Nullable
    V remove(final ResourceLocation key);

    /**
     * Clears the registry of all data related to entries.
     */
    void clear();
}
