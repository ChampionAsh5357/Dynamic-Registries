package net.ashwork.dynamicregistries.forge.registry;

import net.ashwork.dynamicregistries.core.registry.IRetrievalRegistry;
import net.ashwork.dynamicregistries.forge.entry.IWrappedRegistryEntry;
import net.ashwork.dynamicregistries.forge.util.WrappedResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;

//TODO: Document
public final class RetrievalRegistry<V extends IWrappedRegistryEntry<V>> implements IRetrievalRegistry<WrappedResourceLocation, V> {

    public static <X extends IWrappedRegistryEntry<X>> RetrievalRegistry<X> wrap(IForgeRegistry<X> registry) {
        return new RetrievalRegistry<>(registry);
    }

    private final IForgeRegistry<V> registry;

    private RetrievalRegistry(IForgeRegistry<V> registry) {
        this.registry = registry;
    }

    @Override
    public WrappedResourceLocation getName() {
        return WrappedResourceLocation.create(this.registry.getRegistryName());
    }

    @Override
    public Class<V> getSuperType() {
        return this.registry.getRegistrySuperType();
    }

    @Nullable
    @Override
    public V getValue(WrappedResourceLocation key) {
        return this.registry.getValue(key.getVal());
    }
}
