package net.ashwork.dynamicregistries.forge.registry;

import net.ashwork.dynamicregistries.core.registry.IDynamicRegistry;
import net.ashwork.dynamicregistries.forge.entry.IForgeCodecEntry;
import net.ashwork.dynamicregistries.forge.entry.IForgeDynamicEntry;
import net.ashwork.dynamicregistries.forge.util.WrappedResourceLocation;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface IForgeDynamicRegistry<V extends IForgeDynamicEntry<V>, C extends IForgeCodecEntry<V, C>> extends IDynamicRegistry<WrappedResourceLocation, V, C> {

    default boolean containsKey(ResourceLocation loc) {
        return this.containsKey(WrappedResourceLocation.create(loc));
    }

    @Nullable
    default V getValue(ResourceLocation loc) {
        return this.getValue(WrappedResourceLocation.create(loc));
    }

    default Optional<V> getValueOptional(ResourceLocation loc) {
        return this.getValueOptional(WrappedResourceLocation.create(loc));
    }

    @Nullable
    default ResourceLocation getLocationKey(V value) {
        return this.getLocationKeyOptional(value).orElse(null);
    }

    default Optional<ResourceLocation> getLocationKeyOptional(V value) {
        return this.getKeyOptional(value).map(WrappedResourceLocation::getVal);
    }

    @Nonnull
    default Set<ResourceLocation> locationKeySet() {
        return this.keySet().stream().map(WrappedResourceLocation::getVal).collect(Collectors.toUnmodifiableSet());
    }

    @Nonnull
    default Set<Map.Entry<ResourceLocation, V>> locationEntrySet() {
        return Collections.unmodifiableSet(this.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getVal(), Map.Entry::getValue)).entrySet());
    }
}
