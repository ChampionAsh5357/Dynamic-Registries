/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.entry;

import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Default implementation of {@link IDynamicEntry}.
 *
 * @param <V> the super type of the dynamic registry entry
 */
public abstract class DynamicEntry<V extends IDynamicEntry<V>> extends ForgeRegistryEntry.UncheckedRegistryEntry<V> implements IDynamicEntry<V> {}
