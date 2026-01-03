package com.terriblefriends.bookmod.mixin.nbt;

import net.minecraft.nbt.NbtIntArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtIntArray.class)
public class NbtIntArrayMixin {
    @Unique
    private final NbtIntArray NIA_instance = (NbtIntArray) (Object) this;

    @Inject(at=@At("HEAD"),method="toString",cancellable = true)
    private void bookmod$writeToJson(CallbackInfoReturnable<String> cir) {
        StringBuilder stringbuilder = new StringBuilder("[I;");

        for (int i = 0; i < NIA_instance.value.length; ++i)
        {
            if (i != 0)
            {
                stringbuilder.append(',');
            }

            stringbuilder.append(NIA_instance.value[i]);
        }

        stringbuilder.append(']');

        cir.setReturnValue(stringbuilder.toString());
        cir.cancel();
    }
}
