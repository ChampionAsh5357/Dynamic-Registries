/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old.entry;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * A codec entry used to encode/decode an {@link IDynamicEntry} instance.
 * Any dynamic entry must have some codec entry to properly write/read the
 * data. This must be registered to some static {@link IForgeRegistry}
 * instance since they are defined in the user codebase.
 *
 * @param <V> the super type of the dynamic registry entry
 * @param <C> the super type of the codec registry entry
 */
public interface ICodecEntry<V extends IDynamicEntry<?>, C> extends IForgeRegistryEntry<C> {

    Codec<? extends V> entryCodec();
}
