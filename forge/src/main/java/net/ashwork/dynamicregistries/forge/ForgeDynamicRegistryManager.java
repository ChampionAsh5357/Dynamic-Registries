package net.ashwork.dynamicregistries.forge;

import com.google.gson.JsonElement;
import net.ashwork.dynamicregistries.core.AbstractDynamicRegistryManager;
import net.ashwork.dynamicregistries.core.registry.AbstractDynamicRegistryBuilder;
import net.ashwork.dynamicregistries.forge.network.DynamicRegistryPacket;
import net.ashwork.dynamicregistries.forge.registry.ForgeDynamicRegistry;
import net.ashwork.dynamicregistries.forge.registry.ForgeDynamicRegistryBuilder;
import net.ashwork.dynamicregistries.forge.util.WrappedResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Map;
import java.util.stream.Collectors;

//TODO: Document
public class ForgeDynamicRegistryManager extends AbstractDynamicRegistryManager<WrappedResourceLocation, ForgeDynamicRegistryManager> {

    public static final ForgeDynamicRegistryManager STATIC = new ForgeDynamicRegistryManager("STATIC");
    public static final ForgeDynamicRegistryManager DYNAMIC = new ForgeDynamicRegistryManager("DYNAMIC");

    protected ForgeDynamicRegistryManager(String stage) {
        super(stage);
    }

    @Override
    protected ForgeDynamicRegistry<?, ?> constructRegistry(AbstractDynamicRegistryBuilder<WrappedResourceLocation, ?, ?, ?, ?> builder) {
        return ForgeDynamicRegistry.create(this, this.castBuilder(builder));
    }

    @Override
    protected void handleRegistries(Map<WrappedResourceLocation, JsonElement> entries) {
        entries.forEach((id, element) -> {
            String[] paths = id.getVal().getPath().split("/", 2);
            this.getRegistry(paths[0]).register(WrappedResourceLocation.create(new ResourceLocation(id.getVal().getNamespace(), paths[1])), element);
        });
    }

    @Override
    protected void sync() {
        DynamicRegistries.instance().getChannel().send(PacketDistributor.ALL.noArg(),
                new DynamicRegistryPacket(this.filteredForgeRegistries(Lookup.SYNC)));
        super.sync();
    }

    private ForgeDynamicRegistryBuilder<?, ?> castBuilder(AbstractDynamicRegistryBuilder<WrappedResourceLocation, ?, ?, ?, ?> builder) {
        return (ForgeDynamicRegistryBuilder<?, ?>) builder;
    }

    public Map<String,ForgeDynamicRegistry<?, ?>> filteredForgeRegistries(Lookup lookup) {
        return this.filteredRegistries(lookup).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> (ForgeDynamicRegistry<?, ?>) entry.getValue()));
    }
}
