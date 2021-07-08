/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries;

import net.ashwork.dynamicregistries.client.DynamicRegistriesClient;
import net.ashwork.dynamicregistries.event.DynamicRegistryEvent;
import net.ashwork.dynamicregistries.network.DynamicRegistryPacket;
import net.ashwork.dynamicregistries.registry.DynamicRegistry;
import net.ashwork.dynamicregistries.registry.IRegistrableDynamicRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;

import java.util.Objects;
import java.util.function.Function;

/**
 * The base mod class for dynamic registries.
 */
@Mod(DynamicRegistries.ID)
public final class DynamicRegistries {

    /**
     * The identifier of the mod.
     */
    public static final String ID = "dynamicregistries";
    /**
     * The logger used for logging data within the mod.
     */
    public static final Logger LOGGER = LogManager.getLogger("Dynamic Registries");
    //TODO: Create protocol versioning for registry data
    private static final String NETWORK_PROTOCOL_VERSION = "";
    /**
     * Holds the current instance of the loaded mod.
     */
    private static DynamicRegistries instance;

    /**
     * The current data listener for registering objects dynamically.
     */
    private final DynamicRegistryListener registryListener;
    /**
     * A network channel for sending data across the network.
     */
    private final SimpleChannel channel;
    /**
     * Gets the current data storage of a server.
     */
    private final Function<MinecraftServer, DynamicRegistryData> dataGetter;
    /**
     * Keeps track of whether the current registry cache is invalidated and needs to be updated.
     */
    private boolean invalidateCache;

    /**
     * Used for setting up all buses and networks within the mod.
     */
    public DynamicRegistries() {
        instance = this;
        this.registryListener = new DynamicRegistryListener();
        this.channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(ID, "network"), () -> NETWORK_PROTOCOL_VERSION, str -> Objects.equals(str, NETWORK_PROTOCOL_VERSION), str -> Objects.equals(str, NETWORK_PROTOCOL_VERSION));
        this.dataGetter = server -> Objects.requireNonNull(server.getLevel(World.OVERWORLD), "The Overworld is currently null, make sure you are not calling this ").getDataStorage().computeIfAbsent(DynamicRegistryData::new, ID);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus(),
                forgeBus = MinecraftForge.EVENT_BUS;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> new DynamicRegistriesClient(modBus, forgeBus));

        modBus.addListener(this::setupRegistries);
        forgeBus.addListener(this::attachDataStorage);
        forgeBus.addListener(this::addReloadListener);
        forgeBus.addListener(this::serverTick);
    }

    /**
     * Returns the currently loaded instance of the mod.
     *
     * @return the currently loaded instance of the mod
     */
    public static DynamicRegistries instance() {
        return instance;
    }

    /**
     * Gets the network channel.
     *
     * @return the network channel
     */
    public SimpleChannel getChannel() {
        return channel;
    }

    /**
     * Invalidates any cache held by this mod to update information on their
     * respective sides.
     */
    public void invalidate() {
        this.invalidateCache = true;
    }

    /**
     * Sets up the network packets and the static registry instances.
     *
     * @param event the event instance
     */
    private void setupRegistries(final FMLCommonSetupEvent event) {
        this.channel.messageBuilder(DynamicRegistryPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DynamicRegistryPacket::encode)
                .decoder(DynamicRegistryPacket::new)
                .consumer(DynamicRegistryPacket::handle)
                .add();
        event.enqueueWork(() -> {
            LOGGER.debug(MarkerManager.getMarker("New Registry"), "Creating new registries");
            ModLoader.get().postEvent(new DynamicRegistryEvent.NewRegistry());
            DynamicRegistryManager.STATIC.registries(DynamicRegistryManager.Lookup.ALL).forEach(entry -> {
                LOGGER.debug(IRegistrableDynamicRegistry.REGISTER, "Registering entries to {}", entry.getKey());
                final DynamicRegistry<?, ?> registry = entry.getValue();
                registry.unlock();
                ModLoader.get().postEvent(new DynamicRegistryEvent.Register<>(registry));
                registry.lock();
            });
        });
    }

    /**
     * Attach the data storage of the dynamic registries to the Overworld.
     *
     * @param event the event instance
     */
    private void attachDataStorage(final FMLServerStartingEvent event) {
        this.dataGetter.apply(event.getServer());
    }

    /**
     * Add the registry reload listener such that the data is updated on reload.
     *
     * @param event the event instance
     */
    private void addReloadListener(final AddReloadListenerEvent event) {
        event.addListener(this.registryListener);
    }

    /**
     * Checks whether the registry needs to be synced to the client and does so.
     *
     * @implNote
     * Doing this within the reload listener causes a crash as we try to send a
     * on the wrong thread too early. So, we use this as a buffer zone to handle
     * that behavior sanely.
     *
     * @param event the event instance
     */
    private void serverTick(final TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        if (this.invalidateCache) {
            DynamicRegistryManager.DYNAMIC.sendToClient();
            this.dataGetter.apply(LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER)).setDirty();
            this.invalidateCache = false;
        }
    }
}
