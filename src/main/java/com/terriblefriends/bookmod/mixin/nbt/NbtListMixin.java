package com.terriblefriends.bookmod.mixin.nbt;

import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(NbtList.class)
public class NbtListMixin {
    @Shadow private List value;

    @Inject(at=@At("HEAD"),method="toString",cancellable = true)
    private void bookmod$writeToJson(CallbackInfoReturnable<String> cir) {
        StringBuilder stringbuilder = new StringBuilder("[");

        for (int i = 0; i < this.value.size(); ++i)
        {
            if (i != 0)
            {
                stringbuilder.append(',');
            }

            stringbuilder.append(this.value.get(i));
        }

        stringbuilder.append(']');

        cir.setReturnValue(stringbuilder.toString());
        cir.cancel();
    }
}
