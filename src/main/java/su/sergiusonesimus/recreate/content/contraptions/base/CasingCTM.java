package su.sergiusonesimus.recreate.content.contraptions.base;

import java.lang.reflect.Method;

import net.minecraft.world.IBlockAccess;

import su.sergiusonesimus.recreate.content.contraptions.base.CasingBlock.CasingType;
import team.chisel.ctmlib.CTM;
import team.chisel.ctmlib.Dir;

public class CasingCTM extends CTM {

    CasingRenderBlocks renderBlocks;

    public CasingCTM(CasingRenderBlocks renderBlocks) {
        this.renderBlocks = renderBlocks;
    }

    public static CTM getInstance(CasingRenderBlocks casingRenderBlocks) {
        return new CasingCTM(casingRenderBlocks);
    }

    @Override
    public int[] getSubmapIndices(IBlockAccess world, int x, int y, int z, int side) {
        int[] ret = new int[] { 18, 19, 17, 16 };

        if (world == null) {
            return ret;
        }

        CasingBlock block = (CasingBlock) world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);

        buildConnectionMap(world, x, y, z, side, block, meta);

        CasingType type = CasingBlock.getTypeFromMeta(meta);
        try {
            Method connectedOr = CTM.class.getDeclaredMethod("connectedOr", Dir[].class);
            connectedOr.setAccessible(true);
            if (((type == CasingType.ANDESITE || type == CasingType.BRASS) && connectedAnd(Dir.VALUES))
                || (type == CasingType.REFINED_RADIANCE && connectedAnd(Dir.TOP, Dir.LEFT, Dir.BOTTOM, Dir.RIGHT)
                    && !((boolean) connectedOr.invoke(
                        this,
                        (Object) new Dir[] { Dir.TOP_LEFT, Dir.TOP_RIGHT, Dir.BOTTOM_LEFT, Dir.BOTTOM_RIGHT })))) {
                return new int[] { 22, 23, 21, 20 };
            }

            // Map connections to submap indeces
            Method fillSubmaps = CTM.class.getDeclaredMethod("fillSubmaps", int[].class, int.class);
            fillSubmaps.setAccessible(true);
            for (int i = 0; i < 4; i++) {
                fillSubmaps.invoke(this, ret, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

}
