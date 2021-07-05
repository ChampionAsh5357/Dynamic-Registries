/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.ashwork.dynamicregistries.core.entry;

import net.ashwork.dynamicregistries.core.util.IIdentifier;

import javax.annotation.Nullable;

/**
 * An entry that is added to some registry. All entries
 * added to the registry must subclass this in some fashion.
 *
 * @param <I> The identifier type
 * @param <V> The registry entry super type
 */
public interface IEntry<I extends IIdentifier, V> {

    /**
     * Sets a unique name for this entry.
     *
     * @param name The name of the entry
     * @return The current registry instance
     */
    V setEntryName(I name);

    /**
     * @return The identifier set for this entry,
     *         or null if not set
     */
    @Nullable
    I getEntryName();

    /**
     * @return The registry class type
     */
    Class<V> getRegistrySuperType();
}
