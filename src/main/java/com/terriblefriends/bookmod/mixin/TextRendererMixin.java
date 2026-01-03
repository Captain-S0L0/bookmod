package com.terriblefriends.bookmod.mixin;

import com.terriblefriends.bookmod.surrogate.TextRendererSurrogate;
import net.minecraft.client.render.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public class TextRendererMixin implements TextRendererSurrogate {
    @Shadow private byte[] glyphSizes;

    @Unique
    private boolean disableFormatting = false;

    @Unique
    @Override
    public void bookmod$setDisableFormatting(boolean v) {
        this.disableFormatting = v;
    }

    @Inject(at=@At(value = "RETURN", ordinal = 0), method="getWidth(C)I", cancellable = true)
    private void bookmod$checkDisabledFormatting(char par1, CallbackInfoReturnable<Integer> cir) {
        if (this.disableFormatting && par1 == 167 && cir.getReturnValueI() == -1) {
            int var3 = this.glyphSizes[167] >>> 4;
            int var4 = this.glyphSizes[167] & 15;
            if (var4 > 7) {
                var4 = 15;
                var3 = 0;
            }

            ++var4;

            cir.setReturnValue((var4 - var3) / 2 + 1);
            cir.cancel();
        }
    }

    @Redirect(method="drawLayer(Ljava/lang/String;Z)V", at=@At(value="INVOKE", target="Ljava/lang/String;length()I", ordinal = 1))
    private int bookmod$checkDisabledFormatting2(String instance) {
        if (this.disableFormatting) {
            return 0;
        }
        else {
            return instance.length();
        }
    }

    @Redirect(method="insertLineBreaks", at=@At(value="INVOKE", target="Lnet/minecraft/client/render/TextRenderer;isolateFormatting(Ljava/lang/String;)Ljava/lang/String;"))
    private String bookmod$checkDisabledFormatting3(String text) {
        if (this.disableFormatting) {
            return "";
        }
        else {
            return getFormattingOnly(text);
        }
    }

    @Unique
    private static boolean isColor(char character) {
        return character >= '0' && character <= '9' || character >= 'a' && character <= 'f' || character >= 'A' && character <= 'F';
    }

    @Unique
    private static boolean isSpecial(char character) {
        return character >= 'k' && character <= 'o' || character >= 'K' && character <= 'O' || character == 'r' || character == 'R';
    }

    @Unique
    private static String getFormattingOnly(String text) {
        String var1 = "";
        int var2 = -1;
        int var3 = text.length();

        while((var2 = text.indexOf(167, var2 + 1)) != -1) {
            if (var2 < var3 - 1) {
                char var4 = text.charAt(var2 + 1);
                if (isColor(var4)) {
                    var1 = "§" + var4;
                } else if (isSpecial(var4)) {
                    var1 = var1 + "§" + var4;
                }
            }
        }

        return var1;
    }
}
