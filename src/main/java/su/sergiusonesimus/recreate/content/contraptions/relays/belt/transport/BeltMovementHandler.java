package su.sergiusonesimus.recreate.content.contraptions.relays.belt.transport;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.content.contraptions.base.HorizontalKineticBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltPart;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltSlope;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltTileEntity;
import su.sergiusonesimus.recreate.zmixin.interfaces.IMixinPotionEffect;

public class BeltMovementHandler {

    public static class TransportedEntityInfo {

        int ticksSinceLastCollision;
        int lastCollidedPosX, lastCollidedPosY, lastCollidedPosZ;
        Block lastCollidedBlock;
        int lastCollidedBlockMeta;

        public TransportedEntityInfo(int collisionX, int collisionY, int collisionZ, Block belt, int beltMeta) {
            refresh(collisionX, collisionY, collisionZ, belt, beltMeta);
        }

        public void refresh(int collisionX, int collisionY, int collisionZ, Block belt, int beltMeta) {
            ticksSinceLastCollision = 0;
            lastCollidedPosX = collisionX;
            lastCollidedPosY = collisionY;
            lastCollidedPosZ = collisionZ;
            lastCollidedBlock = belt;
            lastCollidedBlockMeta = beltMeta;
        }

        public TransportedEntityInfo tick() {
            ticksSinceLastCollision++;
            return this;
        }

        public int getTicksSinceLastCollision() {
            return ticksSinceLastCollision;
        }
    }

    public static boolean canBeTransported(Entity entity) {
        if (entity.isDead) return false;
        if (entity instanceof EntityPlayer player && player.isSneaking()) return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    public static void transportEntity(BeltTileEntity beltTe, Entity entityIn, TransportedEntityInfo info) {
        int posX = info.lastCollidedPosX;
        int posY = info.lastCollidedPosY;
        int posZ = info.lastCollidedPosZ;
        World world = beltTe.getWorld();
        TileEntity te = world.getTileEntity(posX, posY, posZ);
        int passengerX = MathHelper.floor_double(entityIn.posX);
        int passengerY = MathHelper.floor_double(entityIn.boundingBox.minY);
        int passengerZ = MathHelper.floor_double(entityIn.posZ);
        TileEntity tileEntityBelowPassenger = world.getTileEntity(passengerX, passengerY, passengerZ);
        Block block = info.lastCollidedBlock;
        if (!(block instanceof HorizontalKineticBlock hkb)) return;
        int meta = info.lastCollidedBlockMeta;
        Direction movementFacing = Direction
            .get(beltTe.getSpeed() < 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, hkb.getAxis(meta));

        boolean collidedWithBelt = te instanceof BeltTileEntity;
        boolean betweenBelts = tileEntityBelowPassenger instanceof BeltTileEntity && tileEntityBelowPassenger != te;

        // Don't fight other Belts
        if (!collidedWithBelt || betweenBelts) return;

        // Too slow
        boolean notHorizontal = beltTe.slopeType != BeltSlope.HORIZONTAL;
        if (Math.abs(beltTe.getSpeed()) < 1) return;

        // Not on top
        if (entityIn.boundingBox.minY - .25f < posY) return;

        // Lock entities in place
        boolean isPlayer = entityIn instanceof EntityPlayer;
        if (entityIn instanceof EntityLivingBase elb && !isPlayer) elb.addPotionEffect(
            ((IMixinPotionEffect) new PotionEffect(Potion.moveSlowdown.id, 10, 1, false)).setIsVisible(false));

        final Direction beltFacing = hkb.getDirection(meta);
        final BeltSlope slope = beltTe.slopeType;
        final Axis axis = beltFacing.getAxis();
        float movementSpeed = beltTe.getBeltMovementSpeed();
        final Direction movementDirection = Direction
            .get(axis == Axis.X ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, axis);

        ChunkCoordinates centeringDirection = Direction.get(
            AxisDirection.POSITIVE,
            beltFacing.getClockWise()
                .getAxis())
            .getNormal();
        ChunkCoordinates normal = movementDirection.getNormal();
        Vec3 movement = Vec3
            .createVectorHelper(normal.posX * movementSpeed, normal.posY * movementSpeed, normal.posZ * movementSpeed);

        double diffCenter = axis == Axis.Z ? (posX + .5f - entityIn.posX) : (posZ + .5f - entityIn.posZ);
        if (Math.abs(diffCenter) > 48 / 64f) return;

        BeltPart part = beltTe.partType;
        float top = 13 / 16f;
        boolean onSlope = notHorizontal && (part == BeltPart.MIDDLE || part == BeltPart.PULLEY
            || part == (slope == BeltSlope.UPWARD ? BeltPart.END : BeltPart.START) && entityIn.posY - posY < top
            || part == (slope == BeltSlope.DOWNWARD ? BeltPart.START : BeltPart.END) && entityIn.posY - posY > top);

        boolean movingDown = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.DOWNWARD : BeltSlope.UPWARD);
        boolean movingUp = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.UPWARD : BeltSlope.DOWNWARD);

