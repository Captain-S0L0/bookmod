package com.terriblefriends.bookmod.mixin;

import com.terriblefriends.bookmod.surrogate.TextFieldWidgetSurrogate;
import com.terriblefriends.bookmod.surrogate.TextRendererSurrogate;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.TextRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin implements TextFieldWidgetSurrogate {
    @Shadow @Final private TextRenderer textRenderer;
    @Unique
    private boolean disableInvalidStripping = false;
    @Unique
    private boolean disableFormatting = false;

    public void bookmod$setDisableInvalidStripping(boolean v) {
        this.disableInvalidStripping = v;
    }

    public void bookmod$setDisableFormatting(boolean v) {
        this.disableFormatting = v;
    }

    @Redirect(method="write", at=@At(value="INVOKE", target="Lnet/minecraft/SharedConstants;stripInvalidChars(Ljava/lang/String;)Ljava/lang/String;"))
    private String bookmod$checkDisabledInvalidStripping(String s) {
        if (this.disableInvalidStripping) {
            return s;
        }
        else {
            return SharedConstants.stripInvalidChars(s);
        }
    }

    @Inject(at=@At("HEAD"), method="render")
    private void bookmod$checkDisabledFormatting(CallbackInfo ci) {
        if (this.disableFormatting) {
            ((TextRendererSurrogate)this.textRenderer).bookmod$setDisableFormatting(true);
        }
    }

    @Inject(at=@At("TAIL"), method="render")
    private void bookmod$checkDisabledFormatting2(CallbackInfo ci) {
        if (this.disableFormatting) {
            ((TextRendererSurrogate)this.textRenderer).bookmod$setDisableFormatting(false);
        }
    }

    @Inject(at=@At("HEAD"), method="mouseClicked")
    private void bookmod$checkDisabledFormatting3(int mouseY, int button, int par3, CallbackInfo ci) {
        if (this.disableFormatting) {
            ((TextRendererSurrogate)this.textRenderer).bookmod$setDisableFormatting(true);
        }
    }

    @Inject(at=@At("TAIL"), method="mouseClicked")
    private void bookmod$checkDisabledFormatting4(int mouseY, int button, int par3, CallbackInfo ci) {
        if (this.disableFormatting) {
            ((TextRendererSurrogate)this.textRenderer).bookmod$setDisableFormatting(false);
        }
    }
}
