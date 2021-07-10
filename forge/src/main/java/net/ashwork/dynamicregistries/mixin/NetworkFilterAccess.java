/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.mixin;

import net.minecraft.network.NetworkManager;
import net.minecraftforge.network.NetworkFilters;
import net.minecraftforge.network.VanillaPacketFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.function.Function;

/**
 * A mixin interface used to append onto the existing packet filters.
 */
@Mixin(value = NetworkFilters.class, remap = false)
public interface NetworkFilterAccess {

    /**
     * Sets the filter instances. Should only be used to append the data and not completely overwrite it.
     *
     * @param filters the network manager packet filters
     */
    @Accessor("instances")
    static void setFilterInstances(final Map<String, Function<NetworkManager, VanillaPacketFilter>> filters) {
        throw new AssertionError("Mixin not applied for setting filter instances");
    }

    /**
     * Gets the filter instances. Used to grab the existing entries and then append later.
     *
     * @return the filter instances
     */
    @Accessor("instances")
    static Map<String, Function<NetworkManager, VanillaPacketFilter>> getFilterInstances() {
        throw new AssertionError("Mixin not applied for getting filter instances");
    }
}
