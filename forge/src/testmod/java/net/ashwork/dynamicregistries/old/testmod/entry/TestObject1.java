/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old.testmod.entry;

import net.ashwork.dynamicregistries.old.entry.DynamicEntry;
import net.ashwork.dynamicregistries.old.entry.ICodecEntry;
import net.ashwork.dynamicregistries.old.testmod.TestMod;
import net.ashwork.dynamicregistries.old.testmod.registry.TestObjectRegistrar;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.MarkerManager;

/**
 * The first implementation of the generic test class.
 */
public class TestObject1 extends DynamicEntry<TestObject> implements TestObject {

    /**
     * A generic test primitive value.
     */
    private final int testPrimitive;
    /**
     * A generic test object value.
     */
    private final ResourceLocation testObject;

    /**
     * A constructor instance.
     *
     * @param testPrimitive the test primitive
     * @param testObject the test object
     */
    public TestObject1(final int testPrimitive, final ResourceLocation testObject) {
        this.testPrimitive = testPrimitive;
        this.testObject = testObject;
    }

    @Override
    public void executeThings() {
        TestMod.LOGGER.info(MarkerManager.getMarker("Test Object 1"), "Executed: {}, {}", this.testPrimitive, this.testObject);
    }

    @Override
    public int getTestPrimitive() {
        return this.testPrimitive;
    }

    @Override
    public ResourceLocation getTestObject() {
        return this.testObject;
    }

    @Override
    public ICodecEntry<? extends TestObject, ?> codec() {
        return TestObjectRegistrar.TEST_OBJECT_1.get();
    }
}
