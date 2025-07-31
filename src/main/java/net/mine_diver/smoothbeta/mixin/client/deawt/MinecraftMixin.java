package net.mine_diver.smoothbeta.mixin.client.deawt;

import net.mine_diver.smoothbeta.client.DeAWTMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Mouse;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;setTitle(Ljava/lang/String;)V",
            remap = false,
            shift = At.Shift.AFTER
        )
    )
    private void setupDisplay(CallbackInfo ci) {
        ByteBuffer[] icons = new ByteBuffer[2];

        try {
            icons[0] = loadIcon("/assets/deawt/icons/16.png");
            icons[1] = loadIcon("/assets/deawt/icons/32.png");
        } catch (Exception ignored) {}

        if(icons[0] != null && icons[1] != null){
            Display.setIcon(icons);
        }

        if (Display.getTitle().contains("Minecraft Minecraft")) {
            Display.setTitle(Display.getTitle().replaceFirst("Minecraft ", ""));
        }

        Display.setResizable(true);
    }

    /* For some reason this method uses the AWT canvas. Yuck. */
    @Redirect(method = "grabMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;release()V"))
    private void redirectReleaseMouse(Mouse instance) {
        org.lwjgl.input.Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
        org.lwjgl.input.Mouse.setGrabbed(false);
    }

    /**
     * Completely yoinked from <a href="https://modrinth.com/mod/gambac">Gambac</a>
     * Credits to DanyGames2014
     */
    @Unique
    private ByteBuffer loadIcon(String path) {
        try {
            InputStream stream = DeAWTMinecraft.class.getResourceAsStream(path);
            if (stream == null) {
                throw new RuntimeException("Icon resource not found: " + path);
            }
            BufferedImage image = ImageIO.read(stream);

            int[] pixels = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

            ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = pixels[y * image.getWidth() + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                    buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
                    buffer.put((byte) (pixel & 0xFF));         // Blue
                    buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
                }
            }

            buffer.flip();
            return buffer;
        } catch (Exception e) {
            return null;
        }
    }
}