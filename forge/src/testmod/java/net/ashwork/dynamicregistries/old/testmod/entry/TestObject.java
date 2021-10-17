/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old.testmod.entry;

import net.ashwork.dynamicregistries.old.entry.IDynamicEntry;
import net.minecraft.resources.ResourceLocation;

/**
 * A generic test class.
 */
public interface TestObject extends IDynamicEntry<TestObject> {

    /**
     * Executes a logging message.
     */
    void executeThings();

    /**
     * Gets a test primitive.
     *
     * @return the test primitive
     */
    int getTestPrimitive();

    /**
     * Gets a test object.
     *
     * @return the test object
     */
    ResourceLocation getTestObject();
}
