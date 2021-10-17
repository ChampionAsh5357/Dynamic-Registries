/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries.old.entry;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Default implementation of {@link ICodecEntry}.
 *
 * @param <V> the super type of the dynamic registry entry
 * @param <C> the super type of the codec registry entry
 */
public abstract class CodecEntry<V extends IDynamicEntry<?>, C extends ICodecEntry<V, C>> extends ForgeRegistryEntry.UncheckedRegistryEntry<C> implements ICodecEntry<V, C> {

    /**
     * An implementation of {@link CodecEntry} for easy extensibility. Takes in some
     * codec to be passed in of the particular type.
     *
     * @param <V> the super type of the dynamic registry entry
     * @param <C> the super type of the codec registry entry
     */
    public abstract static class Instance<V extends IDynamicEntry<?>, C extends ICodecEntry<V, C>> extends CodecEntry<V, C> {

        /**
         * The entry codec.
         */
        private final Codec<? extends V> entryCodec;

        /**
         * A simple constructor instance.
         *
         * @param entryCodec the codec to encode/decode the particular entry
         */
        protected Instance(final Codec<? extends V> entryCodec) {
            this.entryCodec = entryCodec;
        }

        @Override
        public Codec<? extends V> entryCodec() {
            return this.entryCodec;
        }

        @Override
        public String toString() {
            return this.getRegistryName() + "<" + this.getClass().getSimpleName() + "[ " + this.entryCodec + " ]>";
        }
    }
}
