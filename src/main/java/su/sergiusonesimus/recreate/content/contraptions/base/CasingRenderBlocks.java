package su.sergiusonesimus.recreate.content.contraptions.base;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import team.chisel.ctmlib.RenderBlocksCTM;
import team.chisel.ctmlib.TextureSubmap;

public class CasingRenderBlocks extends RenderBlocksCTM {

    public TextureSubmap submapAdditional;

    public CasingRenderBlocks() {
        this.ctm = CasingCTM.getInstance(this);
    }

    @Override
    protected void side(Block block, SubSide side, int iconIndex) {

        IIcon icon;
        TextureSubmap map;
        if (iconIndex >= 20) {
            iconIndex -= 20;
            map = submapAdditional;
        } else if (iconIndex >= 16) {
            iconIndex -= 16;
            map = submapSmall;
        } else {
            map = submap;
        }

        if (map == null) {
            try {
                Field normal = SubSide.class.getDeclaredField("normal");
                normal.setAccessible(true);
                icon = getBlockIconFromSideAndMetadata(block, ((ForgeDirection) normal.get(side)).ordinal(), meta);
            } catch (Exception e) {
                icon = null;
                e.printStackTrace();
            }
        } else {
            int x = iconIndex % map.getWidth();
            int y = iconIndex / map.getHeight();
            icon = map.getSubIcon(x, y);
        }

        double umax = icon.getMaxU();
        double umin = icon.getMinU();
        double vmax = icon.getMaxV();
        double vmin = icon.getMinV();

        minU = umin;
        maxU = umax;
        minV = vmin;
        maxV = vmax;

        try {
            Method render = SubSide.class.getDeclaredMethod("render", RenderBlocksCTM.class);
            render.setAccessible(true);
            render.invoke(side, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
