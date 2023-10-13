package com.terriblefriends.bookmod.mixin.nbt;

import net.minecraft.nbt.NbtFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtFloat.class)
public class NbtFloatMixin {
    private final NbtFloat NF_instance = (NbtFloat) (Object) this;

    @Inject(at=@At("HEAD"),method="toString",cancellable = true)
    private void bookmod$writeToJson(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(""+NF_instance.value+"f");
        cir.cancel();
    }
}
