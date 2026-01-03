package com.terriblefriends.bookmod.mixin.nbt;

import net.minecraft.nbt.NbtDouble;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtDouble.class)
public class NbtDoubleMixin {
    @Unique
    private final NbtDouble ND_instance = (NbtDouble) (Object) this;

    @Inject(at=@At("HEAD"),method="toString",cancellable = true)
    private void bookmod$writeToJson(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(""+ND_instance.value+"d");
        cir.cancel();
    }
}
