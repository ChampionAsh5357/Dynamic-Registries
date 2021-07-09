/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.client;

import net.ashwork.dynamicregistries.DynamicRegistryManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Map;

/**
 * The isolated client instance of the base mod class.
 */
public final class DynamicRegistriesClient {

    /**
     * Holds the current client instance of the loaded mod.
     */
    private static DynamicRegistriesClient instance;

    /**
     * Used for setting up all buses and clientside hooks within the mod.
     *
     * @param modBus the mod event bus
     * @param forgeBus the forge event bus
     */
    public DynamicRegistriesClient(final IEventBus modBus, final IEventBus forgeBus) {
        instance = this;
    }

    /**
     * Returns the currently loaded client instance of the mod.
     *
     * @return the currently loaded client instance of the mod
     */
    public static DynamicRegistriesClient instance() {
        return instance;
    }

    /**
     * Whenever registry data is sent from the server to the client, the existing
     * registries are overwritten and replaced with the new data.
     *
     * @param stage the stage to set the data within
     * @param snapshots a map of registry name to registry snapshot data
     */
    public void handleClientRegistry(final String stage, final Map<ResourceLocation, CompoundNBT> snapshots) {
        DynamicRegistryManager stageManager;
        switch (stage) {
            case "Static":
                stageManager = DynamicRegistryManager.STATIC;
                break;
            case "Dynamic":
                stageManager = DynamicRegistryManager.DYNAMIC;
                break;
            default:
                throw new IllegalArgumentException("Invalid registry manager stage: " + stage);
        }
        snapshots.forEach((name, snapshot) -> stageManager.getRegistry(name).fromSnapshot(snapshot, NBTDynamicOps.INSTANCE));
    }
}
