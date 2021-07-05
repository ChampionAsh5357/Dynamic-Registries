package net.ashwork.dynamicregistries.forge.registry;

import net.ashwork.dynamicregistries.core.registry.AbstractDynamicRegistryBuilder;
import net.ashwork.dynamicregistries.core.registry.IDynamicRegistry;
import net.ashwork.dynamicregistries.forge.ForgeDynamicRegistryManager;
import net.ashwork.dynamicregistries.forge.entry.IForgeCodecEntry;
import net.ashwork.dynamicregistries.forge.entry.IForgeDynamicEntry;
import net.ashwork.dynamicregistries.forge.util.WrappedResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

// TODO: Document
public class ForgeDynamicRegistryBuilder<V extends IForgeDynamicEntry<V>, C extends IForgeCodecEntry<V, C>> extends AbstractDynamicRegistryBuilder<WrappedResourceLocation, V, C, ForgeDynamicRegistry<V, C>, ForgeDynamicRegistryBuilder<V, C>> {

    public static <T extends IForgeDynamicEntry<T>, S extends IForgeCodecEntry<T, S>> ForgeDynamicRegistryBuilder<T, S> create(String name, Class<T> superType, IForgeRegistry<S> codecRegistry) {
        return new ForgeDynamicRegistryBuilder<>(name, superType, codecRegistry);
    }

    protected ForgeDynamicRegistryBuilder(String name, Class<V> superType, IForgeRegistry<C> codecRegistry) {
        super(name, superType, RetrievalRegistry.wrap(codecRegistry));
    }

    @Override
    protected ForgeDynamicRegistryBuilder<V, C> ret() {
        return this;
    }

    public ForgeDynamicRegistryBuilder<V, C> setDefaultKey(ResourceLocation defaultKey) {
        return this.setDefaultKey(WrappedResourceLocation.create(defaultKey));
    }

    @Override
    public IDynamicRegistry<WrappedResourceLocation, V, C> create() {
        return ForgeDynamicRegistryManager.STATIC.createRegistry(this);
    }
}
