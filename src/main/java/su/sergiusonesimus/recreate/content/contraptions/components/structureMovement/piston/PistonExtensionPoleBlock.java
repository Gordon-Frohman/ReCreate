package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston;

import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import su.sergiusonesimus.recreate.content.contraptions.wrench.IWrenchable;
import su.sergiusonesimus.recreate.foundation.block.WrenchableDirectionalBlock;
import su.sergiusonesimus.recreate.foundation.utility.placement.IPlacementHelper;
import su.sergiusonesimus.recreate.foundation.utility.placement.PlacementHelpers;
import su.sergiusonesimus.recreate.foundation.utility.placement.util.PoleHelper;
import su.sergiusonesimus.recreate.util.BlockHelper;

public class PistonExtensionPoleBlock extends WrenchableDirectionalBlock implements IWrenchable {

    public static IIcon poleTop;
    public static IIcon poleSide;

    private static final int placementHelperId = PlacementHelpers.register(PlacementHelper.get());

    public PistonExtensionPoleBlock(Material material) {
        super(material);
        this.setHardness(1.5F);
        this.setResistance(5.0F);
        this.setStepSound(soundTypePiston);
    }

    @Override
    public boolean isToolEffective(String type, int metadata) {
        return type.equals("pickaxe") || type.equals("axe");
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public int getRenderType() {
        return ReCreate.proxy.getPistonExtensionPoleBlockRenderID();
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return getAxis(side) == getAxis(meta) ? poleTop : poleSide;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        poleTop = iconRegister.registerIcon(ReCreate.ID + ":pole_end");
        poleSide = iconRegister.registerIcon(ReCreate.ID + ":pole_side");
    }

    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getMobilityFlag() {
        return 0;
    }

    @Override
    public void onBlockHarvested(World worldIn, int x, int y, int z, int meta, EntityPlayer player) {
        Axis axis = getAxis(meta);
        Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
        ChunkCoordinates normal = direction.getNormal();
        ChunkCoordinates pistonHead = null;
        ChunkCoordinates pistonBase = null;

        for (int modifier : new int[] { 1, -1 }) {
            for (int offset = modifier; modifier * offset
                < MechanicalPistonBlock.maxAllowedPistonPoles(); offset += modifier) {
                int currentX = x + normal.posX * offset;
                int currentY = y + normal.posY * offset;
                int currentZ = z + normal.posZ * offset;
                Block block = worldIn.getBlock(currentX, currentY, currentZ);
                int localMeta = worldIn.getBlockMetadata(currentX, currentY, currentZ);

                if (MechanicalPistonBlock.isExtensionPole(block)
                    && axis == ((PistonExtensionPoleBlock) block).getAxis(localMeta)) continue;

                if (MechanicalPistonBlock.isPiston(block)
                    && axis == ((MechanicalPistonBlock) block).getDirection(localMeta)
                        .getAxis())
                    pistonBase = new ChunkCoordinates(currentX, currentY, currentZ);

                if (MechanicalPistonBlock.isPistonHead(block)
                    && axis == ((MechanicalPistonHeadBlock) block).getAxis(localMeta))
                    pistonHead = new ChunkCoordinates(currentX, currentY, currentZ);

                break;
            }
        }

        if (pistonHead != null && pistonBase != null
            && ((MechanicalPistonHeadBlock) worldIn.getBlock(pistonHead.posX, pistonHead.posY, pistonHead.posZ))
                .getDirection(worldIn.getBlockMetadata(pistonHead.posX, pistonHead.posY, pistonHead.posZ))
                == ((MechanicalPistonBlock) worldIn.getBlock(pistonBase.posX, pistonBase.posY, pistonBase.posZ))
                    .getDirection(worldIn.getBlockMetadata(pistonBase.posX, pistonBase.posY, pistonBase.posZ))) {

            int startX;
            int startY;
            int startZ;
            int endX;
            int endY;
            int endZ;
            if (pistonBase.posX < pistonHead.posX) {
                startX = pistonBase.posX;
                endX = pistonHead.posX;
            } else {
                startX = pistonHead.posX;
                endX = pistonBase.posX;
            }
            if (pistonBase.posY < pistonHead.posY) {
                startY = pistonBase.posY;
                endY = pistonHead.posY;
            } else {
                startY = pistonHead.posY;
                endY = pistonBase.posY;
            }
            if (pistonBase.posZ < pistonHead.posZ) {
                startZ = pistonBase.posZ;
                endZ = pistonHead.posZ;
            } else {
                startZ = pistonHead.posZ;
                endZ = pistonBase.posZ;
            }

            for (int tempX = startX; tempX <= endX; tempX++) {
                for (int tempY = startY; tempY <= endY; tempY++) {
                    for (int tempZ = startZ; tempZ <= endZ; tempZ++) {
                        if ((tempX != x || tempY != y || tempZ != z)
                            && (tempX != pistonBase.posX || tempY != pistonBase.posY || tempZ != pistonBase.posZ))
                            BlockHelper.breakBlock(worldIn, tempX, tempY, tempZ, !player.capabilities.isCreativeMode);
                    }
                }
            }

            if (worldIn.getTileEntity(
                pistonBase.posX,
                pistonBase.posY,
                pistonBase.posZ) instanceof MechanicalPistonTileEntity baseTE) {
                baseTE.state = PistonState.RETRACTED;
                baseTE.offset = 0;
                baseTE.onLengthBroken();
            }
        }

        super.onBlockHarvested(worldIn, x, y, z, meta, player);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        setBlockBoundsBasedOnState(worldIn, x, y, z);
        return super.getCollisionBoundingBoxFromPool(worldIn, x, y, z);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        double pixel = 1.0D / 16.0D;
        minX = minY = minZ = 6 * pixel;
        maxX = maxY = maxZ = 1.0D - 6 * pixel;
        switch (getAxis(worldIn.getBlockMetadata(x, y, z))) {
            case X:
                minX = 0.0D;
                maxX = 1.0D;
                break;
            case Y:
                minY = 0.0D;
                maxY = 1.0D;
                break;
            case Z:
                minZ = 0.0D;
                maxZ = 1.0D;
                break;
        }
    }

    @Override
    public int onBlockPlaced(World worldIn, int x, int y, int z, int side, float subX, float subY, float subZ,
        int meta) {
        meta = getMetaFromDirection(
            Direction.from3DDataValue(side)
                .getOpposite());
        worldIn.setBlockMetadataWithNotify(x, y, z, meta, 2);
        return meta;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {}

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        ItemStack heldItem = player.getHeldItem();
        if (heldItem == null) return false;
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        MovingObjectPosition ray = new MovingObjectPosition(
            x,
            y,
            z,
            side,
            Vec3.createVectorHelper(subX, subY, subZ),
            true);

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(heldItem) && !player.isSneaking())
            return placementHelper.getOffset(player, world, block, meta, x, y, z, ray)
                .placeInWorld(world, (ItemBlock) heldItem.getItem(), player, ray);

        return false;
    }

    public static class PlacementHelper extends PoleHelper<Direction> {

        private static final PlacementHelper instance = new PlacementHelper();

        public static PlacementHelper get() {
            return instance;
        }

        private PlacementHelper() {
            super(
                (block, meta) -> block instanceof PistonExtensionPoleBlock,
                (block, meta) -> ((PistonExtensionPoleBlock) block).getAxis(meta));
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack != null && stack.getItem() instanceof ItemBlock ib
                && Block.getBlockFromItem(ib) instanceof PistonExtensionPoleBlock;
        }
    }
}
