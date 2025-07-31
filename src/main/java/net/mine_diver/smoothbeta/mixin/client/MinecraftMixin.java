package net.mine_diver.smoothbeta.mixin.client;

import net.mine_diver.smoothbeta.client.render.Shaders;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GLContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void initShaders(CallbackInfo ci) {
        Shaders.initShaders();
    }

    @Redirect(method = "runGame", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;isActive()Z"))
    private boolean forceDisableFullscreenToggle() {
        return true;
    }
}
