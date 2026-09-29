package su.sergiusonesimus.recreate.foundation.gui.element;

import com.cleanroommc.modularui.drawable.UITexture;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface ScreenElement {

    @SideOnly(Side.CLIENT)
    void render(int x, int y);

    public UITexture asUITexture();

}
