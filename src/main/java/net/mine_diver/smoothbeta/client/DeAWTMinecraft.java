package net.mine_diver.smoothbeta.client;

import net.mine_diver.smoothbeta.mixin.client.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftApplet;
import net.minecraft.client.crash.CrashSummary;
import net.minecraft.client.render.Window;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Most of this stuff was ported from <a href="https://modrinth.com/mod/gambac">Gambac</a>
 * Credits to DanyGames2014
 * Also ported from <a href="https://github.com/kimoVoid/deAWT/">deAWT</a>
 */
public class DeAWTMinecraft extends Minecraft {
    private final int previousWidth;
    private final int previousHeight;

    public DeAWTMinecraft(int width, int height, boolean fullscreen) {
        super(null, null, null, width, height, fullscreen);
        this.previousWidth = width;
        this.previousHeight = height;
    }

    @Override
    public void printCrashReport(CrashSummary summary) {
        StringWriter var2 = new StringWriter();
        summary.cause.printStackTrace(new PrintWriter(var2));
        String var3 = var2.toString();
        String var4 = "";
        String var5 = "";

        try {
            var5 = var5 + "Generated " + new SimpleDateFormat().format(new Date()) + "\n";
            var5 = var5 + "\n";
            var5 = var5 + "Minecraft: Minecraft 1.0.0\n";
            var5 = var5 + "OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version") + "\n";
            var5 = var5 + "Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + "\n";
            var5 = var5 + "VM: " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor") + "\n";
            var5 = var5 + "LWJGL: " + Sys.getVersion() + "\n";
            var4 = GL11.glGetString(7936);
            var5 = var5 + "OpenGL: " + GL11.glGetString(7937) + " version " + GL11.glGetString(7938) + ", " + GL11.glGetString(7936) + "\n";
        } catch (Throwable var8) {
            var5 = var5 + "[failed to get system properties (" + var8 + ")]\n";
        }

        var5 = var5 + "\n";
        var5 = var5 + var3;
        String var6 = "";
        var6 = var6 + "\n";
        var6 = var6 + "\n";
        if (var3.contains("Pixel format not accelerated")) {
            var6 = var6 + "      Bad video card drivers!      \n";
            var6 = var6 + "      -----------------------      \n";
            var6 = var6 + "\n";
            var6 = var6 + "Minecraft was unable to start because it failed to find an accelerated OpenGL mode.\n";
            var6 = var6 + "This can usually be fixed by updating the video card drivers.\n";
            if (var4.toLowerCase().contains("nvidia")) {
                var6 = var6 + "\n";
                var6 = var6 + "You might be able to find drivers for your video card here:\n";
                var6 = var6 + "  http://www.nvidia.com/\n";
            } else if (var4.toLowerCase().contains("ati")) {
                var6 = var6 + "\n";
                var6 = var6 + "You might be able to find drivers for your video card here:\n";
                var6 = var6 + "  http://www.amd.com/\n";
            }
        } else {
            var6 = var6 + "      Minecraft has crashed!      \n";
            var6 = var6 + "      ----------------------      \n";
            var6 = var6 + "\n";
            var6 = var6 + "Minecraft has stopped running because it encountered a problem.\n";
            var6 = var6 + "\n";
            var6 = var6 + "If you wish to report this, please copy this entire text and email it to support@mojang.com.\n";
            var6 = var6 + "Please include a description of what you did when the error occured.\n";
        }

        var6 = var6 + "\n";
        var6 = var6 + "\n";
        var6 = var6 + "\n";
        var6 = var6 + "--- BEGIN ERROR REPORT " + Integer.toHexString(var6.hashCode()) + " --------\n";
        var6 = var6 + var5;
        var6 = var6 + "--- END ERROR REPORT " + Integer.toHexString(var6.hashCode()) + " ----------\n";
        var6 = var6 + "\n";
        var6 = var6 + "\n";

        System.out.println(var6);

        this.stop();
        System.exit(0);
    }

    @Override
    public void tick() {
        if (GL11.glGetString(GL11.GL_RENDERER).contains("Apple M")) {
            GL11.glEnable(GL30.GL_FRAMEBUFFER_SRGB);
        }
        if (Display.getWidth() != this.width || Display.getHeight() != this.height) {
            this.onResolutionChanged(Display.getWidth(), Display.getHeight());
        }

        super.tick();
    }

    @Override
    public void toggleFullscreen() {
        boolean isFullscreen = ((MinecraftAccessor) this).isFullscreen();
        try {
            isFullscreen = !isFullscreen;
            if (isFullscreen) {
                this.width = Display.getWidth();
                this.height = Display.getHeight();

                Display.setDisplayMode(Display.getDesktopDisplayMode());
                this.width = Display.getDisplayMode().getWidth();
                this.height = Display.getDisplayMode().getHeight();
            } else {
                this.width = this.previousWidth;
                this.height = this.previousHeight;
                Display.setDisplayMode(new DisplayMode(this.width, this.height));
            }
            if (this.width <= 0) {
                this.width = 1;
            }
            if (this.height <= 0) {
                this.height = 1;
            }

            if (this.screen != null) {
                this.onResolutionChanged(this.width, this.height);
            }

            Display.setFullscreen(isFullscreen);
            Display.update();
        } catch (Exception ignored) {}

        ((MinecraftAccessor) this).setFullscreen(isFullscreen);
    }

    private void onResolutionChanged(int w, int h) {
        if (w <= 0) {
            w = 1;
        }
        if (h <= 0) {
            h = 1;
        }
        this.width = w;
        this.height = h;
        if (this.screen != null) {
            Window scaled = new Window(this.options, w, h);
            int scaledWidth = scaled.getWidth();
            int scaledHeight = scaled.getHeight();
            this.screen.init(this, scaledWidth, scaledHeight);
        }
    }
}
