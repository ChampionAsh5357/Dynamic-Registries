/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.ashwork.dynamicregistries.forge.util;

import com.mojang.serialization.Codec;
import net.ashwork.dynamicregistries.core.util.WrappedIdentifier;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

//TODO: Document
public final class WrappedResourceLocation extends WrappedIdentifier<ResourceLocation> {

    private static final Map<ResourceLocation, WrappedResourceLocation> CACHE = new HashMap<>();
    public static final Codec<WrappedResourceLocation> CODEC = ResourceLocation.CODEC.xmap(WrappedResourceLocation::create, WrappedResourceLocation::getVal);

    public static WrappedResourceLocation create(ResourceLocation loc) {
        return CACHE.computeIfAbsent(loc, WrappedResourceLocation::new);
    }

    protected WrappedResourceLocation(@Nonnull ResourceLocation val) {
        super(val);
    }
}
