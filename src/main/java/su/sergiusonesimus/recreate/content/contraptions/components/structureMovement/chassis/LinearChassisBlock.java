package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.ReCreate;

public class LinearChassisBlock extends AbstractChassisBlock {

    public static IIcon chassisEnd;
    public static IIcon chassisEndSticky;
    public static IIcon chassisSide1;
    public static IIcon chassisSide2;

    public LinearChassisBlock(Material material) {
        super(material);
    }

    private static Direction placeSide;

    @Override
    public int onBlockPlaced(World worldIn, int x, int y, int z, int side, float subX, float subY, float subZ,
        int meta) {
        placeSide = Direction.from3DDataValue(side);
        return super.onBlockPlaced(worldIn, x, y, z, side, subX, subY, subZ, meta);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        int type = itemIn.getItemDamage() == 1 ? 1 : 0;
        ChunkCoordinates normal = placeSide.getOpposite()
            .getNormal();
        Block neighbourBlock = worldIn.getBlock(x + normal.posX, y + normal.posY, z + normal.posZ);
        int neighbourMeta = worldIn.getBlockMetadata(x + normal.posX, y + normal.posY, z + normal.posZ);

        if (placer == null || !placer.isSneaking()) {
            if (isChassis(neighbourBlock)) {
                worldIn.setBlockMetadataWithNotify(x, y, z, neighbourMeta & 12 | type, 3);
                return;
            }
            worldIn.setBlockMetadataWithNotify(
                x,
                y,
                z,
                this.getMetaFromDirection(Direction.getNearestLookingDirection(placer)) + type,
                3);
            return;
        }
        worldIn.setBlockMetadataWithNotify(x, y, z, worldIn.getBlockMetadata(x, y, z) + type, 3);
    }

    @Override
    public Boolean getGlueableSide(IBlockAccess worldIn, int x, int y, int z, Direction face) {
        int meta = worldIn.getBlockMetadata(x, y, z);
        if (face.getAxis() != getAxis(meta)) return null;
        return ((ChassisTileEntity) worldIn.getTileEntity(x, y, z)).getGlueableSide(face);
    }

    @Override
    public void setGlueableSide(IBlockAccess worldIn, int x, int y, int z, Direction face, boolean value) {
        int meta = worldIn.getBlockMetadata(x, y, z);
        if (face.getAxis() != getAxis(meta)) return;
        ((ChassisTileEntity) worldIn.getTileEntity(x, y, z)).setGlueableSide(face, value);
    }

    @Override
    protected boolean glueAllowedOnSide(IBlockAccess world, int x, int y, int z, Direction side) {
        ChunkCoordinates normal = side.getNormal();
        int meta = world.getBlockMetadata(x, y, z);
        Block other = world.getBlock(x + normal.posX, y + normal.posY, z + normal.posZ);
        int otherMeta = world.getBlockMetadata(x + normal.posX, y + normal.posY, z + normal.posZ);
        return !sameKind(other, otherMeta, this, meta)
            || getAxis(meta) != ((LinearChassisBlock) other).getAxis(otherMeta);
    }

    public static boolean isChassis(Block block) {
        return block == AllBlocks.linear_chassis;
    }

    public static boolean sameKind(Block block1, int meta1, Block block2, int meta2) {
        return block1 == block2 && (meta1 % 4) == (meta2 % 4);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess worldIn, int x, int y, int z, int side) {
        Boolean stickySide = this.getGlueableSide(worldIn, x, y, z, Direction.from3DDataValue(side));
        if (stickySide != null && stickySide) return chassisEndSticky;
        return super.getIcon(worldIn, x, y, z, side);
    }

    @Override
    protected IIcon getSideIcon(int meta) {
        return (meta & 3) == 1 ? chassisSide2 : chassisSide1;
    }

    @Override
    protected IIcon getTopIcon(int meta) {
        return chassisEnd;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        chassisEnd = reg.registerIcon(ReCreate.ID + ":linear_chassis_end");
        chassisEndSticky = reg.registerIcon(ReCreate.ID + ":linear_chassis_end_sticky");
        chassisSide1 = reg.registerIcon(ReCreate.ID + ":linear_chassis_side");
        chassisSide2 = reg.registerIcon(ReCreate.ID + ":secondary_linear_chassis_side");
    }

    public int damageDropped(int meta) {
        return meta & 3;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
        list.add(new ItemStack(itemIn, 1, 0));
        list.add(new ItemStack(itemIn, 1, 1));
    }

    // TODO
    // public static class ChassisCTBehaviour extends ConnectedTextureBehaviour {
    //
    // @Override
    // public CTSpriteShiftEntry get(BlockState state, Direction direction) {
    // Block block = state.getBlock();
    // BooleanProperty glueableSide = ((LinearChassisBlock) block).getGlueableSide(state, direction);
    // if (glueableSide == null)
    // return AllBlocks.LINEAR_CHASSIS.has(state) ? AllSpriteShifts.CHASSIS_SIDE
    // : AllSpriteShifts.SECONDARY_CHASSIS_SIDE;
    // return state.getValue(glueableSide) ? AllSpriteShifts.CHASSIS_STICKY : AllSpriteShifts.CHASSIS;
    // }
    //
    // @Override
    // protected Direction getUpDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
    // Axis axis = state.getValue(AXIS);
    // if (face.getAxis() == axis)
    // return super.getUpDirection(reader, pos, state, face);
    // return Direction.get(AxisDirection.POSITIVE, axis);
    // }
    //
    // @Override
    // protected Direction getRightDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face)
    // {
    // Axis axis = state.getValue(AXIS);
    // return axis != face.getAxis() && axis.isHorizontal() ? (face.getAxis()
    // .isHorizontal() ? Direction.DOWN : (axis == Axis.X ? Direction.NORTH : Direction.EAST))
    // : super.getRightDirection(reader, pos, state, face);
    // }
    //
    // @Override
    // protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
    // Axis axis = state.getValue(AXIS);
    // boolean side = face.getAxis() != axis;
    // if (side && axis == Axis.X && face.getAxis()
    // .isHorizontal())
    // return true;
    // return super.reverseUVsHorizontally(state, face);
    // }
    //
    // @Override
    // protected boolean reverseUVsVertically(BlockState state, Direction face) {
    // return super.reverseUVsVertically(state, face);
    // }
    //
    // @Override
    // public boolean reverseUVs(BlockState state, Direction face) {
    // Axis axis = state.getValue(AXIS);
    // boolean end = face.getAxis() == axis;
    // if (end && axis.isHorizontal() && (face.getAxisDirection() == AxisDirection.POSITIVE))
    // return true;
    // if (!end && axis.isHorizontal() && face == Direction.DOWN)
    // return true;
    // return super.reverseUVs(state, face);
    // }
    //
    // @Override
    // public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
    // BlockPos otherPos, Direction face) {
    // Axis axis = state.getValue(AXIS);
    // boolean superConnect = face.getAxis() == axis ? super.connectsTo(state, other, reader, pos, otherPos, face)
    // : sameKind(state, other);
    // return superConnect && axis == other.getValue(AXIS);
    // }
    //
    // }

}
