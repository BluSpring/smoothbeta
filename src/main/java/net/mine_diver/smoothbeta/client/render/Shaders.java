package net.mine_diver.smoothbeta.client.render;

import net.fabricmc.api.ClientModInitializer;
import net.mine_diver.smoothbeta.client.render.gl.Program;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Shaders implements ClientModInitializer {
    private static Shader terrainShader;

    @Override
    public void onInitializeClient() {
    }

    public static void initShaders() {
        applyShader(loadShaders());
    }

    private record Application(
            Runnable clearCache,
            Supplier<Shader> shaderFactory
    ) {}

    private static Application loadShaders() {
        //Profiler.push("cache_release");
        List<Program> list = new ArrayList<>();
        list.addAll(Program.Type.FRAGMENT.getProgramCache().values());
        list.addAll(Program.Type.VERTEX.getProgramCache().values());

        //profiler.swap("shader_factory");
        Supplier<Shader> shaderFactory = () -> {
            try {
                return new Shader("terrain", VertexFormats.POSITION_TEXTURE_COLOR_NORMAL_LIGHT);
            } catch (IOException e) {
                throw new RuntimeException("Could not reload terrain shader", e);
            }
        };

        //profiler.pop();
        //profiler.endTick();
        return new Application(() -> list.forEach(Program::release), shaderFactory);
    }

    private static void applyShader(Application application) {
        //profiler.push("cache_release");
        application.clearCache.run();

        if (terrainShader != null) {
            //profiler.swap("delete_shader");
            terrainShader.close();
        }

        //profiler.swap("load_shader");
        terrainShader = application.shaderFactory.get();

        //profiler.pop();
        //profiler.endTick();
    }

    public static Shader getTerrainShader() {
        return terrainShader;
    }
}
