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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Map;

/**
 * A reload listener used for handling registry load within the dynamic registry.
 */
public class DynamicRegistryListener extends SimpleJsonResourceReloadListener {

    /**
     * A marker that represents all logging information while reloading.
     */
    private static final Marker RELOAD_LISTENER = MarkerManager.getMarker("Reload Listener");

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
    protected void apply(final Map<ResourceLocation, JsonElement> entries, final ResourceManager manager, final ProfilerFiller profiler) {
        DynamicRegistries.LOGGER.debug(RELOAD_LISTENER, "Reloading registries with {}", entries);
        profiler.push("dynamic_registries_reload");
        DynamicRegistryManager.DYNAMIC.reload(entries, JsonOps.INSTANCE, DynamicRegistryManager.STATIC);
        DynamicRegistries.instance().invalidate();
        profiler.pop();
    }
}
