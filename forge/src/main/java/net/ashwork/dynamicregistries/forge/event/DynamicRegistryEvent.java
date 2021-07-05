package net.ashwork.dynamicregistries.forge.event;

import net.ashwork.dynamicregistries.forge.entry.IForgeCodecEntry;
import net.ashwork.dynamicregistries.forge.entry.IForgeDynamicEntry;
import net.ashwork.dynamicregistries.forge.registry.IForgeDynamicRegistry;
import net.ashwork.dynamicregistries.forge.registry.IForgeRegistrableDynamicRegistry;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

import java.lang.reflect.Type;

// TODO: Document
public abstract class DynamicRegistryEvent<V extends IForgeDynamicEntry<V>, C extends IForgeCodecEntry<V, C>> extends GenericEvent<V> implements IModBusEvent {

    private final Class<C> codecClass;

    protected DynamicRegistryEvent(Class<V> entryClass, Class<C> codecClass) {
        super(entryClass);
        this.codecClass = codecClass;
    }

    public Type getSerializerType() {
        return this.codecClass;
    }

    public static class NewRegistry extends Event implements IModBusEvent {
        public NewRegistry() {}

        @Override
        public String toString() {
            return "DynamicRegistryEvent$NewRegistry";
        }
    }

    public static class Register<V extends IForgeDynamicEntry<V>, C extends IForgeCodecEntry<V, C>> extends DynamicRegistryEvent<V, C> {

        private final IForgeRegistrableDynamicRegistry<V, C> registry;

        public Register(IForgeRegistrableDynamicRegistry<V, C> registry) {
            super(registry.getEntrySuperType(), registry.getCodecRegistrySuperType());
            this.registry = registry;
        }

        public IForgeRegistrableDynamicRegistry<V, C> getRegistry() {
            return this.registry;
        }

        @Override
        public String toString() {
            return "DynamicRegistryEvent$Register<" + this.getRegistry().getName() + ">";
        }
    }
}
