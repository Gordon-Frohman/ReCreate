package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.metaworlds.util.Rotation;
import su.sergiusonesimus.recreate.AllItems;
import su.sergiusonesimus.recreate.AllSounds;
import su.sergiusonesimus.recreate.content.contraptions.wrench.IWrenchable;
import su.sergiusonesimus.recreate.foundation.block.ITE;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;

public abstract class AbstractChassisBlock extends BlockRotatedPillar implements IWrenchable, ITE<ChassisTileEntity> {

    public AbstractChassisBlock(Material material) {
        super(material);
        this.setHardness(2.0F);
        this.setStepSound(soundTypeWood);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        if (player.isPlayerSleeping() || player.isRiding() || player.isSneaking()) return false;

        ItemStack heldItem = player.getHeldItem();
        if (heldItem == null) return false;
        boolean isSlimeBall = OreDictionary.containsMatch(false, OreDictionary.getOres("slimeball"), heldItem)
            || heldItem.getItem() == AllItems.super_glue;

        Direction sideDir = Direction.from3DDataValue(side);
        Boolean affectedSide = getGlueableSide(worldIn, x, y, z, sideDir);
        if (affectedSide == null) return false;

        if (isSlimeBall && affectedSide) {
            for (Direction face : Iterate.directions) {
                Boolean glueableSide = getGlueableSide(worldIn, x, y, z, face);
                if (glueableSide != null && !glueableSide && glueAllowedOnSide(worldIn, x, y, z, face)) {
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
                    setGlueableSide(worldIn, x, y, z, face, true);
                }
            }
            if (!worldIn.isRemote) worldIn.markBlockForUpdate(x, y, z);
            return true;
        }

        if (!player.isSneaking() && !isSlimeBall) return false;
        if (affectedSide.booleanValue() == isSlimeBall) return false;
        if (!glueAllowedOnSide(worldIn, x, y, z, sideDir)) return false;
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
        setGlueableSide(worldIn, x, y, z, sideDir, isSlimeBall);
        worldIn.markBlockForUpdate(x, y, z);
        return true;
    }

    public int rotate(World world, int x, int y, int z, Rotation rotation) {
        int meta = world.getBlockMetadata(x, y, z);
        if (rotation == Rotation.NONE) return meta;

        int rotatedMeta = meta;
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) switch (meta >> 2) {
            default:
                break;
            case 1:
                rotatedMeta += 4;
                break;
            case 2:
                rotatedMeta -= 4;
                break;
        }
        Map<Direction, Boolean> oldDirections = new HashMap<Direction, Boolean>();
        for (Direction face : Iterate.directions) {
            Boolean glueableSide = getGlueableSide(world, x, y, z, face);
            if (glueableSide != null) oldDirections.put(face, glueableSide);
        }

        for (Entry<Direction, Boolean> entry : oldDirections.entrySet()) {
            Direction face = entry.getKey();
            Direction rotatedFacing = rotation.rotate(face);
            setGlueableSide(world, x, y, z, rotatedFacing, entry.getValue());
        }

        return rotatedMeta;
    }

    // TODO
    // @Override
    // public BlockState mirror(BlockState state, Mirror mirrorIn) {
    // if (mirrorIn == Mirror.NONE) return state;
    //
    // BlockState mirrored = state;
    // for (Direction face : Iterate.directions) {
    // BooleanProperty glueableSide = getGlueableSide(mirrored, face);
    // if (glueableSide != null) mirrored = mirrored.setValue(glueableSide, false);
    // }
    //
    // for (Direction face : Iterate.directions) {
    // BooleanProperty glueableSide = getGlueableSide(state, face);
    // if (glueableSide == null || !state.getValue(glueableSide)) continue;
    // Direction mirroredFacing = mirrorIn.mirror(face);
    // BooleanProperty mirroredGlueableSide = getGlueableSide(mirrored, mirroredFacing);
    // if (mirroredGlueableSide != null) mirrored = mirrored.setValue(mirroredGlueableSide, true);
    // }
    //
    // return mirrored;
    // }

    public abstract Boolean getGlueableSide(IBlockAccess worldIn, int x, int y, int z, Direction face);

    public abstract void setGlueableSide(IBlockAccess worldIn, int x, int y, int z, Direction face, boolean value);

    protected boolean glueAllowedOnSide(IBlockAccess worldIn, int x, int y, int z, Direction side) {
        return true;
    }

    @Override
    public Class<ChassisTileEntity> getTileEntityClass() {
        return ChassisTileEntity.class;
    }

    @Override
    public Direction getDirection(int meta) {
        switch (meta >> 2) {
            default:
                return Direction.UP;
            case 1:
                return Direction.EAST;
            case 2:
                return Direction.SOUTH;
        }
    }

    @Override
    public int getMetaFromDirection(Direction direction) {
        switch (direction.getAxis()) {
            default:
                return 0;
            case X:
                return 4;
            case Z:
                return 8;
        }
    }

    @Override
    public boolean onWrenched(World world, int x, int y, int z, int face, EntityPlayer player) {
        int meta = world.getBlockMetadata(x, y, z);
        Map<Direction, Boolean> oldDirections = new HashMap<Direction, Boolean>();
        int stickySides = 0;
        int notStickySides = 0;
        for (Direction dir : Iterate.directions) {
            Boolean glueableSide = getGlueableSide(world, x, y, z, dir);
            if (glueableSide != null) {
                oldDirections.put(dir, glueableSide);
                if (glueableSide) stickySides++;
                else notStickySides++;
            }
        }

        int rotatedMeta = getRotatedBlockMeta(world, x, y, z, face);

        if (meta != rotatedMeta || (stickySides != 0 && notStickySides != 0)) {
            world.setBlockMetadataWithNotify(x, y, z, rotatedMeta, 3);
            Direction rotationDirection = Direction.from3DDataValue(face);
            Axis rotationAxis = rotationDirection.getAxis();
            for (Entry<Direction, Boolean> entry : oldDirections.entrySet()) {
                Direction dir = entry.getKey();
                int rotCount = rotationDirection.getAxisDirection() == AxisDirection.POSITIVE ? 1 : 3;
                Direction rotatedFacing = dir;
                for (int i = 0; i < rotCount; i++) rotatedFacing = rotatedFacing.rotateAround(rotationAxis);
                setGlueableSide(world, x, y, z, rotatedFacing, entry.getValue());
            }
            if (meta == rotatedMeta) world.markBlockForUpdate(x, y, z);
            playRotateSound(world, x, y, z);
            return true;
        }

        return false;
    }

    @Override
    public int getRotatedBlockMeta(World world, int x, int y, int z, int face) {
        int originalMeta = world.getBlockMetadata(x, y, z);
        return this.getMetaFromDirection(
            this.getDirection(originalMeta)
                .rotateAround(
                    Direction.from3DDataValue(face)
                        .getAxis()))
            + (originalMeta & 3);
    }

}
