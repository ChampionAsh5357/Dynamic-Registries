/*
 * Dynamic Registries
 * Copyright (c) 2021-2021 ChampionAsh5357.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ashwork.dynamicregistries;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.WorldSavedData;

//TODO: Document and implement
public class DynamicRegistryData extends WorldSavedData {

    public DynamicRegistryData() {
        super(DynamicRegistries.ID);
    }

    @Override
    public void load(CompoundNBT tag) {
        tag.getAllKeys().forEach(name ->
                DynamicRegistryManager.DYNAMIC.getRegistry(new ResourceLocation(name)).fromSnapshot(tag.get(name), NBTDynamicOps.INSTANCE));
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        DynamicRegistryManager.DYNAMIC.registries(DynamicRegistryManager.Lookup.SAVE).forEach(entry ->
                tag.put(entry.getKey().toString(), entry.getValue().toSnapshot(NBTDynamicOps.INSTANCE)));
        return tag;
    }
}
