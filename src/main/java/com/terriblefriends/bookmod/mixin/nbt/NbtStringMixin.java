package com.terriblefriends.bookmod.mixin.nbt;

import net.minecraft.nbt.NbtString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtString.class)
public class NbtStringMixin {
    @Unique
    private final NbtString NS_instance = (NbtString) (Object) this;

    @Inject(at=@At("HEAD"),method="toString",cancellable = true)
    private void bookmod$writeToJson(CallbackInfoReturnable<String> cir) {
        StringBuilder stringbuilder = new StringBuilder("\"");

        for (int i = 0; i < NS_instance.value.length(); ++i)
        {
            char c0 = NS_instance.value.charAt(i);

            if (c0 == '\\' || c0 == '"')
            {
                stringbuilder.append('\\');
            }

            stringbuilder.append(c0);
        }
        cir.setReturnValue(stringbuilder.append('"').toString());
        cir.cancel();
    }
}
