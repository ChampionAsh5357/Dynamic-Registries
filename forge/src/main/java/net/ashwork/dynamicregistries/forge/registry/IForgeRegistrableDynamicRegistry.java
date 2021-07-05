package net.ashwork.dynamicregistries.forge.registry;

import net.ashwork.dynamicregistries.core.registry.IRegistrableDynamicRegistry;
import net.ashwork.dynamicregistries.forge.entry.IForgeCodecEntry;
import net.ashwork.dynamicregistries.forge.entry.IForgeDynamicEntry;
import net.ashwork.dynamicregistries.forge.util.WrappedResourceLocation;

public interface IForgeRegistrableDynamicRegistry<V extends IForgeDynamicEntry<V>, C extends IForgeCodecEntry<V, C>> extends IRegistrableDynamicRegistry<WrappedResourceLocation, V, C> {


}
