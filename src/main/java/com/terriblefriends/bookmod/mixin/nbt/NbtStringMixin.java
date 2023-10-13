package com.terriblefriends.bookmod.mixin.nbt;

import net.minecraft.nbt.NbtString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtString.class)
public class NbtStringMixin {
    private final NbtString NS_instance = (NbtString) (Object) this;

    @Inject(at=@At("HEAD"),method="toString",cancellable = true)
    private void bookmod$writeToJson(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(""+NS_instance.value);
        cir.cancel();
    }
}
