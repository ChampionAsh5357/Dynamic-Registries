/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

/**
 * A reload listener used for handling registry load within the dynamic registry.
 */
public class DynamicRegistryListener extends JsonReloadListener {

    /**
     * A {@link Gson} instance. Only used for transmuting a string to a {@link JsonElement}.
     */
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    /**
     * Constructs the listener under the {@code dynamic_registries} data folder.
     */
    public DynamicRegistryListener() {
        super(GSON, "dynamic_registries");
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> entries, final IResourceManager manager, final IProfiler profiler) {
        profiler.push("dynamic_registries_reload");
        DynamicRegistryManager.DYNAMIC.reload(entries, JsonOps.INSTANCE, DynamicRegistryManager.STATIC);
        DynamicRegistries.instance().invalidate();
        profiler.pop();
    }
}
