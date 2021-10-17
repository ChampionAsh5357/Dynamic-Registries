/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old;

import net.ashwork.dynamicregistries.old.registry.ISnapshotDynamicRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;

/**
 * A storage container to store persistent registry data. Since this will
 * persist across all dimensions, the data is stored on the Overworld.
 */
public class DynamicRegistryData extends SavedData {

    /**
     * A marker that represents all logging information while saving and loading data.
     */
    private static final Marker SAVED_DATA = MarkerManager.getMarker("Saved Data");

    /**
     * Creates the data from the associated tag.
     *
     * @param tag the encoded data
     * @return an instance of {@link DynamicRegistryData} with the decoded data
     */
    public static DynamicRegistryData fromTag(final CompoundTag tag) {
        return new DynamicRegistryData().load(tag);
    }

    /**
     * Constructs the instance of this storage container.
     */
    public DynamicRegistryData() {
        super();
    }

    /**
     * Decodes a tag to this data instance.
     *
     * @param tag the encoded data
     * @return the current instance
     */
    private DynamicRegistryData load(final CompoundTag tag) {
        DynamicRegistries.LOGGER.debug(SAVED_DATA, "Loading data from {} to registries", tag);
        tag.getAllKeys().forEach(name ->
                DynamicRegistryManager.DYNAMIC.getRegistry(DynamicRegistryManager.DYNAMIC.updateLegacyName(new ResourceLocation(name)))
                        .fromSnapshot(tag.get(name), NbtOps.INSTANCE, true)
        );
        return this;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        DynamicRegistries.LOGGER.debug(SAVED_DATA, "Saving data to {} from registries", tag);
        DynamicRegistryManager.DYNAMIC.registries(DynamicRegistryManager.Lookup.SAVE).forEach(entry -> {
            @Nullable Tag encodedRegistry = entry.getValue().toSnapshot(NbtOps.INSTANCE);
            if (encodedRegistry != null) tag.put(entry.getKey().toString(), encodedRegistry);
            else DynamicRegistries.LOGGER.error(ISnapshotDynamicRegistry.SNAPSHOT, "Registry {} has thrown an error while encoding, skip saving", entry.getKey());
        });
        return tag;
    }
}
