package com.terriblefriends.bookmod.mixin.nbt;

import net.minecraft.nbt.NbtLong;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtLong.class)
public class NbtLongMixin {
    @Unique
    private final NbtLong NL_instance = (NbtLong) (Object) this;

    @Inject(at=@At("HEAD"),method="toString",cancellable = true)
    private void bookmod$writeToJson(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(""+NL_instance.value+"L");
        cir.cancel();
    }
}
