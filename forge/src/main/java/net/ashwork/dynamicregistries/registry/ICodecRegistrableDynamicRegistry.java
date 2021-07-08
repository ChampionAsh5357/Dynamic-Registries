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
import net.ashwork.dynamicregistries.entry.ICodecEntry;
import net.ashwork.dynamicregistries.entry.IDynamicEntry;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

/**
 * A registrable instance of a dynamic registry that can encode/decode data
 * from a codec.
 *
 * @param <V> the super type of the dynamic registry entry
 * @param <C> the super type of the codec registry entry
 */
public interface ICodecRegistrableDynamicRegistry<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> extends IRegistrableDynamicRegistry<V, C> {

    /**
     * Returns the codec used to encode/decode the registry object from its
     * exploded form rather than its registry name.
     *
     * @return the codec used to encode/decode the registry object from its
     *         exploded form
     */
    Codec<V> entryCodec();

    /**
     * Registers a registry object from its encoded form to the registry.
     *
     * @param key the identifier of the registry object
     * @param value the encoded form of the registry object
     * @param ops the operator used to transmute the encoded object
     * @param <T> the type of the encoded object
     */
    default <T> void register(final ResourceLocation key, final T value, final DynamicOps<T> ops) {
        this.entryCodec().parse(ops, value).resultOrPartial(error ->
                DynamicRegistries.LOGGER.error(IRegistrableDynamicRegistry.REGISTER, "{} could not be decoded from {} within {}: {}", key, value, this.getName(), error))
        .ifPresent(registryObject -> {
            registryObject.setRegistryName(key);
            this.register(registryObject);
        });
    }

    /**
     * Registers all registry objects from their encoded forms to the registry.
     *
     * @param entries a map of identifiers to encoded registry objects
     * @param ops the operator used to transmute the encoded object
     * @param <T> the type of the encoded object
     */
    default <T> void registerAll(final Map<ResourceLocation, T> entries, final DynamicOps<T> ops) {
        entries.forEach((key, value) -> this.register(key, value, ops));
    }
}
