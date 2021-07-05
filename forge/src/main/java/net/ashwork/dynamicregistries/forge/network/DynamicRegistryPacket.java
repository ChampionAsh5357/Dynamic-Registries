package net.ashwork.dynamicregistries.forge.network;

import net.ashwork.dynamicregistries.forge.ForgeDynamicRegistryManager;
import net.ashwork.dynamicregistries.forge.registry.ForgeDynamicRegistry;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class DynamicRegistryPacket {

    private final Map<String, ForgeDynamicRegistry<?, ?>> registries;

    public DynamicRegistryPacket(final Map<String, ForgeDynamicRegistry<?, ?>> registries) {
        this.registries = registries;
    }

    public DynamicRegistryPacket(PacketBuffer buffer) {
        this.registries = null;
        final int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            ForgeDynamicRegistry<?, ?> registry = (ForgeDynamicRegistry<?, ?>) ForgeDynamicRegistryManager.DYNAMIC.getRegistry(buffer.readUtf());
            registry.readRegistry(buffer);
        }
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(this.registries.size());
        this.registries.forEach((name, registry) -> {
            buffer.writeUtf(name);
            registry.writeRegistry(buffer);
        });
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        // TODO: Add eventual context
        return true;
    }
}
