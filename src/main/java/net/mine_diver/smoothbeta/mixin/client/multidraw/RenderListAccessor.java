package net.mine_diver.smoothbeta.mixin.client.multidraw;

import net.minecraft.client.render.world.RenderChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.IntBuffer;

@Mixin(RenderChunkStorage.class)
public interface RenderListAccessor {
    @Accessor("glList")
    void smoothbeta_setGlListBuffer(IntBuffer buffer);

    @Accessor("renderingLeft")
    boolean smoothbeta_getInitialized();

    @Accessor("regionX")
    int smoothbeta_getX();

    @Accessor("regionY")
    int smoothbeta_getY();

    @Accessor("regionZ")
    int smoothbeta_getZ();

    @Accessor("cameraX")
    double smoothbeta_getOffsetX();

    @Accessor("cameraY")
    double smoothbeta_getOffsetY();

    @Accessor("cameraZ")
    double smoothbeta_getOffsetZ();
}
