package net.ashwork.dynamicregistries.forge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.ashwork.dynamicregistries.forge.util.WrappedResourceLocation;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.stream.Collectors;

public class ForgeDynamicRegistryListener extends JsonReloadListener {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public ForgeDynamicRegistryListener() {
        super(GSON, "dynamic_registries");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, IResourceManager manager, IProfiler profiler) {
        profiler.push("dynamic_registries");
        ForgeDynamicRegistryManager.DYNAMIC.reload(entries.entrySet().stream()
                .collect(Collectors.toMap(entry -> WrappedResourceLocation.create(entry.getKey()), Map.Entry::getValue)), ForgeDynamicRegistryManager.STATIC);
        profiler.pop();
    }
}
