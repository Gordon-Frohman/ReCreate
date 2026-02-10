package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.AllSounds;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import su.sergiusonesimus.recreate.foundation.block.WrenchableDirectionalBlock;
import su.sergiusonesimus.recreate.util.BlockHelper;

public class MechanicalPistonHeadBlock extends WrenchableDirectionalBlock {

    public MechanicalPistonHeadBlock(Material material) {
        super(material);
        this.setHardness(1.5F);
        this.setResistance(5.0F);
        this.setStepSound(soundTypePiston);
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public int getRenderType() {
        return ReCreate.proxy.getMechanicalPistonHeadBlockRenderID();
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        Direction sideDir = getDirection(side);
        Direction blockDir = getDirection(meta);
        if (sideDir == blockDir) {
            return (isSticky(meta) ? Blocks.sticky_piston : Blocks.piston).getPistonExtensionTexture();
        } else if (sideDir == blockDir.getOpposite()) {
            return Blocks.piston.getPistonExtensionTexture();
        }
        return MechanicalPistonBlock.pistonSide;
    }

    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        if (player.isPlayerSleeping() || player.isRiding() || player.isSneaking()) return false;
        ItemStack heldItem = player.getHeldItem();
        int meta = worldIn.getBlockMetadata(x, y, z);
        if (!OreDictionary.containsMatch(false, OreDictionary.getOres("slimeball"), heldItem) || this.isSticky(meta))
            return false;
        worldIn.setBlockMetadataWithNotify(x, y, z, meta + 6, 2);
        Direction direction = getDirection(meta);
        if (Direction.from3DDataValue(side) != direction) return false;

        ChunkCoordinates normal = direction.getOpposite()
            .getNormal();
        int nextX = x + normal.posX;
        int nextY = y + normal.posY;
        int nextZ = z + normal.posZ;
        Block nextBlock = worldIn.getBlock(nextX, nextY, nextZ);
        int nextMeta = worldIn.getBlockMetadata(nextX, nextY, nextZ);

        while (nextBlock instanceof PistonExtensionPoleBlock pole && pole.getAxis(nextMeta) == direction.getAxis()) {
            nextX += normal.posX;
            nextY += normal.posY;
            nextZ += normal.posZ;
            nextBlock = worldIn.getBlock(nextX, nextY, nextZ);
            nextMeta = worldIn.getBlockMetadata(nextX, nextY, nextZ);
        }

        if (!(nextBlock instanceof MechanicalPistonBlock piston) || piston.getDirection(nextMeta) != direction
            || piston.getPistonState(worldIn, nextX, nextY, nextZ) != PistonState.EXTENDED
            || piston.isSticky) return false;
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
        NBTTagCompound nbt = new NBTTagCompound();
        worldIn.getTileEntity(nextX, nextY, nextZ)
            .writeToNBT(nbt);
        worldIn.setBlock(nextX, nextY, nextZ, AllBlocks.sticky_mechanical_piston, nextMeta, 2);
        worldIn.setTileEntity(nextX, nextY, nextZ, TileEntity.createAndLoadEntity(nbt));
        return true;
    }

    @Override
    public int getMobilityFlag() {
        return 0;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemStack(AllBlocks.piston_extension_pole);
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return Item.getItemFromBlock(AllBlocks.piston_extension_pole);
    }

