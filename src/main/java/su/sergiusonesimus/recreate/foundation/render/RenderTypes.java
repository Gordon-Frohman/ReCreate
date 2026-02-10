package su.sergiusonesimus.recreate.foundation.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.recreate.AllSpecialTextures;

public class RenderTypes {

    public static void setupOutlineSolid() {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(AllSpecialTextures.BLANK.getLocation());
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        setupLightmapAndOverlay();
    }

    public static void setupOutlineTranslucent(ResourceLocation texture, boolean cull) {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(texture);
        if (cull) GL11.glEnable(GL11.GL_CULL_FACE);
        else GL11.glDisable(GL11.GL_CULL_FACE);

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        setupLightmapAndOverlay();
    }

    public static void setupGlowingSolid(ResourceLocation texture) {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(texture);
        GL11.glDisable(GL11.GL_BLEND);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void setupGlowingSolidDefault() {
        setupGlowingSolid(new ResourceLocation("textures/atlas/blocks.png"));
    }

    public static void setupGlowingTranslucent(ResourceLocation texture) {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(texture);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void setupGlowingTranslucentDefault() {
        setupGlowingTranslucent(new ResourceLocation("textures/atlas/blocks.png"));
    }

    public static void setupItemPartial(boolean translucent) {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(new ResourceLocation("textures/atlas/blocks.png"));
        if (translucent) {
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        } else {
            GL11.glDisable(GL11.GL_BLEND);
        }
        setupLightmapAndOverlay();
    }

    private static void setupLightmapAndOverlay() {
        int lastX = (int) (OpenGlHelper.lastBrightnessX);
        int lastY = (int) (OpenGlHelper.lastBrightnessY);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lastX, (float) lastY);
    }

    public static void cleanUp() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

}
