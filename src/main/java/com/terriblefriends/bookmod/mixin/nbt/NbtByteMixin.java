package com.terriblefriends.bookmod.mixin.nbt;

import net.minecraft.nbt.NbtByte;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtByte.class)
public class NbtByteMixin {
    @Unique
    private final NbtByte NB_instance = (NbtByte) (Object) this;

    @Inject(at=@At("HEAD"),method="toString",cancellable = true)
    private void bookmod$writeToJson(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(""+NB_instance.value+"b");
        cir.cancel();
    }
}
