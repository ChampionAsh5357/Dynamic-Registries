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
import net.ashwork.dynamicregistries.registry.DynamicRegistryBuilder;
import net.ashwork.dynamicregistries.registry.IRegistrableDynamicRegistry;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.event.IModBusEvent;

import java.lang.reflect.Type;

/**
 * A superclass of all dynamic registry events.
 *
 * @param <V> the super type of the dynamic registry entry
 * @param <C> the super type of the codec registry entry
 */
public abstract class DynamicRegistryEvent<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> extends GenericEvent<V> implements IModBusEvent {

    /**
     * The super type of the codec registry entry.
     */
    private final Class<C> codecClass;

    /**
     * Constructs an instance of the registry event. Should use one of the available
     * subclasses.
     *
     * @param entryClass the super type of the dynamic registry entry
     * @param codecClass the super type of the codec registry entry
     */
    protected DynamicRegistryEvent(final Class<V> entryClass, final Class<C> codecClass) {
        super(entryClass);
        this.codecClass = codecClass;
    }

    /**
     * Returns the super type of the codec registry entry.
     *
     * @return the super type of the codec registry entry
     */
    public Type getSerializerType() {
        return this.codecClass;
    }

    /**
     * New dynamic registries should be registered during this event via {@link DynamicRegistryBuilder}.
     */
    public static class NewRegistry extends Event implements IModBusEvent {
        /**
         * An empty constructor.
         */
        public NewRegistry() {}

        @Override
        public String toString() {
            return "DynamicRegistryEvent$NewRegistry";
        }
    }

    /**
     * Static registry entries should be registered during this event. Dynamic
     * entries can override this value, and no registry order is guaranteed.
     * This should not be used unless adding an value that should be immutable
     * like the defaulted registry entry.
     *
     * @param <V> the super type of the dynamic registry entry
     * @param <C> the super type of the codec registry entry
     */
    public static class Register<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> extends DynamicRegistryEvent<V, C> {

        /**
         * The registrable dynamic registry.
         */
        private final IRegistrableDynamicRegistry<V, C> registry;

        /**
         * Constructs a register event via the dynamic registry.
         *
         * @param registry the registrable dynamic registry
         */
        public Register(final IRegistrableDynamicRegistry<V, C> registry) {
            super(registry.getEntrySuperType(), registry.getCodecSuperType());
            this.registry = registry;
        }

        /**
         * Returns the registrable dynamic registry.
         *
         * @return the registrable dynamic registry
         */
        public IRegistrableDynamicRegistry<V, C> getRegistry() {
            return this.registry;
        }

        @Override
        public String toString() {
            return "DynamicRegistryEvent$Register<" + this.getRegistry().getName() + ">";
        }
    }
}
