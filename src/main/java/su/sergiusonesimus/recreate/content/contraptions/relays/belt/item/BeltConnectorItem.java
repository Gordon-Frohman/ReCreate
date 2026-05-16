package su.sergiusonesimus.recreate.content.contraptions.relays.belt.item;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltPart;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltSlope;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftBlock;
import su.sergiusonesimus.recreate.content.contraptions.wrench.IWrenchable;
import su.sergiusonesimus.recreate.foundation.config.AllConfigs;

public class BeltConnectorItem extends ItemBlock {

    public BeltConnectorItem(Block block) {
        super(block);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName() {
        return "item.belt_connector";
    }

    @Nonnull
    @Override
    public boolean onItemUse(ItemStack item, EntityPlayer playerEntity, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (playerEntity != null && playerEntity.isSneaking()) {
            item.setTagCompound(null);
            return true;
        }

        boolean validAxis = validateAxis(world, x, y, z);

        if (world.isRemote) return validAxis ? true : false;

        NBTTagCompound tag = item.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            item.setTagCompound(tag);
        }
        Integer firstPulleyX = null;
        Integer firstPulleyY = null;
        Integer firstPulleyZ = null;

        // Remove first if no longer existant or valid
        if (tag.hasKey("FirstPulley")) {
            NBTTagCompound firstPulley = tag.getCompoundTag("FirstPulley");
            firstPulleyX = firstPulley.getInteger("X");
            firstPulleyY = firstPulley.getInteger("Y");
            firstPulleyZ = firstPulley.getInteger("Z");
            if (!validateAxis(world, firstPulleyX, firstPulleyY, firstPulleyZ)
                || Vec3.createVectorHelper(firstPulleyX - x, firstPulleyY - y, firstPulleyZ - z)
                    .lengthVector() > maxLength() * 2) {
                tag.removeTag("FirstPulley");
                item.setTagCompound(tag);
            }
        }

        if (!validAxis || playerEntity == null) return false;

        if (tag.hasKey("FirstPulley")) {

            if (!canConnect(world, firstPulleyX, firstPulleyY, firstPulleyZ, x, y, z)) return false;

            if (firstPulleyX != null && firstPulleyY != null
                && firstPulleyZ != null
                && (firstPulleyX != x || firstPulleyY != y || firstPulleyZ != z)) {
                createBelts(world, firstPulleyX, firstPulleyY, firstPulleyZ, x, y, z);
                // TODO AllTriggers.triggerFor(AllTriggers.CONNECT_BELT, playerEntity);
                if (!playerEntity.capabilities.isCreativeMode) item.stackSize--;
            }

            if (item != null && item.stackSize > 0) {
                item.setTagCompound(null);
                // TODO playerEntity.getCooldowns().addCooldown(this, 5);
            }
            return true;
        }

        NBTTagCompound firstPulley = new NBTTagCompound();
        firstPulley.setInteger("X", x);
        firstPulley.setInteger("Y", y);
        firstPulley.setInteger("Z", z);
        tag.setTag("FirstPulley", firstPulley);
        item.setTagCompound(tag);
        // TODO playerEntity.getCooldowns().addCooldown(this, 5);
        return true;
    }

    public static void createBelts(World world, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        world.playSoundEffect(
            (startX + endX) / 2,
            (startY + endY) / 2,
            (startZ + endZ) / 2,
            Block.soundTypeCloth.getBreakSound(),
            0.5F,
            1F);

        BeltSlope slope = getSlopeBetween(startX, startY, startZ, endX, endY, endZ);
        Direction facing = getFacingFromTo(startX, startY, startZ, endX, endY, endZ);

        int dX = endX - startX;
        int dZ = endZ - startZ;
        if (dX == dZ) facing = Direction.get(
            facing.getAxisDirection(),
            ((ShaftBlock) world.getBlock(startX, startY, startZ))
                .getAxis(world.getBlockMetadata(startX, startY, startZ)) == Axis.X ? Axis.Z : Axis.X);

        List<ChunkCoordinates> beltsToCreate = getBeltChainBetween(
            startX,
            startY,
            startZ,
            endX,
            endY,
            endZ,
            slope,
            facing);

        for (ChunkCoordinates pos : beltsToCreate) {
            BeltPart part = pos.posX == startX && pos.posY == startY && pos.posZ == startZ ? BeltPart.START
                : pos.posX == endX && pos.posY == endY && pos.posZ == endZ ? BeltPart.END : BeltPart.MIDDLE;
            Block shaft = world.getBlock(pos.posX, pos.posY, pos.posZ);
            int shaftMeta = world.getBlockMetadata(pos.posX, pos.posY, pos.posZ);
            boolean pulley = ShaftBlock.isShaft(shaft);
            if (part == BeltPart.MIDDLE && pulley) part = BeltPart.PULLEY;
            if (pulley && ((ShaftBlock) shaft).getAxis(shaftMeta) == Axis.Y) slope = BeltSlope.SIDEWAYS;
            KineticTileEntity.switchToBlockState(
                world,
                pos.posX,
                pos.posY,
                pos.posZ,
                AllBlocks.belt,
                ((BeltBlock) AllBlocks.belt).getMetaFromDirection(facing) + (slope == BeltSlope.SIDEWAYS ? 0 : 8),
                false);
            BeltTileEntity bte = (BeltTileEntity) world.getTileEntity(pos.posX, pos.posY, pos.posZ);
            bte.slopeType = slope;
            bte.partType = part;
        }
    }

