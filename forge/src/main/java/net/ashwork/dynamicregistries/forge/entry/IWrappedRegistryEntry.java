package net.ashwork.dynamicregistries.forge.entry;

import net.ashwork.dynamicregistries.core.entry.IEntry;
import net.ashwork.dynamicregistries.forge.util.WrappedResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public interface IWrappedRegistryEntry<V> extends IForgeRegistryEntry<V>, IEntry<WrappedResourceLocation, V> {

    @Override
    default V setEntryName(WrappedResourceLocation name) {
        return this.setRegistryName(name.getVal());
    }

    @Nullable
    @Override
    default WrappedResourceLocation getEntryName() {
        return WrappedResourceLocation.create(this.getRegistryName());
    }

    @Override
    default Class<V> getRegistrySuperType() {
        return this.getRegistryType();
    }
}
