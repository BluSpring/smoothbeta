package net.mine_diver.smoothbeta.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.OS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Invoker("logGlError")
    void smoothbeta_printOpenGLError(String location);

    @Invoker("getOs")
    static OS callGetOs() {
        throw new IllegalStateException();
    }

    @Accessor("fullscreen")
    boolean isFullscreen();

    @Accessor("fullscreen")
    void setFullscreen(boolean b);
}
