/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old.entry;

import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * A dynamic entry instance. All dynamic registries will implement
 * this interface. An entry will hold a reference to the codec
 * entry that can encode/decode that particular instance.
 *
 * @param <V> the super type of the dynamic registry entry
 */
public interface IDynamicEntry<V> extends IForgeRegistryEntry<V> {

    /**
     * Returns the code entry that encodes/decodes this instance.
     *
     * @return the code entry that encodes/decodes this instance
     */
    ICodecEntry<? extends V, ?> codec();
}
