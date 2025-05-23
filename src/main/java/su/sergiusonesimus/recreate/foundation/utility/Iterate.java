package su.sergiusonesimus.recreate.foundation.utility;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.util.ChunkCoordinates;

import su.sergiusonesimus.recreate.util.Direction;
import su.sergiusonesimus.recreate.util.Direction.Axis;

public class Iterate {

    public static final boolean[] trueAndFalse = { true, false };
    public static final boolean[] falseAndTrue = { false, true };
    public static final int[] zeroAndOne = { 0, 1 };
    public static final int[] positiveAndNegative = { 1, -1 };
    public static final Direction[] directions = Direction.values();
    public static final Direction[] horizontalDirections = getHorizontals();
    public static final Axis[] axes = Axis.values();
    public static final EnumSet<Direction.Axis> axisSet = EnumSet.allOf(Direction.Axis.class);

    private static Direction[] getHorizontals() {
        Direction[] directions = new Direction[4];
        for (int i = 0; i < 4; i++) directions[i] = Direction.from2DDataValue(i);
        return directions;
    }

    public static Direction[] directionsInAxis(Axis axis) {
        switch (axis) {
            case X:
                return new Direction[] { Direction.EAST, Direction.WEST };
            case Y:
                return new Direction[] { Direction.UP, Direction.DOWN };
            default:
            case Z:
                return new Direction[] { Direction.SOUTH, Direction.NORTH };
        }
    }

    public static List<ChunkCoordinates> hereAndBelow(ChunkCoordinates pos) {
        return Arrays.asList(pos, new ChunkCoordinates(pos.posX, pos.posY - 1, pos.posZ));
    }

    public static List<ChunkCoordinates> hereBelowAndAbove(ChunkCoordinates pos) {
        return Arrays.asList(
            pos,
            new ChunkCoordinates(pos.posX, pos.posY - 1, pos.posZ),
            new ChunkCoordinates(pos.posX, pos.posY + 1, pos.posZ));
    }
}
