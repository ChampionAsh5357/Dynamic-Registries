/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.ashwork.dynamicregistries.core.registry;

import net.ashwork.dynamicregistries.core.util.IIdentifier;
import net.ashwork.dynamicregistries.core.entry.ICodecEntry;
import net.ashwork.dynamicregistries.core.entry.IDynamicEntry;

/**
 * A modifiable instance of a dynamic registry.
 *
 * @param <I> The identifier type
 * @param <V> The dynamic registry entry super type
 * @param <C> The codec registry entry super type
 */
public interface IModifiableDynamicRegistry<I extends IIdentifier, V extends IDynamicEntry<I, V>, C extends ICodecEntry<I, V, C>>
        extends IDynamicRegistry<I, V, C> {

    /**
     * Clears the registry of all entry data.
     */
    void clear();

    /**
     * Removes an entry from the registry
     *
     * @param key The identifier the value is associated with
     * @return The removed entry, or null if no entry was available
     */
    V remove(I key);

    /**
     * @return If the registry is currently locked and unable
     *         to be modified
     */
    boolean isLocked();
}
