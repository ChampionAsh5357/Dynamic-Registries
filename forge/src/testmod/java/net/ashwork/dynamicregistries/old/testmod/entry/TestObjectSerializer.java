/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old.testmod.entry;

import com.mojang.serialization.Codec;
import net.ashwork.dynamicregistries.old.entry.CodecEntry;

/**
 * A serializer for the {@link TestObject} dynamic registry.
 */
public final class TestObjectSerializer extends CodecEntry.Instance<TestObject, TestObjectSerializer> {

    /**
     * A constructor instance.
     *
     * @param entryCodec the entry codec for the dynamic registry.
     */
    public TestObjectSerializer(Codec<? extends TestObject> entryCodec) {
        super(entryCodec);
    }
}
