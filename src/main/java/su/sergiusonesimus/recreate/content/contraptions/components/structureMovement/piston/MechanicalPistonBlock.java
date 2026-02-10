package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.AllSounds;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.base.DirectionalAxisKineticBlock;
import su.sergiusonesimus.recreate.content.contraptions.base.IRotate;
import su.sergiusonesimus.recreate.content.contraptions.relays.gearbox.GearboxBlock;
import su.sergiusonesimus.recreate.foundation.block.ITE;
import su.sergiusonesimus.recreate.foundation.config.AllConfigs;
import su.sergiusonesimus.recreate.foundation.utility.Lang;
import su.sergiusonesimus.recreate.util.BlockHelper;

public class MechanicalPistonBlock extends DirectionalAxisKineticBlock implements ITE<MechanicalPistonTileEntity> {

    public static IIcon pistonBottom;
    public static IIcon pistonInner;
    public static IIcon pistonSide;
    public static IIcon pistonSideGearbox;

    /**
     * 0 - normal piston <br>
     * 1 - bottom cover <br>
     * 2 - piston head
     */
    public int renderType = 0;

    protected boolean isSticky;

    public static MechanicalPistonBlock normal(Material material) {
        return new MechanicalPistonBlock(material, false);
    }

    public static MechanicalPistonBlock sticky(Material material) {
        return new MechanicalPistonBlock(material, true);
    }

