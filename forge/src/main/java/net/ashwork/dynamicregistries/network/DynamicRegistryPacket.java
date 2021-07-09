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
import net.ashwork.dynamicregistries.DynamicRegistries;
import net.ashwork.dynamicregistries.client.DynamicRegistriesClient;
import net.ashwork.dynamicregistries.registry.ISnapshotDynamicRegistry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * A packet that is used to encode registry information from the server
 * to the client.
 */
public class DynamicRegistryPacket {

    /**
     * The stage being synced. Only used for the dynamic stage currently.
     */
    private final String stage;
    /**
     * A map of registry names to their encoded registries
     */
    private final Map<ResourceLocation, CompoundNBT> snapshots;

    /**
     * Constructs the packet on the server.
     *
     * @param stage the registry stage
     * @param snapshots a map of registry names to their encoded registries
     */
    public DynamicRegistryPacket(final String stage, final Map<ResourceLocation, CompoundNBT> snapshots) {
        this.stage = stage;
        this.snapshots = snapshots;
    }

    /**
     * Constructs the packet on the client. Decodes the data from the given {@code buffer}.
     *
     * @param buffer a buffer containing the sent packet information
     */
    public DynamicRegistryPacket(final PacketBuffer buffer) {
        this(buffer.readUtf(), Util.make(() -> {
            final int size = buffer.readInt();
            final ImmutableMap.Builder<ResourceLocation, CompoundNBT> snapshots = ImmutableMap.builder();
            IntStream.range(0, size).forEach(u -> {
                final ResourceLocation name = buffer.readResourceLocation();
                @Nullable CompoundNBT tag = buffer.readAnySizeNbt();
                if (tag != null) snapshots.put(name, tag);
                else DynamicRegistries.LOGGER.error(ISnapshotDynamicRegistry.SNAPSHOT, "Registry snapshot {} returned null, skipping", name);
            });
            return snapshots.build();
        }));
    }

    /**
     * Encodes the data to a {@code buffer} to be sent to the client.
     *
     * @param buffer the buffer to encode the data to
     */
    //TODO: Check how to do partial packets, send login packet for initial syncing
    public void encode(final PacketBuffer buffer) {
        buffer.writeUtf(this.stage);
        buffer.writeInt(this.snapshots.size());
        this.snapshots.forEach((name, snapshot) -> {
            buffer.writeResourceLocation(name);
            buffer.writeNbt(snapshot);
        });
    }

    /**
     * Handles what do to with the data once sent to the client.
     *
     * @param context a supplier containing the network context
     * @return if the packet was handled, should always be {@code true}.
     */
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        DynamicRegistriesClient.instance().handleClientRegistry(this.stage, this.snapshots)
                )
        );
        return true;
    }
}
