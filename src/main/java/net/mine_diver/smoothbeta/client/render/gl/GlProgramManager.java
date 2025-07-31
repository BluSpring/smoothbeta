package net.mine_diver.smoothbeta.client.render.gl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mine_diver.smoothbeta.SmoothBeta;
import org.lwjgl.opengl.GL20;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public class GlProgramManager {
	public static void useProgram(int program) {
		GL20.glUseProgram(program);
	}

	public static void deleteProgram(GlShader shader) {
		shader.getFragmentShader().release();
		shader.getVertexShader().release();
		GL20.glDeleteProgram(shader.getProgramRef());
	}

	public static int createProgram() throws IOException {
		int i = GL20.glCreateProgram();
		if (i <= 0) throw new IOException("Could not create shader program (returned program ID " + i + ")");
		else return i;
	}

	public static void linkProgram(GlShader shader) {
		shader.attachReferencedShaders();
		GL20.glLinkProgram(shader.getProgramRef());
		int i = GL20.glGetProgrami(shader.getProgramRef(), GL20.GL_LINK_STATUS);
		if (i == 0) {
			SmoothBeta.LOGGER.warn("Error encountered when linking program containing VS {} and FS {}. Log output:", shader.getVertexShader().getName(), shader.getFragmentShader().getName());
			SmoothBeta.LOGGER.warn(GL20.glGetProgramInfoLog(shader.getProgramRef(), 0x8000));
		}

	}
}