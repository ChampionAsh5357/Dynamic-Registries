package net.ashwork.dynamicregistries.forge;

import net.ashwork.dynamicregistries.core.AbstractDynamicRegistryManager;
import net.ashwork.dynamicregistries.forge.event.DynamicRegistryEvent;
import net.ashwork.dynamicregistries.forge.network.DynamicRegistryPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Objects;

@Mod(DynamicRegistries.ID)
public final class DynamicRegistries {

    private static final Logger LOGGER = LogManager.getLogger("Dynamic Registries");
    private static final Marker REGISTRIES = MarkerManager.getMarker("Registries");
    public static final String ID = "dynamicregistries";
    private static final String NETWORK_VERSION = "1";
    private static DynamicRegistries instance;

    private final ForgeDynamicRegistryListener dynamicRegistryListener;
    private final SimpleChannel channel;

    public DynamicRegistries() {
        instance = this;
        this.dynamicRegistryListener = new ForgeDynamicRegistryListener();
        this.channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(ID, "network"), () -> NETWORK_VERSION, (str) -> Objects.equals(str, NETWORK_VERSION), (str) -> Objects.equals(str, NETWORK_VERSION));

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus(),
                forgeBus = MinecraftForge.EVENT_BUS;
        modBus.addListener(this::setupRegistries);
        forgeBus.addListener(this::addReloadListener);
        forgeBus.addListener(this::serverTickEvent);
    }

    public static DynamicRegistries instance() {
        return instance;
    }

    public SimpleChannel getChannel() {
        return this.channel;
    }

    private void setupRegistries(final FMLCommonSetupEvent event) {
        this.channel.messageBuilder(DynamicRegistryPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DynamicRegistryPacket::encode)
                .decoder(DynamicRegistryPacket::new)
                .consumer(DynamicRegistryPacket::handle)
                .add();
        event.enqueueWork(() -> {
            LOGGER.debug(MarkerManager.getMarker("New Registry"), "Adding new registries");
            ModLoader.get().postEvent(new DynamicRegistryEvent.NewRegistry());
            ForgeDynamicRegistryManager.STATIC.filteredForgeRegistries(AbstractDynamicRegistryManager.Lookup.ALL).forEach((name, registry) -> {
                LOGGER.debug(REGISTRIES, "Registering entries to " + name);
                registry.unlock();
                ModLoader.get().postEvent(new DynamicRegistryEvent.Register<>(registry));
                registry.lock();
            });
        });
    }

    private void serverTickEvent(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        if (ForgeDynamicRegistryManager.DYNAMIC.needsSync()) {
            ForgeDynamicRegistryManager.DYNAMIC.sync();
        }
    }

    private void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(this.dynamicRegistryListener);
    }

    //TODO: Write Test Case
    //TODO: Write/read to save
    //TODO: Create dynamic model hook
}
