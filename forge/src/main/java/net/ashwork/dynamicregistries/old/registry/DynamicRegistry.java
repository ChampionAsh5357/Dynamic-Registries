/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old.registry;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ashwork.dynamicregistries.old.DynamicRegistries;
import net.ashwork.dynamicregistries.old.DynamicRegistryData;
import net.ashwork.dynamicregistries.old.DynamicRegistryListener;
import net.ashwork.dynamicregistries.old.DynamicRegistryManager;
import net.ashwork.dynamicregistries.old.entry.ICodecEntry;
import net.ashwork.dynamicregistries.old.entry.IDynamicEntry;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The base implementation of {@link IDynamicRegistry}. All registries will be
 * an implementation of this class.
 *
 * @param <V> the super type of the dynamic registry entry
 * @param <C> the super type of the codec registry entry
 */
public class DynamicRegistry<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> implements ICodecRegistrableDynamicRegistry<V, C>, IModifiableDynamicRegistry<V, C>, ISnapshotDynamicRegistry<V, C>, IStageableDynamicRegistry<V, C> {

    /**
     * The name of the registry.
     */
    private final ResourceLocation name;
    /**
     * The registry configuration.
     */
    protected final DynamicRegistryBuilder<V, C> builder;
    /**
     * The stage the registry is constructed within.
     */
    private final DynamicRegistryManager stage;
    /**
     * The super class of the registry entries.
     */
    private final Class<V> superType;
    /**
     * The codec registry to encode/decode these registry entries.
     */
    private final IForgeRegistry<C> codecRegistry;
    /**
     * The default key of the registry.
     */
    @Nullable
    private final ResourceLocation defaultKey;
    /**
     * The reload strategy of the registry.
     */
    private final ReloadStrategy reloadStrategy;
    /**
     * The registry entry codecs in their simple and exploded form.
     */
    private final Codec<V> registryEntryCodec, explodedEntryCodec;
    /**
     * The snapshot codec for encoding/decoding the registry.
     */
    private final Function<Boolean, Codec<DynamicRegistry<V, C>>> snapshotCodec;

    /**
     * The entries within the registry.
     */
    protected final BiMap<ResourceLocation, V> entries;
    /**
     * The entry aliases within the registry.
     */
    protected final Map<ResourceLocation, ResourceLocation> aliases;
    /**
     * Registry names that are stored with no mapped values.
     */
    protected final Set<ResourceLocation> dummies;

    /**
     * Stores the missing entries associated with this registry.
     */
    private MissingEntryManager missingEntryManager;
    /**
     * The default value of the registry.
     */
    @Nullable
    private V defaultValue;
    /**
     * When {@code true}, the registry cannot be modified.
     */
    private boolean locked;

    /**
     * Constructs the new registry for the specified stage.
     *
     * @param builder the configuration details of the registry
     * @param stage the current stage of the registry
     */
    @SuppressWarnings("unchecked") // Suppresses warnings for cast to the current entry codec type
    public DynamicRegistry(DynamicRegistryBuilder<V, C> builder, DynamicRegistryManager stage) {
        this.name = builder.getName();
        this.builder = builder;
        this.stage = stage;
        this.superType = builder.getSuperType();
        this.codecRegistry = builder.getCodecRegistry();
        this.defaultKey = builder.getDefaultKey();
        this.reloadStrategy = builder.getReloadStrategy();
        this.registryEntryCodec = ResourceLocation.CODEC.comapFlatMap(id -> {
            @Nullable
            V val = this.getValue(id);
            return val != null ? DataResult.success(val)
                    : DataResult.error("Not a valid registry object within " + this.getName() + ": " + id);
        }, IDynamicEntry::getRegistryName);
        this.explodedEntryCodec = ResourceLocation.CODEC.comapFlatMap(id -> {
            @Nullable C val = this.codecRegistry.getValue(id);
            return val != null ? DataResult.success(val)
                    : DataResult.error("Not a valid registry object within " + this.codecRegistry.getRegistryName() + ": " + id);
        }, ICodecEntry::getRegistryName).dispatch(dyn -> (C) dyn.codec(), ICodecEntry::entryCodec);
        this.snapshotCodec = isSavedData -> RecordCodecBuilder.create(instance ->
                instance.group(
                        RecordCodecBuilder.point(this),
                        Codec.unboundedMap(ResourceLocation.CODEC, this.explodedEntryCodec).fieldOf("entries").forGetter(reg -> reg.entries),
                        Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).fieldOf("aliases").forGetter(reg -> reg.aliases),
                        ResourceLocation.CODEC.listOf().xmap(list -> (Set<ResourceLocation>) ImmutableSet.copyOf(list), ImmutableList::copyOf).fieldOf("dummies").forGetter(reg -> reg.dummies),
                        RecordCodecBuilder.point(isSavedData)
                ).apply(instance, DynamicRegistry<V, C>::fromSnapshot)
        );
        this.entries = HashBiMap.create();
        this.aliases = new HashMap<>();
        this.dummies = new HashSet<>();
        this.locked = true;
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public Class<V> getEntrySuperType() {
        return this.superType;
    }

