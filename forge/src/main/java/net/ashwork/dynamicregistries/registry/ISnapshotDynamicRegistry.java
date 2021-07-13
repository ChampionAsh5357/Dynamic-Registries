/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.ashwork.dynamicregistries.DynamicRegistries;
import net.ashwork.dynamicregistries.DynamicRegistryData;
import net.ashwork.dynamicregistries.entry.ICodecEntry;
import net.ashwork.dynamicregistries.entry.IDynamicEntry;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;

/**
 * A snapshot instance of an {@link IDynamicRegistry}.
 *
 * @param <V> the super type of the dynamic registry entry
 * @param <C> the super type of the codec registry entry
 */
public interface ISnapshotDynamicRegistry<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> extends IDynamicRegistry<V, C> {

    /**
     * A marker that represents all logging information while encoding/decoding snapshots.
     */
    Marker SNAPSHOT = MarkerManager.getMarker("Snapshot");

    /**
     * Returns the codec used to encode/decode the registry from its snapshot.
     *
     * @param isSavedData if the data was populated from {@link DynamicRegistryData}
     * @return  the codec used to encode/decode the registry from its snapshot
     */
    Codec<ISnapshotDynamicRegistry<V, C>> snapshotCodec(final boolean isSavedData);

    /**
     * Encodes a registry snapshot.
     *
     * @param ops the operator used to transmute the encoded object
     * @param <T> the type of the encoded object
     * @return the encoded form of the registry snapshot
     */
    @Nullable
    default <T> T toSnapshot(final DynamicOps<T> ops) {
        return this.snapshotCodec(false).encodeStart(ops, this).resultOrPartial(error ->
                DynamicRegistries.LOGGER.error(SNAPSHOT, "Could not encode a snapshot of {}: {}", this.getName(), error)
        ).orElse(null);
    }

    /**
     * Decodes and implements the registry snapshot.
     *
     * @implNote
     * {@code isSaveData} is used since registry data can be appended or cleared later.
     * Saved data is loaded later than the reload, so we need to make sure not to override
     * the entries.
     *
     * @param input the encoded form of the registry snapshot
     * @param ops the operator used to transmute the encoded object
     * @param isSaveData if the data was populated from {@link DynamicRegistryData}
     * @param <T> the type of the encoded object
     */
    default <T> void fromSnapshot(final T input, final DynamicOps<T> ops, final boolean isSaveData) {
        this.snapshotCodec(isSaveData).parse(ops, input).error().ifPresent(error ->
                DynamicRegistries.LOGGER.error(SNAPSHOT, "Could not decode a snapshot of {}: {}", this.getName(), error)
        );
    }
}
