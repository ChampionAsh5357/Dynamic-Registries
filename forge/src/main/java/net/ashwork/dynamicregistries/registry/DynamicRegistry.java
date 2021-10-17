package net.ashwork.dynamicregistries.registry;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import net.ashwork.dynamicregistries.entry.ExpandedResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface DynamicRegistry<V> extends Iterable<V>, Codec<V>, Keyable {

    ExpandedResourceKey name();

    default boolean containsKey(final ResourceLocation key) {
        return this.containsKey(ExpandedResourceKey.createEntry(this.name(), key));
    }

    boolean containsKey(final ExpandedResourceKey key);

    boolean containsValue(final V value);

    @Nullable
    V get(final ResourceLocation key);

    @Nullable
    default V get(final ExpandedResourceKey key) {
        return (key.belongsTo(this.name())) ? this.get(key.entry()) : null;
    }

    default Optional<V> getOptional(final ResourceLocation key) {
        return Optional.ofNullable(this.get(key));
    }

    default Optional<V> getOptional(final ExpandedResourceKey key) {
        return Optional.ofNullable(this.get(key));
    }

    @Nullable
    ResourceLocation getKey(final V value);

    @Nullable
    default ExpandedResourceKey getResourceKey(final V value) {
        final ResourceLocation rl = this.getKey(value);
        return rl == null ? null : ExpandedResourceKey.createEntry(this.name(), rl);
    }

    default Optional<ResourceLocation> getKeyOptional(final V value) {
        return Optional.ofNullable(this.getKey(value));
    }

    default Optional<ExpandedResourceKey> getResourceKeyOptional(final V value) {
        return Optional.ofNullable(this.getResourceKey(value));
    }

    Set<ResourceLocation> keySet();

    Set<V> values();

    Set<Map.Entry<ResourceLocation, V>> entrySet();

    @Override
    default <T> Stream<T> keys(DynamicOps<T> ops) {
        return this.keySet().stream().map(rl -> ops.createString(rl.toString()));
    }

    default Stream<V> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    default Iterator<V> iterator() {
        return this.values().iterator();
    }

    @Override
    default <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        return ResourceLocation.CODEC.decode(ops, input).flatMap(pair -> {
           final V value = this.get(pair.getFirst());
           return value == null ? DataResult.error("Unknown registry key: " + pair.getFirst()) : DataResult.success(Pair.of(value, pair.getSecond()));
        });
    }

    @Override
    default <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        final ResourceLocation rl = this.getKey(input);
        return rl == null ? DataResult.error("Unknown registry element: " + input) : ops.mergeToPrimitive(prefix, ops.createString(rl.toString()));
    }
}
