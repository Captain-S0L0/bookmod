package com.terriblefriends.bookmod.mixin.accessor;

import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NbtList.class)
public interface NbtListAccessor {
    @Accessor("type")
    byte bookmod$getType();
}
