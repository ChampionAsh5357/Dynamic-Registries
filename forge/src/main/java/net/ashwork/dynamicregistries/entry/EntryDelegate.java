package net.ashwork.dynamicregistries.entry;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class EntryDelegate<T> implements Supplier<T> {

    private final ExpandedResourceKey key;
    @Nullable
    private T value;

    public EntryDelegate(final ExpandedResourceKey key) {
        this.key = key;
    }

    @Override
    public T get() {
        return Objects.requireNonNull(this.value, () -> "Entry Delegate not present: " + this.key);
    }

    public ExpandedResourceKey getKey() {
        return this.key;
    }
}
