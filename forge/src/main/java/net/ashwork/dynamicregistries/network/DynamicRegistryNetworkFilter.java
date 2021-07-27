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
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraftforge.fmllegacy.network.ICustomPacket;
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
     * @param connection the network manager
     */
    public DynamicRegistryNetworkFilter(final @Nullable Connection connection) {
        super(buildHandlers(connection));
    }

    /**
     * Builds the handlers that can split available packets for this mod.
     *
     * @param connection the network manager
     * @return a map of packets to their packet splitter handler
     */
    private static Map<Class<? extends Packet<?>>, BiConsumer<Packet<?>, List<? super Packet<?>>>> buildHandlers(final @Nullable Connection connection)
    {
        VanillaPacketSplitter.RemoteCompatibility compatibility = connection == null ? VanillaPacketSplitter.RemoteCompatibility.ABSENT : VanillaPacketSplitter.getRemoteCompatibility(connection);
        if (compatibility == VanillaPacketSplitter.RemoteCompatibility.ABSENT) return ImmutableMap.of();
        return ImmutableMap.of(ClientboundCustomPayloadPacket.class, DynamicRegistryNetworkFilter::splitPacket);
    }

    @Override
    protected boolean isNecessary(final Connection connection) {
        return !connection.isMemoryConnection() && VanillaPacketSplitter.isRemoteCompatible(connection);
    }

    /**
     * Splits the packets as necessary. Will only try to split if packet is from this mod.
     *
     * @param packet the packet being checked for splitting
     * @param out the list of split packets
     */
    private static void splitPacket(final Packet<?> packet, final List<? super Packet<?>> out)
    {
        if (packet instanceof ICustomPacket<?>
                && ((ICustomPacket<?>) packet).getName().equals(DynamicRegistries.NETWORK_ID))
            VanillaPacketSplitter.appendPackets(ConnectionProtocol.PLAY, PacketFlow.CLIENTBOUND, packet, out);
        else out.add(packet);
    }
}
