/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries;

import com.google.common.collect.ImmutableMap;
import net.ashwork.dynamicregistries.client.DynamicRegistriesClient;
import net.ashwork.dynamicregistries.event.DynamicRegistryEvent;
import net.ashwork.dynamicregistries.mixin.NetworkFilterAccess;
import net.ashwork.dynamicregistries.network.DynamicRegistryNetworkFilter;
import net.ashwork.dynamicregistries.network.DynamicRegistryPacket;
import net.ashwork.dynamicregistries.registry.DynamicRegistry;
import net.ashwork.dynamicregistries.registry.IRegistrableDynamicRegistry;
import net.minecraft.network.NetworkManager;
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
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.network.VanillaPacketFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    /**
     * The network identifier of the mod.
     */
    public static final ResourceLocation NETWORK_ID = new ResourceLocation(ID, "network");
    /**
     * The network protocol version of the mod.
     *
     * @implSpec
     * Network protocol is broken down into three portions:
     * <li>
     *     <ul>Production (p): The currently built version of the mod.</ul>
     *     <ul>Snapshot (s): An unstable version of the mod.</ul>
     *     <ul>Changes (c): Changes within one of the above version.</ul>
     * </li>
     * The versioning protocol will look like {@code (?:p|s)([0-9]*[1-9])c([0-9]+)}.
     * If a version is in production, changes are considered to be compatible and will only check
     * the beginning portion of the string. If a version is in snapshot, the exact protocol string
     * will be checked.
     */
    private static final String NETWORK_PROTOCOL_VERSION = "s1c3";
    /**
     * The protocol version regex to compare against.
     *
     * @see #NETWORK_PROTOCOL_VERSION
     */
    private static final Pattern PROTOCOL_VERSIONS = Pattern.compile("([ps])([0-9]*[1-9])c([0-9]+)");
    /**
     * Holds the current instance of the loaded mod.
     */
    private static DynamicRegistries instance;

    /**
     * The current data listener for registering objects dynamically.
     */
    private final DynamicRegistryListener registryListener;
    /**
     * Gets the current data storage of a server.
     */
    private final Function<MinecraftServer, DynamicRegistryData> dataGetter;

    /**
     * A network channel for sending data across the network.
     */
    private SimpleChannel channel;
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
        this.dataGetter = server -> Objects.requireNonNull(server.getLevel(World.OVERWORLD), "The Overworld is currently null, make sure you are not calling this ").getDataStorage().computeIfAbsent(DynamicRegistryData::new, ID);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus(),
                forgeBus = MinecraftForge.EVENT_BUS;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> new DynamicRegistriesClient(modBus, forgeBus));

        modBus.addListener(this::setupRegistries);
        forgeBus.addListener(this::attachDataStorage);
        forgeBus.addListener(this::addReloadListener);
        forgeBus.addListener(this::serverTick);
        forgeBus.addListener(this::serverStopped);

        this.injectNetworkFilter();
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
        return Objects.requireNonNull(this.channel, "Attempted to get channel before initialization during common setup");
    }

    /**
     * Invalidates any cache held by this mod to update information on their
     * respective sides.
     */
    public void invalidate() {
        this.invalidateCache = true;
    }

    /**
     * Injects the dynamic registry packet splitter into the available splitters.
     */
    private void injectNetworkFilter() {
        ImmutableMap.Builder<String, Function<NetworkManager, VanillaPacketFilter>> filters = ImmutableMap.builder();
        filters.putAll(NetworkFilterAccess.getFilterInstances());
        filters.put(ID + ":dynamic_registry_splitter", DynamicRegistryNetworkFilter::new);
        NetworkFilterAccess.setFilterInstances(filters.build());
    }

    /**
     * Sets up the network packets and the static registry instances.
     *
     * @param event the event instance
     */
    private void setupRegistries(final FMLCommonSetupEvent event) {
        final Predicate<String> networkVersionCheck = version -> {
            final Matcher versionMatcher = PROTOCOL_VERSIONS.matcher(version),
                    protocolMatcher = PROTOCOL_VERSIONS.matcher(NETWORK_PROTOCOL_VERSION);
            if (!(versionMatcher.matches() && protocolMatcher.matches())) return false;
            return protocolMatcher.group(1).equals(versionMatcher.group(1))
                    && Integer.valueOf(protocolMatcher.group(2)).equals(Integer.valueOf(versionMatcher.group(2)))
                    && version.contains("p") || Integer.valueOf(protocolMatcher.group(3)).equals(Integer.valueOf(versionMatcher.group(3)));
        };
        this.channel = NetworkRegistry.newSimpleChannel(NETWORK_ID, () -> NETWORK_PROTOCOL_VERSION, networkVersionCheck, networkVersionCheck);
        this.channel.messageBuilder(DynamicRegistryPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DynamicRegistryPacket::encode)
                .decoder(DynamicRegistryPacket::new)
                .consumer(DynamicRegistryPacket::handle)
                .add();


        LOGGER.debug(MarkerManager.getMarker("New Registry"), "Creating new registries");
        ModLoader.get().postEvent(new DynamicRegistryEvent.NewRegistry());
        DynamicRegistryManager.STATIC.registries(DynamicRegistryManager.Lookup.ALL).forEach(entry -> {
            LOGGER.debug(IRegistrableDynamicRegistry.REGISTER, "Registering entries to {}", entry.getKey());
            final DynamicRegistry<?, ?> registry = entry.getValue();
            registry.unlock();
            ModLoader.get().postEvent(new DynamicRegistryEvent.Register<>(registry));
            registry.lock();
        });
    }

    /**
     * Attach the data storage of the dynamic registries to the Overworld.
     *
     * @param event the event instance
     */
    private void attachDataStorage(final FMLServerStartingEvent event) {
        LOGGER.debug(MarkerManager.getMarker("Data"), "Initializing world saved data for dynamic registries.");
        this.dataGetter.apply(event.getServer());
    }

    /**
     * When the server has stopped, all data from the registries are cleared to
     * prevent any cross world registry data.
     *
     * @param event the event instance
     */
    private void serverStopped(final FMLServerStoppedEvent event) {
        DynamicRegistryManager.DYNAMIC.registries(DynamicRegistryManager.Lookup.ALL).forEach(entry -> entry.getValue().clear());
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
