package net.ashwork.dynamicregistries.entry;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ExpandedResourceKey implements Comparable<ExpandedResourceKey> {
    private static final Map<String, ExpandedResourceKey> VALUES = new ConcurrentHashMap<>();
    private final ResourceLocation root, entry;

    public static ExpandedResourceKey createEntry(final ExpandedResourceKey rootKey, final ResourceLocation entryName) {
        return create(rootKey.entry(), entryName);
    }

    protected static ExpandedResourceKey create(final ResourceLocation root, final ResourceLocation entry) {
        return VALUES.computeIfAbsent((root + ":" + entry).intern(), s -> new ExpandedResourceKey(root, entry));
    }

    private ExpandedResourceKey(final ResourceLocation root, final ResourceLocation entry) {
        this.root = Objects.requireNonNull(root, "The root of a resource key cannot be null");
        this.entry = Objects.requireNonNull(entry, "The entry of a resource key cannot be null");
    }

    public ResourceLocation root() {
        return this.root;
    }

    public ResourceLocation entry() {
        return this.entry;
    }

    public boolean belongsTo(final ExpandedResourceKey o) {
        return this.root().equals(o.entry());
    }

    @Override
    public int compareTo(final ExpandedResourceKey o) {
        int ret = this.root().compareTo(o.root());
        return ret == 0 ? this.entry().compareTo(o.entry()) : ret;
    }

    @Override
    public int hashCode() {
        return Objects.hash(root, entry);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpandedResourceKey that = (ExpandedResourceKey) o;
        return Objects.equals(root, that.root) && Objects.equals(entry, that.entry);
    }

    @Override
    public String toString() {
        return "ExpandedResourceKey[" + this.root + " / " + this.entry + ']';
    }
}
