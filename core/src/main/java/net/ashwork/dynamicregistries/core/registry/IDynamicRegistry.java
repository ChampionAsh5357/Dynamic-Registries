/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.ashwork.dynamicregistries.core.registry;

import com.mojang.serialization.Codec;
import net.ashwork.dynamicregistries.core.util.IIdentifier;
import net.ashwork.dynamicregistries.core.entry.ICodecEntry;
import net.ashwork.dynamicregistries.core.entry.IDynamicEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The main interface for the dynamic registry system.
 *
 * @param <I> The identifier type
 * @param <V> The dynamic registry entry super type
 * @param <C> The codec registry entry super type
 */
public interface IDynamicRegistry<I extends IIdentifier, V extends IDynamicEntry<I, V>, C extends ICodecEntry<I, V, C>>
        extends Iterable<V>, Codec<V> {

    /**
     * All registry names must be between 3 and 64 characters.
     * The first character must be from the lowercase alphabet.
     * The last character must be lowercase alphanumeric. All
     * other characters can be lowercase alphanumeric and underscores.
     */
    Pattern VALID_NAMES = Pattern.compile("^[a-z][a-z0-9_]{1,62}[a-z0-9]$");

    /**
     * @implSpec All registry names will be checked
     *           against {@link IDynamicRegistry#VALID_NAMES}.
     * @implNote This is implemented as a string
     *           since the domain is expected to
     *           be as subdirectory within some
     *           main registry directory.
     *
     * @return The name of the registry.
     */
    String getName();

    /**
     * @return The registry type
     */
    Class<V> getEntrySuperType();

    /**
     * @return The codec registry type
     */
    Class<C> getCodecRegistrySuperType();

    /**
     * @param key The identifier the value is associated with
     * @return If there is a value associated with the identifier
     */
    boolean containsKey(I key);

    /**
     * @param value The value being checked
     * @return If the value is within the registry
     */
    boolean containsValue(V value);

    /**
     * @return If the registry has no entries
     */
    boolean isEmpty();

    /**
     * @param key The identifier the value is associated with
     * @return The value if present, null otherwise
     */
    @Nullable
    V getValue(I key);

    /**
     * @param key The identifier the value is associated with
     * @return An {@link Optional} containing the value if present,
     *         otherwise {@link Optional#empty()}
     */
    default Optional<V> getValueOptional(I key) {
        return Optional.ofNullable(this.getValue(key));
    }

    /**
     * @param value The value within the registry
     * @return The identifier associated with the value, null otherwise
     */
    @Nullable
    I getKey(V value);

    /**
     * @param value The value within the registry
     * @return An {@link Optional} containing the identifier if present,
     *         otherwise {@link Optional#empty()}
     */
    default Optional<I> getKeyOptional(V value) {
        return Optional.ofNullable(this.getKey(value));
    }

    /**
     * @return A set of identifiers within the registry
     */
    @Nonnull
    Set<I> keySet();

    /**
     * @return A set of values within the registry
     */
    @Nonnull
    Set<V> values();

    /**
     * @return A set of entries within the registry
     */
    @Nonnull
    Set<Map.Entry<I, V>> entrySet();

    /**
     * @return A stream of values within the registry
     */
    @Nonnull
    default Stream<V> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    default Iterator<V> iterator() {
        return this.values().iterator();
    }
}