    protected MechanicalPistonBlock(Material material, boolean sticky) {
        super(material);
        isSticky = sticky;
        this.setStepSound(soundTypePiston);
        this.setHardness(1.5F);
        this.setResistance(5.0F);
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public int getRenderType() {
        return ReCreate.proxy.getMechanicalPistonBlockRenderID();
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess worldIn, int x, int y, int z, int side) {
        Direction sideDir = Direction.from3DDataValue(side);
        Direction blockDir = getDirection(worldIn.getBlockMetadata(x, y, z));
        if (sideDir != blockDir || getPistonState(worldIn, x, y, z) == PistonState.RETRACTED)
            return this.getIcon(side, worldIn.getBlockMetadata(x, y, z));
        else return pistonInner;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        Direction sideDir = Direction.from3DDataValue(side);
        Direction blockDir = this.getDirection(meta);
        if (sideDir == blockDir) return renderType == 1 ? GearboxBlock.gearboxSide
            : (this.isSticky ? Blocks.sticky_piston : Blocks.piston).getPistonExtensionTexture();
        else if (sideDir == blockDir.getOpposite())
            return renderType == 2 ? Blocks.piston.getPistonExtensionTexture() : pistonBottom;
        boolean axisAlingFirst = this.isAxisAlongFirstCoordinate(meta);
        boolean returnGearBox = false;
        switch (blockDir.getAxis()) {
            case X:
                returnGearBox = axisAlingFirst ? sideDir.getAxis() == Axis.Y : sideDir.getAxis() == Axis.Z;
                break;
            case Y:
                returnGearBox = axisAlingFirst ? sideDir.getAxis() == Axis.X : sideDir.getAxis() == Axis.Z;
                break;
            case Z:
                returnGearBox = axisAlingFirst ? sideDir.getAxis() == Axis.X : sideDir.getAxis() == Axis.Y;
                break;
        }
        return returnGearBox ? pistonSideGearbox : pistonSide;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        pistonBottom = iconRegister.registerIcon(ReCreate.ID + ":piston_bottom");
        pistonInner = iconRegister.registerIcon(ReCreate.ID + ":piston_inner");
        pistonSide = iconRegister.registerIcon(ReCreate.ID + ":piston_side");
        pistonSideGearbox = iconRegister.registerIcon(ReCreate.ID + ":piston_side_gearbox");
    }

    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        if (player.isPlayerSleeping() || player.isRiding() || player.isSneaking()) return false;
        ItemStack heldItem = player.getHeldItem();
        if (!OreDictionary.containsMatch(false, OreDictionary.getOres("slimeball"), heldItem)) {
            if (heldItem == null) {
                withTileEntityDo(worldIn, x, y, z, te -> te.assembleNextTick = true);
                return true;
            }
            return false;
        }
        if (getPistonState(worldIn, x, y, z) != PistonState.RETRACTED) return false;
        int meta = worldIn.getBlockMetadata(x, y, z);
        Direction direction = getDirection(meta);
        if (Direction.from3DDataValue(side) != direction) return false;
        MechanicalPistonBlock block = (MechanicalPistonBlock) worldIn.getBlock(x, y, z);
        if (block.isSticky) return false;
        if (worldIn.isRemote) {
            worldIn.spawnParticle(
                "iconcrack_" + Item.getIdFromItem(Items.slime_ball),
                x + subX,
                y + subY,
                z + subZ,
                0,
                0,
                0);
            return true;
        }
        AllSounds.SLIME_ADDED.playOnServer(worldIn, x, y, z, .5f, 1);
        if (!player.capabilities.isCreativeMode) player.getHeldItem().stackSize--;
        worldIn.setBlock(x, y, z, AllBlocks.sticky_mechanical_piston, meta, 1 | 2);
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!(neighbor instanceof IRotate rot)) return;
        Direction direction = this.getDirection(world.getBlockMetadata(x, y, z));
        ChunkCoordinates normal = direction.getOpposite()
            .getNormal();
        if (world.getBlock(x + normal.posX, y + normal.posY, z + normal.posZ) != neighbor) return;
        if (!world.isRemote) world.scheduleBlockUpdate(x, y, z, this, 0);
    }

    @Override
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        Direction direction = this.getDirection(worldIn.getBlockMetadata(x, y, z));
        ChunkCoordinates normal = direction.getOpposite()
            .getNormal();
        Block neighbor = worldIn.getBlock(x + normal.posX, y + normal.posY, z + normal.posZ);
        if (!(neighbor instanceof PistonExtensionPoleBlock pole)) return;
        int poleMeta = worldIn.getBlockMetadata(x + normal.posX, y + normal.posY, z + normal.posZ);
        if (pole.getDirection(poleMeta)
            .getAxis() != direction.getAxis()) return;
        withTileEntityDo(worldIn, x, y, z, te -> {
            if (te.lastException == null) return;
            te.lastException = null;
            te.sendData();
        });
    }

    @Override
    public boolean onWrenched(World world, int x, int y, int z, int face, EntityPlayer player) {
        if (getPistonState(world, x, y, z) != PistonState.RETRACTED) return false;
        return super.onWrenched(world, x, y, z, face, player);
    }

    public enum PistonState {

        RETRACTED,
        MOVING,
        EXTENDED;

        public String getSerializedName() {
            return Lang.asId(name());
        }
    }

    @Override
    public void onBlockHarvested(World worldIn, int x, int y, int z, int meta, EntityPlayer player) {
        Direction direction = this.getDirection(worldIn.getBlockMetadata(x, y, z));
        ChunkCoordinates normal = direction.getNormal();
        ChunkCoordinates pistonHead = null;
        ChunkCoordinates pistonBase = new ChunkCoordinates(x, y, z);
        boolean dropBlocks = player == null || !player.capabilities.isCreativeMode;

        Integer maxPoles = maxAllowedPistonPoles();
        for (int offset = 1; offset < maxPoles; offset++) {
            int currentX = x + normal.posX * offset;
            int currentY = y + normal.posY * offset;
            int currentZ = z + normal.posZ * offset;
            Block block = worldIn.getBlock(currentX, currentY, currentZ);
            int blockMeta = worldIn.getBlockMetadata(currentX, currentY, currentZ);

            if (isExtensionPole(block)
                && direction.getAxis() == ((PistonExtensionPoleBlock) block).getDirection(blockMeta)
                    .getAxis())
                continue;

            if (isPistonHead(block) && ((MechanicalPistonHeadBlock) block).getDirection(blockMeta) == direction) {
                pistonHead = new ChunkCoordinates(currentX, currentY, currentZ);
            }

            break;
        }

        if (pistonHead != null && pistonBase != null) {
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
                        if (tempX != x || tempY != y || tempZ != z)
                            BlockHelper.breakBlock(worldIn, tempX, tempY, tempZ, dropBlocks);
                    }
                }
            }
        }

        normal = direction.getOpposite()
            .getNormal();
        for (int offset = 1; offset < maxPoles; offset++) {
            int currentX = x + normal.posX * offset;
            int currentY = y + normal.posY * offset;
            int currentZ = z + normal.posZ * offset;
            Block block = worldIn.getBlock(currentX, currentY, currentZ);
            int blockMeta = worldIn.getBlockMetadata(currentX, currentY, currentZ);

            if (isExtensionPole(block)
                && direction.getAxis() == ((PistonExtensionPoleBlock) block).getDirection(blockMeta)
                    .getAxis()) {
                BlockHelper.breakBlock(worldIn, currentX, currentY, currentZ, dropBlocks);
                continue;
            }

            break;
        }

        super.onBlockHarvested(worldIn, x, y, z, meta, player);
    }

    @SuppressWarnings("static-access")
    public static int maxAllowedPistonPoles() {
        return AllConfigs.SERVER.kinetics.maxPistonPoles;
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(worldIn, x, y, z);
        return AxisAlignedBB.getBoundingBox(
            (double) x + this.minX,
            (double) y + this.minY,
            (double) z + this.minZ,
            (double) x + this.maxX,
            (double) y + this.maxY,
            (double) z + this.maxZ);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        minX = minY = minZ = 0.0D;
        maxX = maxY = maxZ = 1.0D;
        if (getPistonState(worldIn, x, y, z) != PistonState.RETRACTED) {
            double topHeight = 4.0D / 16.0D;
            switch (this.getDirection(worldIn.getBlockMetadata(x, y, z))) {
                case UP:
                    maxY -= topHeight;
                    break;
                case DOWN:
                    minY += topHeight;
                    break;
                case EAST:
                    maxX -= topHeight;
                    break;
                case WEST:
                    minX += topHeight;
                    break;
                case SOUTH:
                    maxZ -= topHeight;
                    break;
                case NORTH:
                    minZ += topHeight;
                    break;
            }
        }
    }

    public List<AxisAlignedBB> getSelectedBoundingBoxesList(World worldIn, int x, int y, int z) {
        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        list.add(getSelectedBoundingBoxFromPool(worldIn, x, y, z));
        if (this.getPistonState(worldIn, x, y, z) == PistonState.EXTENDED) {
            double minX = 0.0D;
            double minY = 0.0D;
            double minZ = 0.0D;
            double maxX = 1.0D;
            double maxY = 1.0D;
            double maxZ = 1.0D;
            double pixel = 1.0D / 16.0D;
            Direction dir = this.getDirection(worldIn.getBlockMetadata(x, y, z));

            switch (dir.getAxis()) {
                case X:
                    minY = minZ = 6 * pixel;
                    maxY = maxZ = 1.0D - 6 * pixel;
                    if (dir.getAxisDirection() == AxisDirection.POSITIVE) minX = 1.0D - 4 * pixel;
                    else maxX = 4 * pixel;
                    break;
                case Y:
                    minX = minZ = 6 * pixel;
                    maxX = maxZ = 1.0D - 6 * pixel;
                    if (dir.getAxisDirection() == AxisDirection.POSITIVE) minY = 1.0D - 4 * pixel;
                    else maxY = 4 * pixel;
                    break;
                case Z:
                    minY = minX = 6 * pixel;
                    maxY = maxX = 1.0D - 6 * pixel;
                    if (dir.getAxisDirection() == AxisDirection.POSITIVE) minZ = 1.0D - 4 * pixel;
                    else maxZ = 4 * pixel;
                    break;
            }

            list.add(AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ));
        }
        return list;
    }

    @Override
    public Class<MechanicalPistonTileEntity> getTileEntityClass() {
        return MechanicalPistonTileEntity.class;
    }

    public static boolean isPiston(Block block) {
        return block == AllBlocks.mechanical_piston || isStickyPiston(block);
    }

    public static boolean isStickyPiston(Block block) {
        return block == AllBlocks.sticky_mechanical_piston;
    }

    public static boolean isExtensionPole(Block block) {
        return block == AllBlocks.piston_extension_pole;
    }

    public static boolean isPistonHead(Block block) {
        return block == AllBlocks.mechanical_piston_head;
    }

    public PistonState getPistonState(IBlockAccess worldIn, int x, int y, int z) {
        if (worldIn.getTileEntity(x, y, z) instanceof MechanicalPistonTileEntity mpte) return mpte.state;
        return null;
    }

    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        if (getPistonState(world, x, y, z) == PistonState.RETRACTED)
            return Direction.fromForgeDirection(side) != getDirection(world.getBlockMetadata(x, y, z)).getOpposite();
        return false;
    }
}
