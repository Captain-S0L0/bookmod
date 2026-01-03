package com.terriblefriends.bookmod.mixin.nbt;

import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

@Mixin(NbtCompound.class)
public class NbtCompoundMixin {
    @Shadow private Map elements;
    @Unique
    private static final Pattern pattern = Pattern.compile("[A-Za-z0-9._+-]+");

    @Inject(at=@At("HEAD"),method="toString",cancellable = true)
    private void bookmod$writeToJson(CallbackInfoReturnable<String> cir) {
        StringBuilder stringbuilder = new StringBuilder("{");
        Collection<String> collection = this.elements.keySet();

        for (String s : collection) {
            if (stringbuilder.length() != 1) {
                stringbuilder.append(',');
            }

            stringbuilder.append(escape(s)).append(':').append(this.elements.get(s));
        }

        stringbuilder.append('}');

        cir.setReturnValue(stringbuilder.toString());
        cir.cancel();
    }

    @Unique
    private static String escape(String s) {
        if (pattern.matcher(s).matches()) {
            return s;
        }
        else {
            StringBuilder stringbuilder = new StringBuilder("\"");

            for (int i = 0; i < s.length(); ++i) {
                char c0 = s.charAt(i);

                if (c0 == '\\' || c0 == '"') {
                    stringbuilder.append('\\');
                }

                stringbuilder.append(c0);
            }

            return stringbuilder.append('"').toString();
        }
    }
}
