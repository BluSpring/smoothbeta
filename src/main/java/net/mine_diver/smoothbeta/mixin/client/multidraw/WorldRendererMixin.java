package net.mine_diver.smoothbeta.mixin.client.multidraw;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.MemoryTracker;
import net.mine_diver.smoothbeta.client.render.*;
import net.minecraft.client.render.world.RenderChunk;
import net.minecraft.client.render.world.RenderChunkStorage;
import net.minecraft.client.render.world.WorldRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.FloatBuffer;

@Mixin(WorldRenderer.class)
abstract class WorldRendererMixin implements SmoothWorldRenderer {
    @Shadow private RenderChunkStorage[] renderStages;

    @Unique
    private VboPool smoothbeta_vboPool;

    @Override
    @Unique
    public VboPool smoothbeta_getTerrainVboPool() {
        return smoothbeta_vboPool;
    }

    @Inject(
            method = "m_6748042()V",
            at = @At("HEAD")
    )
    private void smoothbeta_resetVboPool(CallbackInfo ci) {
        if (smoothbeta_vboPool != null)
            smoothbeta_vboPool.deleteGlBuffers();
        smoothbeta_vboPool = new VboPool(VertexFormats.POSITION_TEXTURE_COLOR_NORMAL_LIGHT);
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "()Lnet/minecraft/client/render/world/RenderChunkStorage;"
            )
    )
    private RenderChunkStorage smoothbeta_injectRenderRegion() {
        return new RenderRegion((WorldRenderer) (Object) this);
    }

    @Inject(
            method = "render(IIID)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/world/RenderChunkStorage;addNewIdToGlList(I)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void smoothbeta_addBufferToRegion(int renderStartIndex, int chunksToRender, int renderStage, double tickDelta, CallbackInfoReturnable<Integer> cir, @Local(index = 17) int stageIdx, @Local(index = 16) RenderChunk renderChunk) {
        ((RenderRegion) this.renderStages[stageIdx]).addBuffer(((SmoothChunkRenderer) renderChunk).smoothbeta_getBuffer(renderStage));
    }

    @Redirect(
            method = "render(IIID)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/world/RenderChunkStorage;addNewIdToGlList(I)V"
            )
    )
    private void smoothbeta_stopCallingRenderList(RenderChunkStorage instance, int i) {}

    @Unique
    private final FloatBuffer
            smoothbeta_modelViewMatrix = MemoryTracker.createFloatBuffer(16),
            smoothbeta_projectionMatrix = MemoryTracker.createFloatBuffer(16);

    @Inject(
            method = "render(ID)V",
            at = @At("HEAD")
    )
    public void smoothbeta_beforeRenderRegion(int d, double par2, CallbackInfo ci) {
        Shader shader = Shaders.getTerrainShader();

        shader.addSampler("Sampler0", 0);
        shader.addSampler("Sampler1", 0);

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, smoothbeta_modelViewMatrix.clear());
        shader.modelViewMat.set(smoothbeta_modelViewMatrix.position(0));

        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, smoothbeta_projectionMatrix.clear());
        shader.projectionMat.set(smoothbeta_projectionMatrix.position(0));

        shader.fogMode.set(switch (GL11.glGetInteger(GL11.GL_FOG_MODE)) {
            case GL11.GL_EXP -> 0;
            case GL11.GL_EXP2 -> 1;
            case GL11.GL_LINEAR -> 2;
            default -> throw new IllegalStateException("Unexpected value: " + GL11.glGetInteger(GL11.GL_FOG_MODE));
        });

        shader.bind();
    }

    @Inject(
            method = "render(ID)V",
            at = @At("RETURN")
    )
    public void smoothbeta_afterRenderRegion(int d, double par2, CallbackInfo ci) {
        Shaders.getTerrainShader().unbind();

        GL20.glDisableVertexAttribArray(0); // pos
        GL20.glDisableVertexAttribArray(1); // texture
        GL20.glDisableVertexAttribArray(2); // color
        GL20.glDisableVertexAttribArray(3); // normal
        GL20.glDisableVertexAttribArray(4); // lightmap

        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
