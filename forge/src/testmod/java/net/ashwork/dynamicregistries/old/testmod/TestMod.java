/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old.testmod;

import net.ashwork.dynamicregistries.old.event.DynamicRegistryEvent;
import net.ashwork.dynamicregistries.old.registry.DynamicRegistryBuilder;
import net.ashwork.dynamicregistries.old.testmod.entry.TestObject;
import net.ashwork.dynamicregistries.old.testmod.registry.TestObjectRegistrar;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The base class for the test mod of dynamic registries.
 */
@Mod(TestMod.ID)
public final class TestMod {

    /**
     * A logger for logging information in the test mod.
     */
    public static final Logger LOGGER = LogManager.getLogger("Test Mod");

    /**
     * The id of the test mod.
     */
    public static final String ID = "dynamicregistries_testmod";

    /**
     * The main constructor for setting up the test mod.
     */
    public TestMod() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        TestObjectRegistrar.attach(modBus);

        modBus.addListener(this::dynamicRegistries);
    }

    /**
     * Creates a new dynamic registry.
     *
     * @param event the event instance
     */
    private void dynamicRegistries(final DynamicRegistryEvent.NewRegistry event) {
        new DynamicRegistryBuilder<>(new ResourceLocation(ID, "test_object"), TestObject.class, TestObjectRegistrar.registry())
        .create();
    }
}
