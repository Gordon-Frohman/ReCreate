package su.sergiusonesimus.recreate.foundation.utility;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class FluidHelper {

    /**
     * Calculates the real flow vector for any water (or lava) block.
     * Works for source, flowing, and falling liquids in MC 1.7.10.
     */
    public static Vec3 getFlowVector(IBlockAccess world, int x, int y, int z) {
        Vec3 vec = Vec3.createVectorHelper(0, 0, 0);

        Block block = world.getBlock(x, y, z);
        if (!(block instanceof BlockLiquid)) return vec;

        int meta = world.getBlockMetadata(x, y, z);
        int level = world.getBlockMetadata(x, y, z) & 7; // water level 0-7
        boolean falling = (meta & 8) != 0;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (dir == ForgeDirection.UP) continue;
            int nx = x + dir.offsetX;
            int ny = y + dir.offsetY;
            int nz = z + dir.offsetZ;
            Block neighbor = world.getBlock(nx, ny, nz);
            if (neighbor instanceof BlockLiquid && neighbor.getMaterial() == block.getMaterial()) {
                int nLevel = world.getBlockMetadata(nx, ny, nz) & 7;
                if (nLevel < level) {
                    vec = vec.addVector(
                        dir.offsetX * (level - nLevel),
                        dir.offsetY * (level - nLevel),
                        dir.offsetZ * (level - nLevel));
                }
            }
        }

        if (falling) {
            // Vanilla uses about -6.0 for falling water's Y vector
            vec = Vec3.createVectorHelper(vec.xCoord, vec.yCoord + 12.0, vec.zCoord);
        }

        return vec.normalize();
    }
}
