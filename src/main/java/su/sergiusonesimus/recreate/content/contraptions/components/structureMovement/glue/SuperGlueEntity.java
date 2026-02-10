package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import org.apache.commons.lang3.Validate;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Rotation;
import su.sergiusonesimus.recreate.AllItems;
import su.sergiusonesimus.recreate.AllSounds;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.BlockMovementChecks;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.BearingBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis.AbstractChassisBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import su.sergiusonesimus.recreate.foundation.networking.AllPackets;
import su.sergiusonesimus.recreate.foundation.utility.BlockFace;

public class SuperGlueEntity extends Entity implements IEntityAdditionalSpawnData {

    private int validationTimer;
    protected int hangingPositionX;
    protected int hangingPositionY;
    protected int hangingPositionZ;
    protected Direction facingDirection;
    private static boolean blockPlacement = false;

    public SuperGlueEntity(World world) {
        super(world);
    }

    public SuperGlueEntity(World world, int posX, int posY, int posZ, Direction direction) {
        super(world);
        hangingPositionX = posX;
        hangingPositionY = posY;
        hangingPositionZ = posZ;
        facingDirection = direction;
        updateFacingWithBoundingBox();
    }

    public int getWidthPixels() {
        return 8;
    }

    public int getHeightPixels() {
        return 8;
    }

    public void onBroken(@Nullable Entity breaker) {
        playSound("mob.slime.small", 1.0F, 1.0F);
        if (onValidSurface()) {
            AllPackets.CHANNEL.sendToAll(
                new GlueEffectPacket(
                    hangingPositionX,
                    hangingPositionY,
                    hangingPositionZ,
                    getFacingDirection().getOpposite(),
                    false));
            AllSounds.SLIME_ADDED.playFrom(this, 0.5F, 0.5F);
        }
    }

    public void playPlaceSound() {
        AllSounds.SLIME_ADDED.playFrom(this, 0.5F, 0.75F);
    }

    protected void updateFacingWithBoundingBox() {
        Validate.notNull(getFacingDirection());
        if (getFacingDirection().getAxis()
            .isHorizontal()) {
            this.rotationPitch = 0;
            this.rotationYaw = getFacingDirection().get2DDataValue() * 90;
        } else {
            this.rotationPitch = -90 * getFacingDirection().getAxisDirection()
                .getStep();
            this.rotationYaw = 0;
        }

        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;
        this.updateBoundingBox();
    }

    protected void updateBoundingBox() {
        if (this.getFacingDirection() != null) {
            double offset = 0.5d - 1 / 256d;
            ChunkCoordinates normal = facingDirection.getNormal();
            double x = (double) hangingPositionX + 0.5d - (double) normal.posX * offset;
            double y = (double) hangingPositionY + 0.5d - (double) normal.posY * offset;
            double z = (double) hangingPositionZ + 0.5d - (double) normal.posZ * offset;
            super.setPosition(
                (double) hangingPositionX + 0.5d,
                (double) hangingPositionY + 0.5d,
                (double) hangingPositionZ + 0.5d);
            double w = getWidthPixels() / 16d / 2d;
            double h = getHeightPixels() / 16d / 2d;
            double l = getWidthPixels() / 16d / 2d;
            Axis axis = this.getFacingDirection()
                .getAxis();
            double depth = 1d / 128d;

            switch (axis) {
                case X:
                    w = depth;
                    break;
                case Y:
                    h = depth;
                    break;
                case Z:
                    l = depth;
            }
            this.boundingBox = AxisAlignedBB.getBoundingBox(x - w, y - h, z - l, x + w, y + h, z + l);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.validationTimer++ == 10 && !this.worldObj.isRemote) {
            this.validationTimer = 0;
            if (isEntityAlive() && !this.onValidSurface()) {
                kill();
                onBroken(null);
            }
        }

    }

    public boolean isVisible() {
        if (!isEntityAlive()) return false;
        if (worldObj instanceof SubWorld) return true;

        ChunkCoordinates normal = getFacingDirection().getOpposite()
            .getNormal();
        return isValidFace(
            worldObj,
            hangingPositionX + normal.posX,
            hangingPositionY + normal.posY,
            hangingPositionZ + normal.posZ,
            getFacingDirection())
            != isValidFace(
                worldObj,
                hangingPositionX,
                hangingPositionY,
                hangingPositionZ,
                getFacingDirection().getOpposite());
    }

