package net.ashwork.dynamicregistries.forge.registry;

import net.ashwork.dynamicregistries.core.registry.IModifiableDynamicRegistry;
import net.ashwork.dynamicregistries.forge.entry.IForgeCodecEntry;
import net.ashwork.dynamicregistries.forge.entry.IForgeDynamicEntry;
import net.ashwork.dynamicregistries.forge.util.WrappedResourceLocation;
import net.minecraft.util.ResourceLocation;

public interface IForgeModifiableDynamicRegistry<V extends IForgeDynamicEntry<V>, C extends IForgeCodecEntry<V, C>> extends IForgeDynamicRegistry<V, C>, IModifiableDynamicRegistry<WrappedResourceLocation, V, C> {

    default V remove(ResourceLocation loc) {
        return this.remove(WrappedResourceLocation.create(loc));
    }
}
