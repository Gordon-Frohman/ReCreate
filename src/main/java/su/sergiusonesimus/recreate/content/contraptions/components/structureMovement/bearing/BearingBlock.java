package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.base.DirectionalKineticBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.gearbox.GearboxBlock;

public abstract class BearingBlock extends DirectionalKineticBlock {

    public static IIcon bearingTop;
    public static IIcon bearingSide;

    public BearingBlock(Material materialIn) {
        super(materialIn);
        this.setStepSound(soundTypePiston);
        this.setHardness(1.5F);
        this.setResistance(10.0F);
    }

    @Override
    public boolean hasShaftTowards(IBlockAccess world, int x, int y, int z, Direction face) {
        return face == getDirection(world.getBlockMetadata(x, y, z)).getOpposite();
    }

    @Override
    public boolean showCapacityWithAnnotation() {
        return true;
    }

    @Override
    public boolean onWrenched(World world, int x, int y, int z, int face, EntityPlayer player) {
        boolean result = super.onWrenched(world, x, y, z, face, player);
        if (!world.isRemote && result) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof MechanicalBearingTileEntity) {
                ((MechanicalBearingTileEntity) te).disassemble();
            }
        }
        return result;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return ReCreate.proxy.getBearingBlockRenderID();
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        switch (side) {
            case 0:
                return GearboxBlock.gearboxSide;
            case 1:
                return bearingTop;
            default:
                return bearingSide;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        BearingBlock.bearingTop = iconRegister.registerIcon(ReCreate.ID + ":bearing_top");
        BearingBlock.bearingSide = iconRegister.registerIcon(ReCreate.ID + ":mechanical_bearing_side");
    }

    /**
     * Checks if the block is a solid face on the given side, used by placement logic.
     *
     * @param world The current world
     * @param x     X Position
     * @param y     Y position
     * @param z     Z position
     * @param side  The side to check
     * @return True if the block is solid on the specified side.
     */
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return this.getDirection(world.getBlockMetadata(x, y, z))
            .getOpposite()
            .toForgeDirection() != side;
    }
}