    public boolean onValidSurface() {
        ChunkCoordinates normal = getFacingDirection().getOpposite()
            .getNormal();
        int relX = hangingPositionX + normal.posX;
        int relY = hangingPositionY + normal.posY;
        int relZ = hangingPositionZ + normal.posZ;
        if (relY >= 256) return false;
        if (!worldObj.blockExists(hangingPositionX, hangingPositionY, hangingPositionZ)
            || !worldObj.blockExists(relX, relY, relZ)) return true;
        if (!isValidFace(worldObj, relX, relY, relZ, getFacingDirection()) && !isValidFace(
            worldObj,
            hangingPositionX,
            hangingPositionY,
            hangingPositionZ,
            getFacingDirection().getOpposite())) return false;
        if (isSideSticky(worldObj, relX, relY, relZ, getFacingDirection()) || isSideSticky(
            worldObj,
            hangingPositionX,
            hangingPositionY,
            hangingPositionZ,
            getFacingDirection().getOpposite())) return false;
        return worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox, e -> e instanceof SuperGlueEntity)
            .isEmpty();
    }

    public static boolean isValidFace(World world, int x, int y, int z, Direction direction) {
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        if (BlockMovementChecks.isBlockAttachedTowards(block, meta, world, x, y, z, direction)) return true;
        if (!BlockMovementChecks.isMovementNecessary(block, meta, world, x, y, z)) return false;
        if (BlockMovementChecks.isNotSupportive(block, meta, direction)) return false;
        return true;
    }

    public static boolean isSideSticky(World world, int x, int y, int z, Direction direction) {
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);

        if (block instanceof MechanicalPistonBlock piston && MechanicalPistonBlock.isStickyPiston(block))
            return piston.getDirection(meta) == direction;
        // TODO
        // if (AllBlocks.STICKER.has(state))
        // return state.getValue(DirectionalBlock.FACING) == direction;
        //
        // if (state.getBlock() == Blocks.SLIME_BLOCK)
        // return true;
        // if (state.getBlock() == Blocks.HONEY_BLOCK)
        // return true;
        //
        // if (AllBlocks.CART_ASSEMBLER.has(state))
        // return Direction.UP == direction;
        //
        // if (AllBlocks.GANTRY_CARRIAGE.has(state))
        // return state.getValue(DirectionalKineticBlock.FACING) == direction;
        //
        if (block instanceof BearingBlock bearing) {
            return bearing.getDirection(meta) == direction;
        }

        if (block instanceof AbstractChassisBlock chassis) {
            Boolean glueableSide = chassis.getGlueableSide(world, x, y, z, direction);
            if (glueableSide == null) return false;
            return glueableSide.booleanValue();
        }

        return false;
    }

    @Override
    public boolean hitByEntity(Entity entityIn) {
        return entityIn instanceof EntityPlayer player ? attackEntityFrom(DamageSource.causePlayerDamage(player), 0)
            : false;
    }

    // TODO
    // @Override
    // public Direction getDirection() {
    // return this.getFacingDirection();
    // }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable()) return false;

        boolean mobGriefing = worldObj.worldInfo.getGameRulesInstance()
            .getGameRuleBooleanValue("mobGriefing");
        Entity trueSource = source.getEntity();
        if (!mobGriefing && trueSource instanceof IMob) return false;

        Entity immediateSource = source.getSourceOfDamage();
        if (!isVisible() && immediateSource instanceof EntityPlayer player) {
            ItemStack heldItem = player.getHeldItem();
            if (heldItem == null || heldItem.getItem() != AllItems.super_glue) return true;
        }

        if (isEntityAlive() && !worldObj.isRemote) {
            onBroken(source.getEntity());
            kill();
            performHurtAnimation();
        }

        return true;
    }

    @Override
    public void addVelocity(double x, double y, double z) {
        if (!worldObj.isRemote && isEntityAlive() && x * x + y * y + z * z > 0.0D) {
            setDead();
            onBroken(null);
        }
    }

    @Override
    public ItemStack getPickedResult(MovingObjectPosition target) {
        return new ItemStack(AllItems.super_glue);
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (player instanceof FakePlayer) return false;
        blockPlacement = true;
        if (player.worldObj.isRemote) triggerPlaceBlock(player);
        blockPlacement = false;
        return true;
    }

    @SideOnly(Side.CLIENT)
    private void triggerPlaceBlock(EntityPlayer player) {
        if (!(player instanceof EntityPlayerSP)) return;
        if (!player.worldObj.isRemote) return;

        EntityPlayerSP cPlayer = (EntityPlayerSP) player;
        Minecraft mc = Minecraft.getMinecraft();
        mc.entityRenderer.getMouseOver(mc.timer.renderPartialTicks);
        MovingObjectPosition ray = mc.objectMouseOver;

        if (ray == null || ray.typeOfHit != MovingObjectType.BLOCK) return;
        BlockFace rayFace = new BlockFace(ray.blockX, ray.blockY, ray.blockZ, ray.sideHit);
        BlockFace hangingFace = new BlockFace(
            hangingPositionX,
            hangingPositionY,
            hangingPositionZ,
            getFacingDirection().getOpposite());
        if (!rayFace.isEquivalent(hangingFace)) return;

        ItemStack itemstack = cPlayer.getHeldItem();
        int countBefore = itemstack.stackSize;
        boolean actionResult = mc.playerController.onPlayerRightClick(
            cPlayer,
            cPlayer.worldObj,
            itemstack,
            ray.blockX,
            ray.blockY,
            ray.blockZ,
            ray.sideHit,
            ray.hitVec);
        if (!actionResult) return;

        cPlayer.swingItem();
        if (itemstack != null && (itemstack.stackSize != countBefore || cPlayer.capabilities.isCreativeMode))
            mc.entityRenderer.itemRenderer.updateEquippedItem();
        return;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        tagCompound.setByte(
            "Facing",
            (byte) this.getFacingDirection()
                .get3DDataValue());
        tagCompound.setInteger("TileX", this.hangingPositionX);
        tagCompound.setInteger("TileY", this.hangingPositionY);
        tagCompound.setInteger("TileZ", this.hangingPositionZ);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompound) {
        this.hangingPositionX = tagCompound.getInteger("TileX");
        this.hangingPositionY = tagCompound.getInteger("TileY");
        if (this.hangingPositionY < 0) kill();
        this.hangingPositionZ = tagCompound.getInteger("TileZ");
        this.facingDirection = Direction.from3DDataValue(tagCompound.getByte("Facing"));
        updateFacingWithBoundingBox();
    }

    @Override
    public EntityItem entityDropItem(ItemStack itemStackIn, float offsetY) {
        ChunkCoordinates normal = this.getFacingDirection()
            .getNormal();
        float xOffset = (float) normal.posX * 0.15F;
        float zOffset = (float) normal.posZ * 0.15F;
        EntityItem itementity = new EntityItem(
            this.worldObj,
            this.posX + xOffset,
            this.posY + yOffset,
            this.posZ + zOffset,
            itemStackIn);
        itementity.delayBeforeCanPickup = 10;
        this.worldObj.spawnEntityInWorld(itementity);
        return itementity;
    }

    // TODO
    // @Override
    // protected boolean repositionEntityAfterLoad() {
    // return false;
    // }

    @Override
    public void setPosition(double x, double y, double z) {
        hangingPositionX = (int) Math.floor(x);
        hangingPositionY = (int) Math.floor(y);
        hangingPositionZ = (int) Math.floor(z);
        updateBoundingBox();
        velocityChanged = true;
    }

    public float rotate(Rotation transformRotation) {
        if (this.getFacingDirection()
            .getAxis() != Direction.Axis.Y) {
            switch (transformRotation) {
                case CLOCKWISE_180:
                    facingDirection = facingDirection.getOpposite();
                    break;
                case COUNTERCLOCKWISE_90:
                    facingDirection = facingDirection.getCounterClockWise();
                    break;
                case CLOCKWISE_90:
                    facingDirection = facingDirection.getClockWise();
                default:
                    break;
            }
        }

        float f = this.rotationYaw % 360;
        switch (transformRotation) {
            case CLOCKWISE_180:
                return f + 180.0F;
            case COUNTERCLOCKWISE_90:
                return f + 90.0F;
            case CLOCKWISE_90:
                return f + 270.0F;
            default:
                return f;
        }
    }

    public int getHangingPositionX() {
        return this.hangingPositionX;
    }

    public int getHangingPositionY() {
        return this.hangingPositionY;
    }

    public int getHangingPositionZ() {
        return this.hangingPositionZ;
    }

    public Direction getAttachedDirection(int x, int y, int z) {
        return (hangingPositionX != x || hangingPositionY != y || hangingPositionZ != z) ? getFacingDirection()
            : getFacingDirection().getOpposite();
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt lightningBolt) {}

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeByte(
            this.getFacingDirection()
                .get3DDataValue());
        buffer.writeInt(this.hangingPositionX);
        buffer.writeInt(this.hangingPositionY);
        buffer.writeInt(this.hangingPositionZ);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.facingDirection = Direction.from3DDataValue(additionalData.readByte());
        this.hangingPositionX = additionalData.readInt();
        this.hangingPositionY = additionalData.readInt();
        this.hangingPositionZ = additionalData.readInt();
        this.updateFacingWithBoundingBox();
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !blockPlacement;
    }

    @Override
    protected void entityInit() {
        // TODO Auto-generated method stub

    }
}
