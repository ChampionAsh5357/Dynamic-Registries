/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.network;

import com.google.common.collect.ImmutableMap;
import io.netty.channel.ChannelHandler;
import net.ashwork.dynamicregistries.DynamicRegistries;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraftforge.fml.network.ICustomPacket;
import net.minecraftforge.network.VanillaPacketFilter;
import net.minecraftforge.network.VanillaPacketSplitter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A network filter used to split registry packets sent to the client if necessary.
 */
@ChannelHandler.Sharable
public class DynamicRegistryNetworkFilter extends VanillaPacketFilter {

    /**
     * A constructor instance.
     *
     * @param manager the network manager
     */
    public DynamicRegistryNetworkFilter(final @Nullable NetworkManager manager) {
        super(buildHandlers(manager));
    }

    /**
     * Builds the handlers that can split available packets for this mod.
     *
     * @param manager the network manager
     * @return a map of packets to their packet splitter handler
     */
    private static Map<Class<? extends IPacket<?>>, BiConsumer<IPacket<?>, List<? super IPacket<?>>>> buildHandlers(final @Nullable NetworkManager manager)
    {
        VanillaPacketSplitter.RemoteCompatibility compatibility = manager == null ? VanillaPacketSplitter.RemoteCompatibility.ABSENT : VanillaPacketSplitter.getRemoteCompatibility(manager);
        if (compatibility == VanillaPacketSplitter.RemoteCompatibility.ABSENT) return ImmutableMap.of();
        return ImmutableMap.of(SCustomPayloadPlayPacket.class, DynamicRegistryNetworkFilter::splitPacket);
    }

    @Override
    protected boolean isNecessary(final NetworkManager manager) {
        return !manager.isMemoryConnection() && VanillaPacketSplitter.isRemoteCompatible(manager);
    }

    /**
     * Splits the packets as necessary. Will only try to split if packet is from this mod.
     *
     * @param packet the packet being checked for splitting
     * @param out the list of split packets
     */
    private static void splitPacket(final IPacket<?> packet, final List<? super IPacket<?>> out)
    {
        if (packet instanceof ICustomPacket<?>
                && ((ICustomPacket<?>) packet).getName().equals(DynamicRegistries.NETWORK_ID))
            VanillaPacketSplitter.appendPackets(ProtocolType.PLAY, PacketDirection.CLIENTBOUND, packet, out);
        else out.add(packet);
    }
}
