/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.ashwork.dynamicregistries.core.entry;

import com.mojang.serialization.Codec;
import net.ashwork.dynamicregistries.core.util.IIdentifier;

/**
 * A codec entry used to encode/decode a dynamic entry instance.
 * Any dynamic entry must have some codec that can be used. This
 * should be registered to some static registry since they are
 * defined within the user's codebase.
 *
 * @param <I> The identifier type
 * @param <T> The dynamic entry type this entry is associated with
 * @param <C> The codec registry entry super type
 */
public interface ICodecEntry<I extends IIdentifier, T, C>
        extends Codec<T>, IEntry<I, C> {
}