    @Override
    public Class<C> getCodecSuperType() {
        return this.codecRegistry.getRegistrySuperType();
    }

    @Override
    public boolean containsKey(ResourceLocation key){
        while (key != null) {
            if (this.entries.containsKey(key)) return true;
            key = this.aliases.get(key);
        }
        return false;
    }

    @Override
    public boolean containsValue(V value) {
        return this.entries.containsValue(value);
    }

    @Override
    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    @Nullable
    @Override
    public V getValue(ResourceLocation key) {
        V ret;
        do {
            ret = this.entries.get(key);
            key = this.aliases.get(key);
        } while (ret == null && key != null);
        return ret == null ? this.defaultValue : ret;
    }

    @Nullable
    @Override
    public ResourceLocation getKey(V value) {
        ResourceLocation ret = this.entries.inverse().get(value);
        return ret == null ? this.defaultKey : ret;
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.entries.keySet());
    }

    @Override
    public Set<V> values() {
        return Collections.unmodifiableSet(this.entries.values());
    }

    @Override
    public Set<Map.Entry<ResourceLocation, V>> entrySet() {
        return Collections.unmodifiableSet(this.entries.entrySet());
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    /**
     * Unlocks the registry for modification.
     */
    public void unlock() {
        this.locked = false;
    }

    /**
     * Locks the registry so no modification can occur.
     */
    public void lock() {
        this.locked = true;
    }

    @Nullable
    @Override
    public V remove(ResourceLocation key) {
        if (this.isLocked())
            throw this.constructLockedError("remove");

        DynamicRegistries.LOGGER.debug(MODIFY, "Registry object {} is being removed from {}", key, this.getName());
        return this.entries.remove(key);
    }

    @Override
    public void clear() {
        if (this.isLocked())
            throw this.constructLockedError("clear");

        DynamicRegistries.LOGGER.debug(MODIFY, "Registry {} is being cleared", this.getName());
        this.aliases.clear();
        this.entries.clear();
        this.dummies.clear();
        this.defaultValue = null;
        this.missingEntryManager = new MissingEntryManager(Collections.emptyMap());
    }

    @Override
    public void register(V value) {
        if (this.isLocked())
            throw this.constructLockedError("register");

        Preconditions.checkNotNull(value, "Cannot add a null object to the registry.");
        ResourceLocation identifier = value.getRegistryName();
        Preconditions.checkNotNull(identifier, "Cannot use a null identifier for entry: " + value);

        V originalEntry = this.getValue(identifier);
        if (originalEntry == value) { // Already registered
            DynamicRegistries.LOGGER.warn(REGISTER, "The object {} has already been registered under the same name {} within registry {}, skipping", value, identifier, this.getName());
            return;
        } else if (this.containsValue(value)) { // Value already registered
            throw new IllegalArgumentException("The object " + value + " has already been registered under the name " + this.getKey(value));
        } else if (this.containsKey(identifier)) { // Key already registered
            if (identifier.equals(this.defaultKey) && this.defaultValue != null) // Setting the default key again
                throw new IllegalArgumentException("Cannot override the default entry " + identifier + " within registry " + this.getName());
            DynamicRegistries.LOGGER.debug(REGISTER, "Registry {} Override: {} {} -> {}", this.getName(), identifier, originalEntry, value);
        }

        if (identifier.equals(this.defaultKey)) {
            this.defaultValue = value;
        }

        this.entries.put(identifier, value);
        this.dummies.remove(identifier);
    }

    @Override
    public DynamicRegistry<V, C> copy(final DynamicRegistryManager stage) {
        return new DynamicRegistry<>(builder, stage);
    }

    @Override
    public Set<ResourceLocation> setAndUnlockFromStage(final DynamicRegistryManager stage) {
        Set<ResourceLocation> oldEntries = new HashSet<>();
        oldEntries.addAll(this.keySet());
        oldEntries.addAll(this.aliases.keySet());
        oldEntries.addAll(this.dummies);

        IDynamicRegistry<V, C> stagedRegistry = stage.getRegistry(this.getName());
        if (stagedRegistry == null)
            throw new IllegalArgumentException("The registry " + this.getName() + " does not exist within " + stage.getName());
        this.unlock();
        Stream<V> stagedEntries = stagedRegistry.stream();
        // If the reload strategy is clear we want to remove all current entries and replace them.
        // Otherwise, we just want filter out the already registered entries and then register whatever is left.
        if (this.reloadStrategy == ReloadStrategy.CLEAR) {
            this.clear();
        } else stagedEntries = stagedEntries.filter(value -> !this.containsValue(value));
        stagedEntries.forEach(this::register);
        return ImmutableSet.copyOf(oldEntries);
    }

    /**
     * Writes the data from a snapshot to this current registry.
     *
     * @param entries the entries of the snapshot
     * @param aliases the entry aliases of the snapshot
     * @param dummies the dummy entries of the snapshot
     * @param isSavedData if the data was populated from {@link DynamicRegistryData}
     * @return the current registry instance with the data overwritten
     */
    private DynamicRegistry<V, C> fromSnapshot(final Map<ResourceLocation, V> entries, final Map<ResourceLocation, ResourceLocation> aliases, final Set<ResourceLocation> dummies, final boolean isSavedData) {
        this.unlock();
        if (isSavedData) {
            if (this.reloadStrategy == ReloadStrategy.REPLACE) {
                dummies.stream().filter(id -> !this.containsKey(id)).forEach(this.dummies::add);
                entries.entrySet().stream().filter(entry -> !this.containsKey(entry.getKey())).forEach(entry -> {
                    V registryObject = entry.getValue();
                    registryObject.setRegistryName(entry.getKey());
                    this.register(registryObject);
                });
                aliases.entrySet().stream().filter(entry -> !this.aliases.containsKey(entry.getKey())).forEach(entry -> this.aliases.put(entry.getKey(), entry.getValue()));

            } else {
                this.handleMissingEntries(Streams.concat(entries.keySet().stream(), aliases.keySet().stream(), dummies.stream()));
            }
        } else {
            this.clear();
            this.dummies.addAll(dummies);
            entries.forEach((id, registryObject) -> {
                registryObject.setRegistryName(id);
                this.register(registryObject);
            });
            this.aliases.putAll(aliases);
        }
        this.lock();
        return this;
    }

    @Override
    public Codec<V> entryCodec() {
        return this.explodedEntryCodec;
    }

    @Override
    public <T> DataResult<Pair<V, T>> decode(final DynamicOps<T> ops, final T input) {
        return this.registryEntryCodec.decode(ops, input);
    }

    @Override
    public <T> DataResult<T> encode(final V input, final DynamicOps<T> ops, final T prefix) {
        return this.registryEntryCodec.encode(input, ops, prefix);
    }

    @Override
    public Codec<ISnapshotDynamicRegistry<V, C>> snapshotCodec(final boolean isSavedData) {
        return this.snapshotCodec.apply(isSavedData).xmap(Function.identity(), DynamicRegistry.class::cast);
    }

    /**
     * Handles populating the missing entry manager and any missing mappings from
     * a previous load.
     *
     * @param oldEntries the old entries of the registry
     * @param entryStrategies the encoded entry strategies
     * @param ops the operator used to transmute the encoded object
     * @param <T> the type of the encoded object
     */
    public <T> void postReloadedEntries(final Set<ResourceLocation> oldEntries, final Set<T> entryStrategies, final DynamicOps<T> ops) {
        // Invalidate the original entry manager and add old entries
        this.missingEntryManager = new MissingEntryManager(Collections.emptyMap());
        entryStrategies.forEach(strategies -> this.addEntryStrategies(strategies, ops));

        this.handleMissingEntries(oldEntries.stream());
    }

    /**
     * Handles the resolution strategy for any missing entries.
     *
     * @param oldEntries the original entries within the registry
     */
    private void handleMissingEntries(final Stream<ResourceLocation> oldEntries) {
        oldEntries.filter(id -> !this.containsKey(id) && !this.aliases.containsKey(id) && !this.dummies.contains(id))
                .forEach(id -> this.missingEntryManager.handle(id, this));
    }

    /**
     * Adds entries strategies to the manager.
     *
     * @param input the encoded entry strategies
     * @param ops the operator used to transmute the encoded object
     * @param <T> the type of the encoded object
     */
    private <T> void addEntryStrategies(final T input, final DynamicOps<T> ops) {
        this.missingEntryManager = this.missingEntryManager.merge(input, ops);
    }

    /**
     * Constructs a generic locked error exception.
     *
     * @param action the action being performed that caused the exception
     * @return an {@link IllegalStateException} to be thrown
     */
    private IllegalStateException constructLockedError(String action) {
        return new IllegalStateException("Attempted to " + action + " from " + this.getName() + " while locked");
    }

    /**
     * Reload strategies that the registry may use whenever {@link DynamicRegistryListener}
     * is called.
     */
    public enum ReloadStrategy {
        /**
         * Previous registry entries will not be removed. They will be overwritten by
         * newly incoming entries and the old ones will persist.
         */
        REPLACE,
        /**
         * Registry entries will be removed during every reload. This is the default
         * behavior.
         */
        CLEAR
    }

    /**
     * Missing entry strategies to be handled whenever the reload strategy is {@link ReloadStrategy#CLEAR}
     * and an existing entry is no longer present.
     */
    private interface MissingEntryStrategy {

        /**
         * Handles the missing entry.
         *
         * @param missingName the name of the missing entry
         * @param registry the registry the entry belonged to
         */
        void handle(final ResourceLocation missingName, final DynamicRegistry<?, ?> registry);
    }

    /**
     * A {@link MissingEntryStrategy} that remaps some missing name to the given remapped name
     * via {@link #getRemappedName()}.
     */
    private interface RemapStrategy extends MissingEntryStrategy {

        /**
         * Gets the remapped name of the entry.
         *
         * @return the remapped name of the entry
         */
        ResourceLocation getRemappedName();

        @Override
        default void handle(final ResourceLocation missingName, final DynamicRegistry<?, ?> registry) {
            DynamicRegistries.LOGGER.debug(MissingEntryManager.MISSING_ENTRY, "{} has been remapped to {} within {}", missingName, this.getRemappedName(), registry.getName());
            registry.aliases.put(missingName, this.getRemappedName());
        }
    }

    /**
     * Holds all missing entry strategies within a registry. Missing entry strategies are always replaced
     * regardless of the registry mode.
     */
    public static class MissingEntryManager implements MissingEntryStrategy {
        /**
         * A marker that represents all logging information for missing entries.
         */
        private static final Marker MISSING_ENTRY = MarkerManager.getMarker("Missing Entry");

        /**
         * A map holding the missing entry strategy types:
         * <ul>
         *     <li>{@code remap}: Remaps the missing entry to the specified entry name.</li>
         *     <li>{@code dummy}: Stores a dummy reference to the default entry. This is the default behavior.</li>
         *     <li>{@code skip}: Ignores the missing entry and drops it from the registry.</li>
         *     <li>{@code fail}: Crashes the game.</li>
         * </ul>
         */
        private static final BiMap<String, Codec<? extends MissingEntryStrategy>> MISSING_STRATEGIES = Util.make(() -> {
            final ImmutableBiMap.Builder<String, Codec<? extends MissingEntryStrategy>> strategies = ImmutableBiMap.builder();
            strategies.put("remap", ResourceLocation.CODEC.fieldOf("remappedName").codec().xmap(remappedName -> () -> remappedName, RemapStrategy::getRemappedName));
            strategies.put("dummy", Codec.unit(() -> (missingName, registry) -> {
                DynamicRegistries.LOGGER.debug(MISSING_ENTRY, "Added {} as a dummy entry within {}", missingName, registry.getName());
                registry.dummies.add(missingName);
            }));
            strategies.put("skip", Codec.unit(() -> (missingName, registry) -> DynamicRegistries.LOGGER.warn(MISSING_ENTRY, "{} will be skipped within {}", missingName, registry.getName())));
            strategies.put("fail", Codec.unit(() -> ((missingName, registry) -> {
                DynamicRegistries.LOGGER.error(MISSING_ENTRY, "The entry {} must be present within {} for this world", missingName, registry.getName());
                throw new RuntimeException("The entry " + missingName + " must be present within " + registry.getName() + " for this world");
            })));
            return strategies.build();
        });

        /**
         * A codec representing a missing entry strategy.
         */
        private static final Codec<MissingEntryStrategy> STRATEGY_CODEC = STRING.dispatch(MISSING_STRATEGIES.inverse()::get, MISSING_STRATEGIES::get);

        /**
         * A codec representing the missing entry strategies within the registry.
         */
        private static final Codec<Map<ResourceLocation, MissingEntryStrategy>> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, STRATEGY_CODEC);

        /**
         * The default entry strategy if none is available.
         */
        private static final MissingEntryStrategy DEFAULT = (missingName, registry) -> {
            DynamicRegistries.LOGGER.debug(MISSING_ENTRY, "Added {} as a dummy entry within {}", missingName, registry.getName());
            registry.dummies.add(missingName);
        };

        /**
         * Strategies for entries in the registry.
         */
        private final Map<ResourceLocation, MissingEntryStrategy> strategies;

        /**
         * A constructor instance.
         *
         * @param strategies the entry strategies
         */
        private MissingEntryManager(final Map<ResourceLocation, MissingEntryStrategy> strategies) {
            this.strategies = ImmutableMap.copyOf(strategies);
        }

        /**
         * Creates a new missing entry manager from the previous entries and the new ones.
         * Any strategies that are duplicated in both the previous and new one will be
         * populated by the new entry.
         *
         * @param input the encoded form of the entry strategies
         * @param ops the operator used to transmute the encoded object
         * @param <T> the type of the encoded object
         * @return a new {@link MissingEntryManager}
         */
        public <T> MissingEntryManager merge(final T input, final DynamicOps<T> ops) {
            return new MissingEntryManager(Util.make(() -> {
                final HashMap<ResourceLocation, MissingEntryStrategy> strategies = new HashMap<>();
                strategies.putAll(this.strategies);
                strategies.putAll(createMap(input, ops));
                return ImmutableMap.copyOf(strategies);
            }));
        }

        @Override
        public void handle(ResourceLocation missingName, DynamicRegistry<?, ?> registry) {
            this.strategies.getOrDefault(missingName, Util.make(() -> {
                DynamicRegistries.LOGGER.warn(MISSING_ENTRY, "No missing entry strategy was present for {} in {}, defaulting to 'dummy'", missingName, registry.getName());
                return DEFAULT;
            })).handle(missingName, registry);
        }

        /**
         * Creates the entry strategies.
         *
         * @param input the encoded form of the entry strategies
         * @param ops the operator used to transmute the encoded object
         * @param <T> the type of the encoded object
         * @return the entry strategies
         */
        private static <T> Map<ResourceLocation, MissingEntryStrategy> createMap(final T input, final DynamicOps<T> ops) {
            return CODEC.parse(ops, input).resultOrPartial(error ->
                    DynamicRegistries.LOGGER.error(MISSING_ENTRY, "Missing entries have failed to deserialize properly: {}", error)
            ).orElse(Collections.emptyMap());
        }
    }
}
