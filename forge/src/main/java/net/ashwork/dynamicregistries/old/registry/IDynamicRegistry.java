/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old.registry;

import com.mojang.serialization.Codec;
import net.ashwork.dynamicregistries.old.entry.ICodecEntry;
import net.ashwork.dynamicregistries.old.entry.IDynamicEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The base of the dynamic registry system. All dynamic registries
 * extend this method at some point. This is heavily based off of
 * {@link IForgeRegistry}.
 *
 * @param <V> the super type of the dynamic registry entry
 * @param <C> the super type of the codec registry entry
 */
public interface IDynamicRegistry<V extends IDynamicEntry<V>, C extends ICodecEntry<V, C>> extends Iterable<V>, Codec<V> {

    /**
     * All registry names must be between 2 and 64 characters.
     * The first character must be from the lowercase alphabet.
     * All other characters can be lowercase alphanumeric, underscores,
     * and dashes.
     */
    Pattern VALID_NAMES = Pattern.compile("^[a-z][a-z0-9_-]{1,63}$");

    /**
     * Returns the name of the dynamic registry.
     *
     * @implSpec
     * All registry names are checked and validated against {@link #VALID_NAMES}.
     * All entries within a particular dynamic registry are stored by
     * {@code ./data/<entry_namespace>/dynamic_registries/<namespace>/<path>/<entry_path>.json}.
     *
     * @return the name of the dynamic registry
     */
    ResourceLocation getName();

    /**
     * Returns the super type of the entries in this registry.
     *
     * @return the super type of the entries in this registry
     */
    Class<V> getEntrySuperType();

    /**
     * Returns the super type of the entries within the codec registry.
     *
     * @return the super type of the entries within the codec registry
     */
    Class<C> getCodecSuperType();

    /**
     * Checks whether this registry has a registry object registered with
     * the given {@code key}.
     *
     * @param key the identifier of the registry object to be checked
     * @return {@code true} if this registry contains a registry object
     *         with the specified key, otherwise {@code false}
     * @throws NullPointerException if {@code key} is null
     */
    boolean containsKey(final ResourceLocation key);

    /**
     * Checks whether this registry has a key registered for the given
     * {@code value}.
     *
     * @param value the registry object to be checked
     * @return {@code true} if this registry contains a key for the
     *         specified registry object, otherwise {@code false}
     * @throws NullPointerException if {@code value} is null
     */
    boolean containsValue(final V value);

    /**
     * Returns {@code true} if the registry has no registry objects registered,
     * otherwise {@code false}.
     *
     * @return {@code true} if the registry has no registry objects registered,
     *         otherwise {@code false}.
     */
    boolean isEmpty();

    /**
     * Gets the registry object associated with the {@code key} if present. If
     * none exists, then {@code null} is returned.
     *
     * @param key the identifier of a registry object
     * @return the associated registry object or {@code null}
     * @throws NullPointerException if {@code key} is null
     */
    @Nullable
    V getValue(final ResourceLocation key);

    /**
     * Gets an optional containing the registry object associated with the
     * {@code key} if present. If none exists, then {@link Optional#empty()}
     * is returned.
     *
     * @param key the identifier of a registry object
     * @return the optional containing the associated registry object or
     *         {@link Optional#empty()}
     * @throws NullPointerException if {@code key} is null
     */
    default Optional<V> getValueOptional(final ResourceLocation key) {
        return Optional.ofNullable(this.getValue(key));
    }

    /**
     * Gets the identifier associated with the {@code value} if present. If
     * none exists, then {@code null} is returned.
     *
     * @param value the registry object
     * @return the associated identifier or {@code null}
     * @throws NullPointerException if {@code key} is null
     */
    @Nullable
    ResourceLocation getKey(final V value);

    /**
     * Gets an optional containing the identifier associated with the
     * {@code value} if present. If none exists, then {@link Optional#empty()}
     * is returned.
     *
     * @param value the registry object
     * @return the optional containing the associated identifier or
     *         {@link Optional#empty()}
     * @throws NullPointerException if {@code value} is null
     */
    default Optional<ResourceLocation> getKeyOptional(final V value) {
        return Optional.ofNullable(this.getKey(value));
    }

    /**
     * Returns the set of all identifiers within the registry.
     *
     * @return the set of all identifiers
     */
    Set<ResourceLocation> keySet();

    /**
     * Returns the set of all registry objects within the registry.
     *
     * @return the set of all registry objects
     */
    Set<V> values();

    /**
     * Returns the set of all entries within the registry.
     *
     * @return the set of all entries
     */
    Set<Map.Entry<ResourceLocation, V>> entrySet();

    /**
     * Returns a stream of registry objects within the registry.
     *
     * @return a stream of registry objects
     */
    default Stream<V> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * Performs the given action for each entry in the dynamic registry until all
     * entries have been processed or the action throws an exception.
     *
     * @param action the action to be performed for each entry
     * @throws NullPointerException if the specified action is null
     */
    default void forEach(BiConsumer<? super ResourceLocation, ? super V> action) {
        Objects.requireNonNull(action, "The action for the registry is null");
        this.entrySet().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    @Override
    default Iterator<V> iterator() {
        return this.values().iterator();
    }
}
