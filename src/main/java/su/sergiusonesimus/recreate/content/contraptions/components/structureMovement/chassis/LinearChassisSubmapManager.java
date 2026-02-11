package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import team.chisel.ctmlib.ISubmapManager;
import team.chisel.ctmlib.RenderBlocksCTM;
import team.chisel.ctmlib.TextureSubmap;

/**
 * A convenience implementation of {@link ISubmapManager} which does the standard CTM behavior.
 */
public class LinearChassisSubmapManager implements ISubmapManager {

    private static String chassisSide1Name = "linear_chassis_side";
    public static TextureSubmap chassisSide1;
    public static TextureSubmap chassisSide1Small;

    private static String chassisSide2Name = "secondary_linear_chassis_side";
    public static TextureSubmap chassisSide2;
    public static TextureSubmap chassisSide2Small;

    private static String chassisEndName = "linear_chassis_end";
    public static TextureSubmap chassisEnd;
    public static TextureSubmap chassisEndSmall;

    private static String chassisEndStickyName = "linear_chassis_end_sticky";
    public static TextureSubmap chassisEndSticky;
    public static TextureSubmap chassisEndStickySmall;

    @SideOnly(Side.CLIENT)
    private static final ThreadLocal<RenderBlocksCTM> renderBlocksThreadLocal = ThreadLocal
        .withInitial(LinearChassisRenderBlocks::new);

    @Override
    public IIcon getIcon(int side, int meta) {
        Axis axis;
        switch (meta >> 2) {
            default:
            case 0:
                axis = Axis.Y;
                break;
            case 1:
                axis = Axis.X;
                break;
            case 2:
                axis = Axis.Z;
                break;
        }
        if (Direction.from3DDataValue(side)
            .getAxis() == axis) {
            return LinearChassisSubmapManager.chassisEndSmall.getBaseIcon();
        } else {
            if ((meta & 3) == 1) return LinearChassisSubmapManager.chassisSide2Small.getBaseIcon();
            else return LinearChassisSubmapManager.chassisSide1Small.getBaseIcon();
        }
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        if (!(world.getBlock(x, y, z) instanceof LinearChassisBlock chassis)) return getIcon(side, meta);
        Direction face = Direction.from3DDataValue(side);
        Boolean sticky = chassis.getGlueableSide(world, x, y, z, face);
        if (sticky != null && sticky) return LinearChassisSubmapManager.chassisEndStickySmall.getBaseIcon();
        else return getIcon(side, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(String modName, Block block, IIconRegister register) {
        chassisSide1 = new TextureSubmap(register.registerIcon(modName + ":" + chassisSide1Name + "-ctm"), 4, 4);
        chassisSide1Small = new TextureSubmap(register.registerIcon(modName + ":" + chassisSide1Name), 2, 2);
        chassisSide2 = new TextureSubmap(register.registerIcon(modName + ":" + chassisSide2Name + "-ctm"), 4, 4);
        chassisSide2Small = new TextureSubmap(register.registerIcon(modName + ":" + chassisSide2Name), 2, 2);
        chassisEnd = new TextureSubmap(register.registerIcon(modName + ":" + chassisEndName + "-ctm"), 4, 4);
        chassisEndSmall = new TextureSubmap(register.registerIcon(modName + ":" + chassisEndName), 2, 2);
        chassisEndSticky = new TextureSubmap(
            register.registerIcon(modName + ":" + chassisEndStickyName + "-ctm"),
            4,
            4);
        chassisEndStickySmall = new TextureSubmap(register.registerIcon(modName + ":" + chassisEndStickyName), 2, 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RenderBlocks createRenderContext(RenderBlocks rendererOld, Block block, IBlockAccess world) {
        return renderBlocksThreadLocal.get();
    }

    @Override
    public void preRenderSide(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, ForgeDirection side) {}

    @Override
    public void postRenderSide(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, ForgeDirection side) {}
}
