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
import net.ashwork.dynamicregistries.core.entry.IEntry;

import javax.annotation.Nullable;

/**
 * A wrapper used to retrieve data from some existing registry system.
 * Used to get the codec entries from their associated registry.
 *
 * @param <I> The identifier type
 * @param <V> The registry entry super type
 */
public interface IRetrievalRegistry<I extends IIdentifier, V extends IEntry<I, V>> {

    /**
     * @return Gets the name of the registry
     */
    I getName();

    /**
     * @return Gets the registry type
     */
    Class<V> getSuperType();

    /**
     * @param key The identifier key
     * @return The registry entry associated with the identifier,
     *         null otherwise
     */
    @Nullable
    V getValue(I key);
}
