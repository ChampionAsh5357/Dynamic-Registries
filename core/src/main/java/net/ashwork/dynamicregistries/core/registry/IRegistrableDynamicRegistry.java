/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.ashwork.dynamicregistries.core.registry;

import com.google.gson.JsonElement;
import net.ashwork.dynamicregistries.core.util.IIdentifier;
import net.ashwork.dynamicregistries.core.entry.ICodecEntry;
import net.ashwork.dynamicregistries.core.entry.IDynamicEntry;

/**
 * A registrable instance of a dynamic registry.
 *
 * @param <I> The identifier type
 * @param <V> The dynamic registry entry super type
 * @param <C> The codec registry entry super type
 */
public interface IRegistrableDynamicRegistry<I extends IIdentifier, V extends IDynamicEntry<I, V>, C extends ICodecEntry<I, V, C>>
        extends IDynamicRegistry<I, V, C>{

    /**
     * Registers a value to the registry.
     * The entry name should be set before
     * registering.
     *
     * @param value The value to be registered
     */
    void register(V value);

    /**
     * Registers a value to the registry from a serialized
     * object.
     *
     * @param identifier The identifier of the registry object
     * @param element The serialized registry object
     */
    void register(I identifier, JsonElement element);

    /**
     * Registers all values to the registry via
     * {@link #register(IDynamicEntry)}.
     *
     * @param values The values to be registered
     */
    default void registerAll(@SuppressWarnings("unchecked") V... values) {
        for (V value : values) this.register(value);
    }
}