        if (beltFacing.getAxis() == Axis.Z) {
            boolean b = movingDown;
            movingDown = movingUp;
            movingUp = b;
        }

        if (movingUp) movement = movement
            .addVector(0, Math.abs(axis.choose(movement.xCoord, movement.yCoord, movement.zCoord)), 0);
        if (movingDown) movement = movement
            .addVector(0, -Math.abs(axis.choose(movement.xCoord, movement.yCoord, movement.zCoord)), 0);

        double multiplier = diffCenter * Math.min(Math.abs(movementSpeed), .1f) * 4;
        Vec3 centering = Vec3.createVectorHelper(
            centeringDirection.posX * multiplier,
            centeringDirection.posY * multiplier,
            centeringDirection.posZ * multiplier);

        if (!(entityIn instanceof EntityLivingBase elb) || elb.moveForward == 0 && elb.moveStrafing == 0)
            movement = movement.addVector(centering.xCoord, centering.yCoord, centering.zCoord);

        float step = entityIn.stepHeight;
        if (!isPlayer) entityIn.stepHeight = 1;

        // Entity Collisions
        if (Math.abs(movementSpeed) < .5f) {
            Vec3 checkDistance = movement.normalize();
            checkDistance.xCoord *= 0.5;
            checkDistance.yCoord *= 0.5;
            checkDistance.zCoord *= 0.5;
            AxisAlignedBB bb = entityIn.boundingBox;
            if (bb == null) return;
            AxisAlignedBB checkBB = AxisAlignedBB.getBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
            checkBB = checkBB.offset(checkDistance.xCoord, checkDistance.yCoord, checkDistance.zCoord)
                .expand(
                    -Math.abs(checkDistance.xCoord),
                    -Math.abs(checkDistance.yCoord),
                    -Math.abs(checkDistance.zCoord));
            List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(entityIn, checkBB);
            list.removeIf(e -> shouldIgnoreBlocking(entityIn, e));
            if (!list.isEmpty()) {
                entityIn.motionX = entityIn.motionY = entityIn.motionZ = 0;
                info.ticksSinceLastCollision--;
                return;
            }
        }

        entityIn.fallDistance = 0;

        if (movingUp) {
            float minVelocity = .13f;
            float yMovement = (float) -(Math.max(Math.abs(movement.yCoord), minVelocity));
            entityIn.moveEntity(0, yMovement, 0);
            entityIn.moveEntity(movement.xCoord, 0, movement.zCoord);
        } else if (movingDown) {
            entityIn.moveEntity(movement.xCoord, 0, movement.zCoord);
            entityIn.moveEntity(0, movement.yCoord, 0);
        } else {
            entityIn.moveEntity(movement.xCoord, movement.yCoord, movement.zCoord);
        }

        entityIn.onGround = true;

        if (!isPlayer) entityIn.stepHeight = step;

        boolean movedPastEndingSlope = onSlope && (world.getBlock(passengerX, passengerY, passengerZ) == AllBlocks.belt
            || world.getBlock(passengerX, passengerY - 1, passengerZ) == AllBlocks.belt);

        if (movedPastEndingSlope && !movingDown && Math.abs(movementSpeed) > 0)
            entityIn.setPosition(entityIn.posX, entityIn.posY + movement.yCoord, entityIn.posZ);
        if (movedPastEndingSlope) {
            entityIn.motionX = movement.xCoord;
            entityIn.motionY = movement.yCoord;
            entityIn.motionZ = movement.zCoord;
            entityIn.isAirBorne = true;
            entityIn.velocityChanged = true;
            // TODO May require addition packet sending for player entities
        }

    }

    public static boolean shouldIgnoreBlocking(Entity me, Entity other) {
        if (other instanceof EntityHanging) return true;
        return isRidingOrBeingRiddenBy(me, other);
    }

    public static boolean isRidingOrBeingRiddenBy(Entity me, Entity other) {
        Entity rider = me.riddenByEntity;
        return (rider != null) && (rider.equals(other) || isRidingOrBeingRiddenBy(rider, other));
    }

}
