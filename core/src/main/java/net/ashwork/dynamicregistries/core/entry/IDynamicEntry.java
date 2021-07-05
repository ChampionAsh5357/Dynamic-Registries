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

/**
 * A dynamic entry instance. Any dynamic registry must have entries
 * that implement this interface. Each dynamic registry holds the codec
 * entry that can encode/decode that particular instance.
 *
 * @param <I> The identifier type
 * @param <T> The dynamic registry entry super type
 */
public interface IDynamicEntry<I extends IIdentifier, T> extends IEntry<I, T> {

    /**
     * @return The codec entry that can encode/decode this instance
     */
    ICodecEntry<I, ? extends T, ?> codec();
}
