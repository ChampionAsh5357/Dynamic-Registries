/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.registry;

import net.ashwork.dynamicregistries.entry.ICodecEntry;
import net.ashwork.dynamicregistries.entry.IDynamicEntry;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * A registrable instance of an {@link IDynamicRegistry}.
 *
 * @param <V> the super type of the dynamic registry entry
 * @param <C> the super type of the codec registry entry
 */
public interface IRegistrableDynamicRegistry<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> extends IDynamicRegistry<V, C> {

    /**
     * A marker that represents all logging information while registering data.
     */
    Marker REGISTER = MarkerManager.getMarker("Register");

    /**
     * Registers a registry object to the registry. If the identifier is not
     * set, an exception will be thrown.
     *
     * @param value the registry object to be registered
     * @throws NullPointerException if the value or the identifier is null
     */
    void register(final V value);

    /**
     * Registers all registry objects to the registry.
     *
     * @param values the registry objects to be registered
     *
     * @see #register(IDynamicEntry)
     */
    @SuppressWarnings("unchecked")
    default void registerAll(final V... values) {
        for (V value : values) this.register(value);
    }
}
