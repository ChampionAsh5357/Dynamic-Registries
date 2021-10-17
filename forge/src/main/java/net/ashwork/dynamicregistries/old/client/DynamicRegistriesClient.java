/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old.client;

import net.ashwork.dynamicregistries.old.DynamicRegistryManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Map;
import java.util.Objects;

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

        forgeBus.addListener(this::playerLeave);
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
    public void handleClientRegistry(final String stage, final Map<ResourceLocation, CompoundTag> snapshots) {
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
        snapshots.forEach((name, snapshot) -> Objects.requireNonNull(stageManager.getRegistry(name), "This registry is not available on the client: " + name)
                .fromSnapshot(snapshot, NbtOps.INSTANCE, false));
    }

    /**
     * Whenever the client player leaves the world, we want to invalidate all data within the current
     * registries to prevent any cross world contamination.
     *
     * @param event the event instance
     */
    private void playerLeave(final ClientPlayerNetworkEvent.LoggedOutEvent event) {
        DynamicRegistryManager.DYNAMIC.registries(DynamicRegistryManager.Lookup.ALL).map(Map.Entry::getValue).forEach(registry -> {
            registry.unlock();
            registry.clear();
            registry.lock();
        });
    }
}
