package net.mine_diver.smoothbeta.mixin.client.multidraw;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.mine_diver.smoothbeta.client.render.SmoothChunkRenderer;
import net.mine_diver.smoothbeta.client.render.SmoothTessellator;
import net.mine_diver.smoothbeta.client.render.SmoothWorldRenderer;
import net.mine_diver.smoothbeta.client.render.VboPool;
import net.mine_diver.smoothbeta.client.render.gl.VertexBuffer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.world.RenderChunk;
import net.minecraft.world.WorldRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashSet;

@Mixin(RenderChunk.class)
class ChunkRendererMixin implements SmoothChunkRenderer {
    @Shadow private static BufferBuilder BUFFER_BUILDER;

    @Shadow public boolean[] blocks;
    @Shadow public int regionX;
    @Shadow public int regionY;
    @Shadow public int regionZ;
    @Unique
    private VertexBuffer[] smoothbeta_buffers;
    @Unique
    private int smoothbeta_currentBufferIndex = -1;

    @Override
    @Unique
    public VertexBuffer smoothbeta_getBuffer(int pass) {
        return smoothbeta_buffers[pass];
    }

    @Override
    @Unique
    public VertexBuffer smoothbeta_getCurrentBuffer() {
        return smoothbeta_buffers[smoothbeta_currentBufferIndex];
    }

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void smoothbeta_init(CallbackInfo ci) {
        smoothbeta_buffers = new VertexBuffer[blocks.length];
        //noinspection deprecation
        VboPool pool = ((SmoothWorldRenderer) ((Minecraft) FabricLoader.getInstance().getGameInstance()).worldRenderer).smoothbeta_getTerrainVboPool();
        for (int i = 0; i < smoothbeta_buffers.length; i++)
            smoothbeta_buffers[i] = new VertexBuffer(pool);
    }

    @Inject(
            method = "compile",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;start()V"
            )
    )
    private void smoothbeta_startRenderingTerrain(
            CallbackInfo ci,
            @Local(index = 11) int renderLayer
    ) {
        smoothbeta_currentBufferIndex = renderLayer;
        ((SmoothTessellator) BUFFER_BUILDER).smoothbeta_startRenderingTerrain(this);
    }

    @Inject(
            method = "compile",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;offset(DDD)V",
                    shift = At.Shift.AFTER,
                    ordinal = 0
            )
    )
    private void smoothbeta_offsetBufferData(CallbackInfo ci) {
        BUFFER_BUILDER.addOffset(this.regionX, this.regionY, this.regionZ);
    }

    @Inject(
            method = "compile",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;end()I",
                    shift = At.Shift.AFTER
            )
    )
    private void smoothbeta_stopRenderingTerrain(CallbackInfo ci) {
        smoothbeta_currentBufferIndex = -1;
        ((SmoothTessellator) BUFFER_BUILDER).smoothbeta_stopRenderingTerrain();
    }
}
