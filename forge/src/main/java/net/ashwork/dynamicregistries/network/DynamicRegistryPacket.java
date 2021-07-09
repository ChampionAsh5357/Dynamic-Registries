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

//TODO: Document and implement
public class DynamicRegistryPacket {

    private final String stage;
    private final Map<ResourceLocation, CompoundNBT> snapshots;

    public DynamicRegistryPacket(final String stage, final Map<ResourceLocation, CompoundNBT> snapshots) {
        this.stage = stage;
        this.snapshots = snapshots;
    }

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

    //TODO: Check how to do partial packets
    public void encode(final PacketBuffer buffer) {
        buffer.writeUtf(this.stage);
        buffer.writeInt(this.snapshots.size());
        this.snapshots.forEach((name, snapshot) -> {
            buffer.writeResourceLocation(name);
            buffer.writeNbt(snapshot);
        });
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        DynamicRegistriesClient.instance().handleClientRegistry(this.stage, this.snapshots)
                )
        );
        return true;
    }
}
