package net.mine_diver.smoothbeta.mixin.client.deawt;

import net.mine_diver.smoothbeta.client.DeAWTMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftApplet;
import net.minecraft.client.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.applet.Applet;

@Mixin(value = MinecraftApplet.class, priority = 999)
public class MinecraftAppletMixin extends Applet {

    @Shadow
    private Minecraft minecraft;

    /**
     * @author kimoVoid
     * @reason Replace AWT canvas
     */
    @Overwrite
    public void init() {
        boolean bl = false;
        if (this.getParameter("fullscreen") != null) {
            bl = this.getParameter("fullscreen").equalsIgnoreCase("true");
        }

        this.minecraft = new DeAWTMinecraft(getWidth(), getHeight(), bl);

        this.minecraft.f_3533455 = this.getDocumentBase().getHost();
        if (this.getDocumentBase().getPort() > 0) {
            this.minecraft.f_3533455 = this.minecraft.f_3533455 + ":" + this.getDocumentBase().getPort();
        }
        if (this.getParameter("username") != null && this.getParameter("sessionid") != null) {
            this.minecraft.session = new Session(this.getParameter("username"), this.getParameter("sessionid"));
            System.out.println("Setting user: " + this.minecraft.session.username + ", " + this.minecraft.session.sessionId);
            if (this.getParameter("mppass") != null) {
                this.minecraft.session.password = this.getParameter("mppass");
            }
        } else {
            this.minecraft.session = new Session("Player", "");
        }
        if (this.getParameter("server") != null && this.getParameter("port") != null) {
            this.minecraft.setServerAddressAndPort(this.getParameter("server"), Integer.parseInt(this.getParameter("port")));
        }

        this.minecraft.run();
    }
}