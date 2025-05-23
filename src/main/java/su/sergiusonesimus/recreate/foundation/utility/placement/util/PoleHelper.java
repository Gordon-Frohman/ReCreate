package su.sergiusonesimus.recreate.foundation.utility.placement.util;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import su.sergiusonesimus.recreate.foundation.config.AllConfigs;
import su.sergiusonesimus.recreate.foundation.utility.Pair;
import su.sergiusonesimus.recreate.foundation.utility.placement.IPlacementHelper;
import su.sergiusonesimus.recreate.foundation.utility.placement.PlacementOffset;
import su.sergiusonesimus.recreate.util.Direction;
import su.sergiusonesimus.recreate.util.Direction.Axis;

public abstract class PoleHelper<T extends Comparable<T>> implements IPlacementHelper {

    protected final Predicate<Pair<Block, Integer>> blockPredicate;
    protected final Function<Pair<Block, Integer>, Axis> axisFunction;

    public PoleHelper(Predicate<Pair<Block, Integer>> blockPredicate,
        Function<Pair<Block, Integer>, Axis> axisFunction) {
        this.blockPredicate = blockPredicate;
        this.axisFunction = axisFunction;
    }

    public boolean matchesAxis(Block block, int meta, Axis axis) {
        if (!blockPredicate.test(Pair.of(block, meta))) return false;

        return axisFunction.apply(Pair.of(block, meta)) == axis;
    }

    public int attachedPoles(World world, int x, int y, int z, Direction direction) {
        ChunkCoordinates directionNormal = direction.getNormal();
        int newX = x + directionNormal.posX;
        int newY = y + directionNormal.posY;
        int newZ = z + directionNormal.posZ;
        Block block = world.getBlock(newX, newY, newZ);
        int meta = world.getBlockMetadata(newX, newY, newZ);
        int count = 0;
        while (matchesAxis(block, meta, direction.getAxis())) {
            count++;
            newX += directionNormal.posX;
            newY += directionNormal.posY;
            newZ += directionNormal.posZ;
            block = world.getBlock(newX, newY, newZ);
            meta = world.getBlockMetadata(newX, newY, newZ);
        }
        return count;
    }

    @Override
    public Predicate<Pair<Block, Integer>> getBlockPredicate() {
        return this.blockPredicate;
    }

    @Override
    public PlacementOffset getOffset(EntityPlayer player, World world, Block block, int meta, int x, int y, int z,
        MovingObjectPosition ray) {
        List<Direction> directions = IPlacementHelper
            .orderedByDistance(x, y, z, ray.hitVec, dir -> dir.getAxis() == axisFunction.apply(Pair.of(block, meta)));
        for (Direction dir : directions) {
            int range = AllConfigs.SERVER.curiosities.placementAssistRange;
            // TODO
            // if (player != null) {
            // AttributeInstance reach = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
            // if (reach != null && reach.hasModifier(ExtendoGripItem.singleRangeAttributeModifier))
            // range += 4;
            // }
            int poles = attachedPoles(world, x, y, z, dir);
            if (poles >= range) continue;

            ChunkCoordinates dirNormal = dir.getNormal();
            int newX = x + dirNormal.posX * (poles + 1);
            int newY = y + dirNormal.posY * (poles + 1);
            int newZ = z + dirNormal.posZ * (poles + 1);
            Block newBlock = world.getBlock(newX, newY, newZ);
            int newMeta = world.getBlockMetadata(newX, newY, newZ);

            if (newBlock.getMaterial()
                .isReplaceable()) return PlacementOffset.success(new ChunkCoordinates(newX, newY, newZ), bPair -> {
                    bPair.setSecond(meta);
                    return bPair;
                });

        }

        return PlacementOffset.fail();
    }
}
