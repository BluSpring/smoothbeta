package net.mine_diver.smoothbeta.mixin.client.multidraw;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.mine_diver.smoothbeta.client.render.SmoothChunkRenderer;
import net.mine_diver.smoothbeta.client.render.SmoothTessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
abstract class TessellatorMixin implements SmoothTessellator {
    @Shadow protected abstract void clear();

    @Shadow private ByteBuffer byteBuffer;
    @Shadow private int index;
    @Unique
    private boolean smoothbeta_renderingTerrain;
    @Unique
    private SmoothChunkRenderer smoothbeta_chunkRenderer;

    @Override
    @Unique
    public void smoothbeta_startRenderingTerrain(SmoothChunkRenderer chunkRenderer) {
        smoothbeta_renderingTerrain = true;
        smoothbeta_chunkRenderer = chunkRenderer;
    }

    @Override
    @Unique
    public void smoothbeta_stopRenderingTerrain() {
        smoothbeta_renderingTerrain = false;
        smoothbeta_chunkRenderer = null;
    }

    @Override
    public boolean smoothbeta_isRenderingTerrain() {
        return smoothbeta_renderingTerrain;
    }

    @Inject(
            method = "end",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/ByteBuffer;limit(I)Ljava/nio/Buffer;",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void smoothbeta_uploadTerrain(CallbackInfoReturnable<Integer> cir) {
        if (!smoothbeta_renderingTerrain) return;
        smoothbeta_chunkRenderer.smoothbeta_getCurrentBuffer().upload(byteBuffer);
        int value = this.index * 4;
        clear();
        cir.setReturnValue(value);
    }

    @ModifyConstant(
            method = "vertex(DDD)V",
            constant = @Constant(intValue = 7, ordinal = 0)
    )
    private int smoothbeta_prohibitExtraVertices(int constant) {
        return smoothbeta_renderingTerrain ? -1 : constant;
    }
}