    @Override
    public void onBlockHarvested(World worldIn, int x, int y, int z, int meta, EntityPlayer player) {
        Direction direction = getDirection(meta);
        ChunkCoordinates pistonHead = new ChunkCoordinates(x, y, z);
        ChunkCoordinates pistonBase = null;
        boolean dropBlocks = player == null || !player.capabilities.isCreativeMode;

        ChunkCoordinates normal = direction.getOpposite()
            .getNormal();
        for (int offset = 1; offset < MechanicalPistonBlock.maxAllowedPistonPoles(); offset++) {
            int currentX = x + normal.posX * offset;
            int currentY = y + normal.posY * offset;
            int currentZ = z + normal.posZ * offset;
            Block block = worldIn.getBlock(currentX, currentY, currentZ);
            int currentMeta = worldIn.getBlockMetadata(currentX, currentY, currentZ);

            if (MechanicalPistonBlock.isExtensionPole(block)
                && direction.getAxis() == ((PistonExtensionPoleBlock) block).getDirection(currentMeta)
                    .getAxis())
                continue;

            if (MechanicalPistonBlock.isPiston(block)
                && ((MechanicalPistonBlock) block).getDirection(currentMeta) == direction)
                pistonBase = new ChunkCoordinates(currentX, currentY, currentZ);

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
                        if ((tempX != x || tempY != y || tempZ != z)
                            && (tempX != pistonBase.posX || tempY != pistonBase.posY || tempZ != pistonBase.posZ))
                            BlockHelper.breakBlock(worldIn, tempX, tempY, tempZ, dropBlocks);
                    }
                }
            }

            MechanicalPistonTileEntity te = ((MechanicalPistonTileEntity) worldIn
                .getTileEntity(pistonBase.posX, pistonBase.posY, pistonBase.posZ));
            te.state = PistonState.RETRACTED;
            te.offset = 0;
            te.onLengthBroken();
            te.notifyUpdate();
        }

        super.onBlockHarvested(worldIn, x, y, z, meta, player);
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask, List list,
        Entity collider) {
        AllBlocks.piston_extension_pole.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        float minX = 0.0F;
        float minY = 0.0F;
        float minZ = 0.0F;
        float maxX = 1.0F;
        float maxY = 1.0F;
        float maxZ = 1.0F;
        float topHeight = 4.0F / 16.0F;
        switch (this.getDirection(worldIn.getBlockMetadata(x, y, z))) {
            case UP:
                minY = 1.0F - topHeight;
                break;
            case DOWN:
                maxY = topHeight;
                break;
            case EAST:
                minX = 1.0F - topHeight;
                break;
            case WEST:
                maxX = topHeight;
                break;
            case SOUTH:
                minZ = 1.0F - topHeight;
                break;
            case NORTH:
                maxZ = topHeight;
                break;
        }
        this.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
        super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox(
            (double) x + this.minX,
            (double) y + this.minY,
            (double) z + this.minZ,
            (double) x + this.maxX,
            (double) y + this.maxY,
            (double) z + this.maxZ);
    }

    public List<AxisAlignedBB> getSelectedBoundingBoxesList(World worldIn, int x, int y, int z) {
        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        double headMinX = 0.0D;
        double headMinY = 0.0D;
        double headMinZ = 0.0D;
        double headMaxX = 1.0D;
        double headMaxY = 1.0D;
        double headMaxZ = 1.0D;
        double poleMinX = 0.0D;
        double poleMinY = 0.0D;
        double poleMinZ = 0.0D;
        double poleMaxX = 1.0D;
        double poleMaxY = 1.0D;
        double poleMaxZ = 1.0D;
        double pixel = 1.0D / 16.0D;
        Direction dir = this.getDirection(worldIn.getBlockMetadata(x, y, z));

        switch (dir.getAxis()) {
            case X:
                poleMinY = poleMinZ = 6 * pixel;
                poleMaxY = poleMaxZ = 1.0D - 6 * pixel;
                if (dir.getAxisDirection() == AxisDirection.POSITIVE) headMinX = poleMaxX = 1.0D - 4 * pixel;
                else headMaxX = poleMinX = 4 * pixel;
                break;
            case Y:
                poleMinX = poleMinZ = 6 * pixel;
                poleMaxX = poleMaxZ = 1.0D - 6 * pixel;
                if (dir.getAxisDirection() == AxisDirection.POSITIVE) headMinY = poleMaxY = 1.0D - 4 * pixel;
                else headMaxY = poleMinY = 4 * pixel;
                break;
            case Z:
                poleMinY = poleMinX = 6 * pixel;
                poleMaxY = poleMaxX = 1.0D - 6 * pixel;
                if (dir.getAxisDirection() == AxisDirection.POSITIVE) headMinZ = poleMaxZ = 1.0D - 4 * pixel;
                else headMaxZ = poleMinZ = 4 * pixel;
                break;
        }

        list.add(
            AxisAlignedBB
                .getBoundingBox(x + headMinX, y + headMinY, z + headMinZ, x + headMaxX, y + headMaxY, z + headMaxZ));
        list.add(
            AxisAlignedBB
                .getBoundingBox(x + poleMinX, y + poleMinY, z + poleMinZ, x + poleMaxX, y + poleMaxY, z + poleMaxZ));
        return list;
    }

    public boolean isSticky(int meta) {
        return meta % 12 >= 6;
    }

    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return getDirection(world.getBlockMetadata(x, y, z)) == Direction.fromForgeDirection(side);
    }
}
