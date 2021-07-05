package net.ashwork.dynamicregistries.forge.entry;

import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class WrappedRegistryEntry<V extends IWrappedRegistryEntry<V>> extends ForgeRegistryEntry.UncheckedRegistryEntry<V> implements IWrappedRegistryEntry<V> {

    public abstract static class Dynamic<V extends IForgeDynamicEntry<V>> extends WrappedRegistryEntry<V> implements IForgeDynamicEntry<V> {}

    public abstract static class Codec<V, C extends IForgeCodecEntry<V, C>> extends WrappedRegistryEntry<C> implements IForgeCodecEntry<V, C> {}
}
