package net.ashwork.dynamicregistries.forge.entry;

import net.ashwork.dynamicregistries.core.entry.IDynamicEntry;
import net.ashwork.dynamicregistries.forge.util.WrappedResourceLocation;

public interface IForgeDynamicEntry<T> extends IDynamicEntry<WrappedResourceLocation, T>, IWrappedRegistryEntry<T> {
}
