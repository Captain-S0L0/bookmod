package com.terriblefriends.bookmod.mixin.nbt;

import net.minecraft.nbt.NbtByteArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtByteArray.class)
public class NbtByteArrayMixin {
    @Unique
    private final NbtByteArray NBA_instance = (NbtByteArray) (Object) this;

    @Inject(at=@At("HEAD"),method="toString",cancellable = true)
    private void bookmod$writeToJson(CallbackInfoReturnable<String> cir) {

        StringBuilder stringbuilder = new StringBuilder("[B;");

        for (int i = 0; i < NBA_instance.value.length; ++i)
        {
            if (i != 0)
            {
                stringbuilder.append(',');
            }

            stringbuilder.append(NBA_instance.value[i]).append('B');
        }

        stringbuilder.append(']');

        cir.setReturnValue(stringbuilder.toString());
        cir.cancel();
    }
}
