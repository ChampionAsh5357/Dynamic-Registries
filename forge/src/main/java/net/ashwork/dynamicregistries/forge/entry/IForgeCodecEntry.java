package net.ashwork.dynamicregistries.forge.entry;

import net.ashwork.dynamicregistries.core.entry.ICodecEntry;
import net.ashwork.dynamicregistries.forge.util.WrappedResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IForgeCodecEntry<T, C> extends ICodecEntry<WrappedResourceLocation, T, C>, IWrappedRegistryEntry<C> {
}
