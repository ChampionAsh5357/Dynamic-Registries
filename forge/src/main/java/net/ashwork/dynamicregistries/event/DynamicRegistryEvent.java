/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.event;

import net.ashwork.dynamicregistries.entry.ICodecEntry;
import net.ashwork.dynamicregistries.entry.IDynamicEntry;
import net.ashwork.dynamicregistries.registry.IRegistrableDynamicRegistry;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

import java.lang.reflect.Type;

//TODO: Document and implement
public abstract class DynamicRegistryEvent<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> extends GenericEvent<V> implements IModBusEvent {

    private final Class<C> codecClass;

    protected DynamicRegistryEvent(final Class<V> entryClass, final Class<C> codecClass) {
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

    public static class Register<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> extends DynamicRegistryEvent<V, C> {

        private final IRegistrableDynamicRegistry<V, C> registry;

        public Register(final IRegistrableDynamicRegistry<V, C> registry) {
            super(registry.getEntrySuperType(), registry.getCodecSuperType());
            this.registry = registry;
        }

        public IRegistrableDynamicRegistry<V, C> getRegistry() {
            return this.registry;
        }

        @Override
        public String toString() {
            return "DynamicRegistryEvent$Register<" + this.getRegistry().getName() + ">";
        }
    }
}
