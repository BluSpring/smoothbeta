package net.mine_diver.smoothbeta.mixin.client.vbos;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin {
    @Shadow private static boolean f_6972226; // tryVBOs

    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;<init>(I)V"))
    private static void zorrow$forceEnableVBOs(CallbackInfo ci) {
        f_6972226 = true;
        //convertQuadsToTriangles = true;
    }

    @Redirect(method = "<init>(I)V", at = @At(value = "FIELD", target = "Lorg/lwjgl/opengl/ContextCapabilities;GL_ARB_vertex_buffer_object:Z"))
    private boolean zorrow$checkSupportsGL15Vbos(ContextCapabilities instance) {
        return instance.OpenGL15;
    }

    @Redirect(method = "<init>(I)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/ARBVertexBufferObject;glGenBuffersARB(Ljava/nio/IntBuffer;)V"))
    private void zorrow$useGl15GenBuffers(IntBuffer intBuffer) {
        GL15.glGenBuffers(intBuffer);
    }

    @Redirect(method = "end", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/ARBVertexBufferObject;glBindBufferARB(II)V"))
    private void zorrow$useGl15VboBindBuffer(int i, int j) {
        GL15.glBindBuffer(i, j);
    }

    @Redirect(method = "end", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/ARBVertexBufferObject;glBufferDataARB(ILjava/nio/ByteBuffer;I)V"))
    private void zorrow$useGl15VboBufferData(int i, ByteBuffer byteBuffer, int j) {
        GL15.glBufferData(i, byteBuffer, j);
    }

    @ModifyArg(method = "end", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glNormalPointer(IIJ)V"), index = 0)
    private int zorrow$useSignedByteType(int type) {
        return GL11.GL_BYTE;
    }
}
