package net.ashwork.dynamicregistries.forge.registry;

import com.mojang.serialization.Codec;
import net.ashwork.dynamicregistries.core.AbstractDynamicRegistryManager;
import net.ashwork.dynamicregistries.core.registry.AbstractDynamicRegistry;
import net.ashwork.dynamicregistries.core.registry.AbstractDynamicRegistryBuilder;
import net.ashwork.dynamicregistries.forge.entry.IForgeCodecEntry;
import net.ashwork.dynamicregistries.forge.entry.IForgeDynamicEntry;
import net.ashwork.dynamicregistries.forge.util.WrappedResourceLocation;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

//TODO: Document
public class ForgeDynamicRegistry<V extends IForgeDynamicEntry<V>, C extends IForgeCodecEntry<V, C>> extends AbstractDynamicRegistry<WrappedResourceLocation, V, C, ForgeDynamicRegistry<V, C>> implements IForgeDynamicRegistry<V, C>, IForgeRegistrableDynamicRegistry<V, C>, IForgeModifiableDynamicRegistry<V, C> {

    public static <T extends IForgeDynamicEntry<T>, S extends IForgeCodecEntry<T, S>> ForgeDynamicRegistry<T, S> create(AbstractDynamicRegistryManager<WrappedResourceLocation, ?> stage, AbstractDynamicRegistryBuilder<WrappedResourceLocation, T, S, ForgeDynamicRegistry<T, S>, ?> builder) {
        return new ForgeDynamicRegistry<>(stage, builder.getName(), builder);
    }

    private ForgeDynamicRegistry(AbstractDynamicRegistryManager<WrappedResourceLocation, ?> stage, String name, AbstractDynamicRegistryBuilder<WrappedResourceLocation, V, C, ForgeDynamicRegistry<V, C>, ?> builder) {
        super(stage, name, builder);
    }

    @Override
    protected ForgeDynamicRegistry<V, C> ret() {
        return this;
    }

    public void writeRegistry(PacketBuffer buffer) {
        try {
            buffer.writeWithCodec(this.registryCodec, this);
        } catch (IOException e) {
            throw new RuntimeException("An IO write exception has occurred within " + this.getName(), e);
        }
    }

    public void readRegistry(PacketBuffer buffer) {
        try {
            buffer.readWithCodec(this.registryCodec);
        } catch (IOException e) {
            throw new RuntimeException("An IO write exception has occurred within " + this.getName(), e);
        }
    }

    @Override
    protected Codec<WrappedResourceLocation> identifierCodec() {
        return WrappedResourceLocation.CODEC;
    }

    @Override
    public ForgeDynamicRegistry<V, C> copy(AbstractDynamicRegistryManager<WrappedResourceLocation, ?> stage) {
        return new ForgeDynamicRegistry<>(stage, this.name, this.builder);
    }
}
