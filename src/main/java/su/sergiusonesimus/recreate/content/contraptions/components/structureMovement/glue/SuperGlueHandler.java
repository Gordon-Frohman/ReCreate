package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.foundation.networking.AllPackets;

public class SuperGlueHandler {

    @SuppressWarnings("unchecked")
    public static Map<Direction, SuperGlueEntity> gatherGlue(World world, int x, int y, int z) {
        List<SuperGlueEntity> entities = world
            .getEntitiesWithinAABB(SuperGlueEntity.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1));
        Map<Direction, SuperGlueEntity> map = new HashMap<>();
        for (SuperGlueEntity entity : entities) map.put(entity.getAttachedDirection(x, y, z), entity);
        return map;
    }

    @SubscribeEvent
    public void glueListensForBlockPlacement(PlaceEvent event) {
        World world = event.world;
        Entity entity = event.player;
        BlockSnapshot snapshot = event.blockSnapshot;

        if (entity == null || world == null || snapshot == null) return;
        if (world.isRemote) return;

        int x = event.blockSnapshot.x;
        int y = event.blockSnapshot.y;
        int z = event.blockSnapshot.z;

        Map<Direction, SuperGlueEntity> gatheredGlue = gatherGlue(world, x, y, z);
        for (Direction direction : gatheredGlue.keySet())
            AllPackets.channel.sendToAll(new GlueEffectPacket(x, y, z, direction, true));

        // TODO
        // if (entity instanceof EntityPlayer) glueInOffHandAppliesOnBlockPlace(event, x, y, z, (EntityPlayer) entity);
    }

    // TODO May later add Offhand mod compatibility
    // public static void glueInOffHandAppliesOnBlockPlace(PlaceEvent event, int x, int y, int z,
    // EntityPlayer placer) {
    // ItemStack itemstack = placer.getOffhandItem();
    // AttributeInstance reachAttribute = placer.getAttribute(ForgeMod.REACH_DISTANCE.get());
    // if (!AllItems.super_glue.isIn(itemstack) || reachAttribute == null)
    // return;
    // if (AllItems.wrench.isIn(placer.getMainHandItem()))
    // return;
    // if (event.getPlacedAgainst() == IPlacementHelper.ID)
    // return;
    //
    // double distance = reachAttribute.getValue();
    // Vec3 start = placer.getEyePosition(1);
    // Vec3 look = placer.getViewVector(1);
    // Vec3 end = start.add(look.x * distance, look.y * distance, look.z * distance);
    // World world = placer.level;
    //
    // RayTraceWorld rayTraceWorld =
    // new RayTraceWorld(world, (p, state) -> p.equals(pos) ? Blocks.AIR.defaultBlockState() : state);
    // MovingObjectPosition ray = rayTraceWorld.clip(
    // new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, placer));
    //
    // Direction face = ray.getDirection();
    // if (face == null || ray.getType() == Type.MISS)
    // return;
    //
    // if (!ray.getBlockPos()
    // .relative(face)
    // .equals(pos)) {
    // event.setCanceled(true);
    // return;
    // }
    //
    // SuperGlueEntity entity = new SuperGlueEntity(world, ray.getBlockPos(), face.getOpposite());
    // NBTTagCompound compoundnbt = itemstack.getTag();
    // if (compoundnbt != null)
    // EntityType.updateCustomEntityTag(world, placer, entity, compoundnbt);
    //
    // if (entity.onValidSurface()) {
    // if (!world.isRemote) {
    // entity.playPlaceSound();
    // world.addFreshEntity(entity);
    // AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
    // new GlueEffectPacket(ray.getBlockPos(), face, true));
    // }
    // itemstack.hurtAndBreak(1, placer, SuperGlueItem::onBroken);
    // }
    // }

}
