package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.util.VecHelper;

public class SuperGlueItem extends Item {

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 99;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        Direction direction = Direction.from3DDataValue(side);
        ChunkCoordinates normal = direction.getNormal();
        int dX = x + normal.posX;
        int dY = y + normal.posY;
        int dZ = z + normal.posZ;

        if (player == null || !this.canPlace(player, item, dX, dY, dZ, side)) return false;

        SuperGlueEntity entity = new SuperGlueEntity(world, dX, dY, dZ, direction);
        // TODO probably useless on 1.7.10
        // NBTTagCompound compoundnbt = item.getTagCompound();
        // if (compoundnbt != null)
        // EntityType.updateCustomEntityTag(world, player, entity, compoundnbt);

        if (!entity.onValidSurface()) return false;

        if (!world.isRemote) {
            world.spawnEntityInWorld(entity);
            entity.playPlaceSound();
        }
        item.damageItem(1, player);

        return true;
    }

    public static void onBroken(EntityPlayer EntityPlayer) {

    }

    protected boolean canPlace(EntityPlayer entity, ItemStack stack, int x, int y, int z, int side) {
        return y < entity.worldObj.getHeight() && entity.canPlayerEdit(x, y, z, side, stack);
    }

    @SideOnly(Side.CLIENT)
    public static void spawnParticles(World world, int x, int y, int z, Direction direction, boolean fullBlock) {
        ChunkCoordinates normal = direction.getNormal();
        Vec3 vec = Vec3.createVectorHelper(normal.posX, normal.posY, normal.posZ);
        Vec3 plane = VecHelper.axisAlingedPlaneOf(vec);
        float multiplier = .5f;
        vec = Vec3.createVectorHelper(vec.xCoord * multiplier, vec.yCoord * multiplier, vec.zCoord * multiplier);
        Vec3 facePos = VecHelper.getCenterOf(x, y, z)
            .addVector(vec.xCoord, vec.yCoord, vec.zCoord);

        float distance = fullBlock ? 1f : .25f + .25f * (world.rand.nextFloat() - .5f);
        plane = Vec3.createVectorHelper(plane.xCoord * distance, plane.yCoord * distance, plane.zCoord * distance);
        String particle = "iconcrack_" + Item.getIdFromItem(Items.slime_ball);

        for (int i = fullBlock ? 40 : 15; i > 0; i--) {
            Vec3 offset = VecHelper.rotate(plane, 360 * world.rand.nextFloat(), direction.getAxis());
            Vec3 motion = offset.normalize();
            multiplier = 1 / 16f;
            motion = Vec3
                .createVectorHelper(motion.xCoord * multiplier, motion.yCoord * multiplier, motion.zCoord * multiplier);
            if (fullBlock) offset = Vec3.createVectorHelper(
                MathHelper.clamp_double(offset.xCoord, -.5, .5),
                MathHelper.clamp_double(offset.yCoord, -.5, .5),
                MathHelper.clamp_double(offset.zCoord, -.5, .5));
            Vec3 particlePos = facePos.addVector(offset.xCoord, offset.yCoord, offset.zCoord);
            world.spawnParticle(
                particle,
                particlePos.xCoord,
                particlePos.yCoord,
                particlePos.zCoord,
                motion.xCoord,
                motion.yCoord,
                motion.zCoord);
        }

    }

}
