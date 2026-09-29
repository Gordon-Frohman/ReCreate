package su.sergiusonesimus.recreate.foundation.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.drawable.UITexture;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.foundation.gui.element.DelegatedStencilElement;
import su.sergiusonesimus.recreate.foundation.gui.element.ScreenElement;
import su.sergiusonesimus.recreate.foundation.utility.Color;

public class AllIcons implements ScreenElement {

    public static final ResourceLocation ICON_ATLAS = ReCreate.asResource("textures/gui/icons.png");
    public static final int ICON_ATLAS_SIZE = 256;

    private static int x = 0, y = -1;
    private int iconX;
    private int iconY;

    public static final AllIcons I_ADD = newRow(), I_TRASH = next(), I_3x3 = next(), I_TARGET = next(),
        I_PRIORITY_VERY_LOW = next(), I_PRIORITY_LOW = next(), I_PRIORITY_HIGH = next(), I_PRIORITY_VERY_HIGH = next(),
        I_BLACKLIST = next(), I_WHITELIST = next(), I_WHITELIST_OR = next(), I_WHITELIST_AND = next(),
        I_WHITELIST_NOT = next(), I_RESPECT_NBT = next(), I_IGNORE_NBT = next();

    public static final AllIcons I_CONFIRM = newRow(), I_NONE = next(), I_OPEN_FOLDER = next(), I_REFRESH = next(),
        I_ACTIVE = next(), I_PASSIVE = next(), I_ROTATE_PLACE = next(), I_ROTATE_PLACE_RETURNED = next(),
        I_ROTATE_NEVER_PLACE = next(), I_MOVE_PLACE = next(), I_MOVE_PLACE_RETURNED = next(),
        I_MOVE_NEVER_PLACE = next(), I_CART_ROTATE = next(), I_CART_ROTATE_PAUSED = next(),
        I_CART_ROTATE_LOCKED = next();

    public static final AllIcons I_DONT_REPLACE = newRow(), I_REPLACE_SOLID = next(), I_REPLACE_ANY = next(),
        I_REPLACE_EMPTY = next(), I_CENTERED = next(), I_ATTACHED = next(), I_INSERTED = next(), I_FILL = next(),
        I_PLACE = next(), I_REPLACE = next(), I_CLEAR = next(), I_OVERLAY = next(), I_FLATTEN = next(), I_LMB = next(),
        I_SCROLL = next(), I_RMB = next();

    public static final AllIcons I_TOOL_DEPLOY = newRow(), I_SKIP_MISSING = next(), I_SKIP_TILES = next(),
        I_DICE = next(), I_TUNNEL_SPLIT = next(), I_TUNNEL_FORCED_SPLIT = next(), I_TUNNEL_ROUND_ROBIN = next(),
        I_TUNNEL_FORCED_ROUND_ROBIN = next(), I_TUNNEL_PREFER_NEAREST = next(), I_TUNNEL_RANDOMIZE = next(),
        I_TUNNEL_SYNCHRONIZE = next(), I_TOOLBOX = next(),

        I_TOOL_MOVE_XZ = newRow(), I_TOOL_MOVE_Y = next(), I_TOOL_ROTATE = next(), I_TOOL_MIRROR = next(),
        I_ARM_ROUND_ROBIN = next(), I_ARM_FORCED_ROUND_ROBIN = next(), I_ARM_PREFER_FIRST = next(),

        I_ADD_INVERTED_ATTRIBUTE = next(), I_FLIP = next(),

        I_PLAY = newRow(), I_PAUSE = next(), I_STOP = next(), I_PLACEMENT_SETTINGS = next(), I_ROTATE_CCW = next(),
        I_HOUR_HAND_FIRST = next(), I_MINUTE_HAND_FIRST = next(), I_HOUR_HAND_FIRST_24 = next(),

        I_PATTERN_SOLID = newRow(), I_PATTERN_CHECKERED = next(), I_PATTERN_CHECKERED_INVERSED = next(),
        I_PATTERN_CHANCE_25 = next(),

        I_PATTERN_CHANCE_50 = newRow(), I_PATTERN_CHANCE_75 = next(), I_FOLLOW_DIAGONAL = next(),
        I_FOLLOW_MATERIAL = next(),

        I_SCHEMATIC = newRow(), I_SEQ_REPEAT = next(),

        I_MTD_LEFT = newRow(), I_MTD_CLOSE = next(), I_MTD_RIGHT = next(), I_MTD_SCAN = next(), I_MTD_REPLAY = next(),
        I_MTD_USER_MODE = next(), I_MTD_SLOW_MODE = next(),

        I_CONFIG_UNLOCKED = newRow(), I_CONFIG_LOCKED = next(), I_CONFIG_DISCARD = next(), I_CONFIG_SAVE = next(),
        I_CONFIG_RESET = next(), I_CONFIG_BACK = next(), I_CONFIG_PREV = next(), I_CONFIG_NEXT = next(),
        I_DISABLE = next(), I_CONFIG_OPEN = next(),

        I_FX_SURFACE_OFF = newRow(), I_FX_SURFACE_ON = next(), I_FX_FIELD_OFF = next(), I_FX_FIELD_ON = next(),
        I_FX_BLEND = next(), I_FX_BLEND_OFF = next();;

    public AllIcons(int x, int y) {
        iconX = x * 16;
        iconY = y * 16;
    }

    private static AllIcons next() {
        return new AllIcons(++x, y);
    }

    private static AllIcons newRow() {
        return new AllIcons(x = 0, ++y);
    }

    @SideOnly(Side.CLIENT)
    public void bind() {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(ICON_ATLAS);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(int x, int y) {
        bind();
        Gui.func_146110_a(x, y, iconX, iconY, 16, 16, 256, 256);
    }

    @SideOnly(Side.CLIENT)
    public void render(int x, int y, Gui gui) {
        bind();
        gui.drawTexturedModalRect(x, y, iconX, iconY, 16, 16);
    }

    @SideOnly(Side.CLIENT)
    public void render(int color) {
        bind();
        Tessellator tessellator = Tessellator.instance;
        Color rgb = new Color(color);

        float u1 = iconX * 1f / ICON_ATLAS_SIZE;
        float u2 = (iconX + 16) * 1f / ICON_ATLAS_SIZE;
        float v1 = iconY * 1f / ICON_ATLAS_SIZE;
        float v2 = (iconY + 16) * 1f / ICON_ATLAS_SIZE;

        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 255);
        tessellator.addVertexWithUV(0, 0, 0, u1, v1);
        tessellator.addVertexWithUV(0, 1, 0, u1, v2);
        tessellator.addVertexWithUV(1, 1, 0, u2, v2);
        tessellator.addVertexWithUV(1, 0, 0, u2, v1);
        tessellator.draw();
    }

    @SideOnly(Side.CLIENT)
    public DelegatedStencilElement asStencil() {
        return new DelegatedStencilElement().withStencilRenderer((w, h, alpha) -> this.render(0, 0))
            .withBounds(16, 16);
    }

    public UITexture asUITexture() {
        float u1 = (float) iconX / (float) ICON_ATLAS_SIZE;
        float u2 = (float) (iconX + 16) / (float) ICON_ATLAS_SIZE;
        float v1 = (float) iconY / (float) ICON_ATLAS_SIZE;
        float v2 = (float) (iconY + 16) / (float) ICON_ATLAS_SIZE;

        return new UITexture(ICON_ATLAS, u1, v1, u2, v2, null);
    }

}
