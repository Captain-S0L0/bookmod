package com.terriblefriends.bookmod.mixin.nbt;

import net.minecraft.nbt.NbtShort;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtShort.class)
public class NbtShortMixin {
    @Unique
    private final NbtShort NS_instance = (NbtShort) (Object) this;

    @Inject(at=@At("HEAD"),method="toString",cancellable = true)
    private void bookmod$writeToJson(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(""+NS_instance.value+"s");
        cir.cancel();
    }
}