    private static Direction getFacingFromTo(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        Axis beltAxis = startX == endX ? Axis.Z : Axis.X;
        int dX = endX - startX;
        int dY = endY - startY;
        int dZ = endZ - startZ;
        AxisDirection axisDirection = AxisDirection.POSITIVE;

        if (dX == 0 && dZ == 0) axisDirection = dY > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        else axisDirection = beltAxis.choose(dX, 0, dZ) > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;

        return Direction.get(axisDirection, beltAxis);
    }

    private static BeltSlope getSlopeBetween(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        int dX = endX - startX;
        int dY = endY - startY;
        int dZ = endZ - startZ;

        if (dY != 0) {
            if (dZ != 0 || dX != 0) return dY > 0 ? BeltSlope.UPWARD : BeltSlope.DOWNWARD;
            return BeltSlope.VERTICAL;
        }
        return BeltSlope.HORIZONTAL;
    }

    private static List<ChunkCoordinates> getBeltChainBetween(int startX, int startY, int startZ, int endX, int endY,
        int endZ, BeltSlope slope, Direction direction) {
        List<ChunkCoordinates> positions = new LinkedList<>();
        int limit = 1000;
        int currentX = startX;
        int currentY = startY;
        int currentZ = startZ;
        ChunkCoordinates normal = direction.getNormal();

        do {
            positions.add(new ChunkCoordinates(currentX, currentY, currentZ));

            if (slope == BeltSlope.VERTICAL) {
                currentY += direction.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -1;
                continue;
            }

            currentX += normal.posX;
            currentY += normal.posY;
            currentZ += normal.posZ;
            if (slope != BeltSlope.HORIZONTAL) currentY += slope == BeltSlope.UPWARD ? 1 : -1;

        } while ((currentX != endX || currentY != endY || currentZ != endZ) && limit-- > 0);

        positions.add(new ChunkCoordinates(endX, endY, endZ));
        return positions;
    }

    public static boolean canConnect(World world, int firstX, int firstY, int firstZ, int secondX, int secondY,
        int secondZ) {
        if (!world.doChunksNearChunkExist(firstX, firstY, firstZ, 1)) return false;
        if (!world.doChunksNearChunkExist(secondX, secondY, secondZ, 1)) return false;
        int x = secondX - firstX;
        int y = secondY - firstY;
        int z = secondZ - firstZ;
        if (Vec3.createVectorHelper(x, y, z)
            .lengthVector() > maxLength()) return false;

        Block firstBlock = world.getBlock(firstX, firstY, firstZ);
        int firstMeta = world.getBlockMetadata(firstX, firstY, firstZ);
        if (!(firstBlock instanceof IWrenchable firstWrenchable)) return false;
        Axis shaftAxis = firstWrenchable.getAxis(firstMeta);

        int sames = ((Math.abs(x) == Math.abs(y)) ? 1 : 0) + ((Math.abs(y) == Math.abs(z)) ? 1 : 0)
            + ((Math.abs(z) == Math.abs(x)) ? 1 : 0);

        if (shaftAxis.choose(x, y, z) != 0) return false;
        if (sames != 1) return false;

        Block secondBlock = world.getBlock(secondX, secondY, secondZ);
        int secondMeta = world.getBlockMetadata(secondX, secondY, secondZ);
        if (!(secondBlock instanceof IWrenchable secondWrenchable)) return false;
        if (shaftAxis != secondWrenchable.getAxis(secondMeta)) return false;
        if (shaftAxis == Axis.Y && x != 0 && z != 0) return false;

        TileEntity tileEntity = world.getTileEntity(firstX, firstY, firstZ);
        TileEntity tileEntity2 = world.getTileEntity(secondX, secondY, secondZ);

        if (!(tileEntity instanceof KineticTileEntity kte)) return false;
        if (!(tileEntity2 instanceof KineticTileEntity kte2)) return false;

        float speed1 = kte.getTheoreticalSpeed();
        float speed2 = kte2.getTheoreticalSpeed();
        if (Math.signum(speed1) != Math.signum(speed2) && speed1 != 0 && speed2 != 0) return false;

        ChunkCoordinates step = new ChunkCoordinates(
            MathHelper.floor_float(Math.signum(x)),
            MathHelper.floor_float(Math.signum(y)),
            MathHelper.floor_float(Math.signum(z)));
        int limit = 1000;
        int currentX = firstX + step.posX;
        int currentY = firstY + step.posY;
        int currentZ = firstZ + step.posZ;
        while ((currentX != secondX || currentY != secondY || currentZ != secondZ) && limit-- > 0) {
            Block block = world.getBlock(currentX, currentY, currentZ);
            int meta = world.getBlockMetadata(currentX, currentY, currentZ);
            if (block instanceof ShaftBlock shaft && shaft.getAxis(meta) == shaftAxis) continue;
            if (!block.getMaterial()
                .isReplaceable()) return false;

            currentX += step.posX;
            currentY += step.posY;
            currentZ += step.posZ;
        }

        return true;

    }

    @SuppressWarnings("static-access")
    public static Integer maxLength() {
        return AllConfigs.SERVER.kinetics.maxBeltLength;
    }

    public static boolean validateAxis(World world, int x, int y, int z) {
        if (!world.doChunksNearChunkExist(x, y, z, 1)) return false;
        if (!ShaftBlock.isShaft(world.getBlock(x, y, z))) return false;
        return true;
    }

}
