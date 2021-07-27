/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.testmod.entry;

import net.ashwork.dynamicregistries.entry.DynamicEntry;
import net.ashwork.dynamicregistries.entry.ICodecEntry;
import net.ashwork.dynamicregistries.testmod.TestMod;
import net.ashwork.dynamicregistries.testmod.registry.TestObjectRegistrar;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.MarkerManager;

/**
 * The second implementation of the generic test class.
 */
public class TestObject2 extends DynamicEntry<TestObject> implements TestObject {

    /**
     * A generic test primitive value.
     */
    private final int testPrimitive;
    /**
     * A generic test object value.
     */
    private final ResourceLocation testObject;
    /**
     * Another generic test primitive value.
     */
    private final double extendedTestPrimitive;
    /**
     * Another generic test object value.
     */
    private final String extendedTestObject;

    /**
     * A constructor instance.
     *
     * @param testPrimitive the test primitive
     * @param testObject the test object
     * @param extendedTestPrimitive another test primitive
     * @param extendedTestObject another test object
     */
    public TestObject2(final int testPrimitive, final ResourceLocation testObject, final double extendedTestPrimitive, final String extendedTestObject) {
        this.testPrimitive = testPrimitive;
        this.testObject = testObject;
        this.extendedTestPrimitive = extendedTestPrimitive;
        this.extendedTestObject = extendedTestObject;
    }

    @Override
    public void executeThings() {
        TestMod.LOGGER.info(MarkerManager.getMarker("Test Object 2"), "Executed: {} -> {}, {} -> {}", this.testPrimitive, this.extendedTestPrimitive, this.testObject, this.extendedTestObject);
    }

    @Override
    public int getTestPrimitive() {
        return this.testPrimitive;
    }

    @Override
    public ResourceLocation getTestObject() {
        return this.testObject;
    }

    /**
     * Gets another test primitive.
     *
     * @return the test primitive
     */
    public double getExtendedTestPrimitive() {
        return this.extendedTestPrimitive;
    }

    /**
     * Gets another test object.
     *
     * @return the test object
     */
    public String getExtendedTestObject() {
        return this.extendedTestObject;
    }

    @Override
    public ICodecEntry<? extends TestObject, ?> codec() {
        return TestObjectRegistrar.TEST_OBJECT_2.get();
    }
}
