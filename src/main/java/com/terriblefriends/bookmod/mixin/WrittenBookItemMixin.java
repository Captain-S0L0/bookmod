package com.terriblefriends.bookmod.mixin;

import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WrittenBookItem.class)
public class WrittenBookItemMixin {
    @Redirect(at=@At(value="INVOKE", target="Lnet/minecraft/nbt/NbtString;toString()Ljava/lang/String;"), method="getDisplayName")
    private String bookmod$getDisplayNameNotToString(NbtString instance) {
        return instance.value;
    }
}
