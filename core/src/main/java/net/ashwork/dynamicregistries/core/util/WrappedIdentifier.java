/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.ashwork.dynamicregistries.core.util;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An implementation of {@link IIdentifier}
 * that wraps the unique object needed to
 * discern the registry entry.
 *
 * @param <T> The identifier type
 */
public class WrappedIdentifier<T> implements IIdentifier {

    private final T val;

    protected WrappedIdentifier(@Nonnull T val) {
        this.val = Objects.requireNonNull(val);
    }

    public T getVal() {
        return val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WrappedIdentifier<?> that = (WrappedIdentifier<?>) o;
        return Objects.equals(val, that.val);
    }

    @Override
    public int hashCode() {
        return Objects.hash(val);
    }

    @Override
    public String toString() {
        return Objects.toString(val);
    }
}
