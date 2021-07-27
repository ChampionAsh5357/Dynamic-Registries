/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.testmod.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ashwork.dynamicregistries.testmod.TestMod;
import net.ashwork.dynamicregistries.testmod.entry.TestObject;
import net.ashwork.dynamicregistries.testmod.entry.TestObject1;
import net.ashwork.dynamicregistries.testmod.entry.TestObject2;
import net.ashwork.dynamicregistries.testmod.entry.TestObjectSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Objects;
import java.util.function.Supplier;

//TODO: Document
public final class TestObjectRegistrar {

    public static final DeferredRegister<TestObjectSerializer> REGISTER = DeferredRegister.create(TestObjectSerializer.class, TestMod.ID);

    private static Supplier<IForgeRegistry<TestObjectSerializer>> REGISTRY;

    public static IForgeRegistry<TestObjectSerializer> registry() {
        return Objects.requireNonNull(REGISTRY, "Test Object registry has not been initialized yet").get();
    }

    public static final RegistryObject<TestObjectSerializer> TEST_OBJECT_1 = register("test_object_1", (Supplier<Codec<TestObject1>>) () ->
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT.fieldOf("primitive_test").forGetter(TestObject1::getTestPrimitive),
                            ResourceLocation.CODEC.fieldOf("object_test").forGetter(TestObject1::getTestObject)
                    ).apply(instance, TestObject1::new)
            )
    );

    public static final RegistryObject<TestObjectSerializer> TEST_OBJECT_2 = register("test_object_2", (Supplier<Codec<TestObject2>>) () ->
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT.fieldOf("primitive_test").forGetter(TestObject2::getTestPrimitive),
                            ResourceLocation.CODEC.fieldOf("object_test").forGetter(TestObject2::getTestObject),
                            Codec.DOUBLE.fieldOf("extended_primitive_test").forGetter(TestObject2::getExtendedTestPrimitive),
                            Codec.STRING.fieldOf("extended_object_test").forGetter(TestObject2::getExtendedTestObject)
                    ).apply(instance, TestObject2::new)
            )
    );

    public static void attach(final IEventBus modBus) {
        REGISTRY = Lazy.of(REGISTER.makeRegistry("test_object", RegistryBuilder::new));
        REGISTER.register(modBus);
    }

    private static <A extends TestObject> RegistryObject<TestObjectSerializer> register(final String name, final Supplier<Codec<A>> entryCodec) {
        return REGISTER.register(name, () -> new TestObjectSerializer(entryCodec.get()));
    }
}
