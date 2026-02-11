package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.compat.ctmlib.ReCreateCTM;
import team.chisel.ctmlib.RenderBlocksCTM;

public class LinearChassisRenderBlocks extends RenderBlocksCTM {

    private int storedX;
    private int storedY;
    private int storedZ;

    private Map<SubSide, Vert[]> storedVertices = new HashMap<SubSide, Vert[]>();

    private Direction currentFace;
    private final int[][] lightVertices = { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, 1 } };

    public LinearChassisRenderBlocks() {
        this.ctm = ReCreateCTM.getInstance(this);
    }

    @Override
    public boolean renderStandardBlock(Block block, int x, int y, int z) {
        if (!(block instanceof LinearChassisBlock chassis)) return false;
        storedX = x;
        storedY = y;
        storedZ = z;

        switch (chassis.getAxis(Minecraft.getMinecraft().theWorld.getBlockMetadata(x, y, z))) {
            default:
            case Y:
                break;
            case X:
                uvRotateSouth = 1;
                uvRotateNorth = 1;
                uvRotateTop = 1;
                uvRotateBottom = 1;
                break;
            case Z:
                uvRotateEast = 1;
                uvRotateWest = 1;
                break;
        }

        boolean result = super.renderStandardBlock(block, x, y, z);

        uvRotateSouth = 0;
        uvRotateEast = 0;
        uvRotateWest = 0;
        uvRotateNorth = 0;
        uvRotateTop = 0;
        uvRotateBottom = 0;

        return result;
    }

    private void selectCurrentSubmap(Block block, Direction face) {
        if (!inWorld || !(block instanceof LinearChassisBlock chassis)) return;
        World world = Minecraft.getMinecraft().theWorld;
        int meta = world.getBlockMetadata(storedX, storedY, storedZ);
        if (Direction.from3DDataValue(meta)
            .getAxis() == face.getAxis()) {
            if (chassis.getGlueableSide(world, storedX, storedY, storedZ, face)) {
                submap = LinearChassisSubmapManager.chassisEndSticky;
                submapSmall = LinearChassisSubmapManager.chassisEndStickySmall;
            } else {
                submap = LinearChassisSubmapManager.chassisEnd;
                submapSmall = LinearChassisSubmapManager.chassisEndSmall;
            }
        } else {
            if ((meta & 3) == 1) {
                submap = LinearChassisSubmapManager.chassisSide2;
                submapSmall = LinearChassisSubmapManager.chassisSide2Small;
            } else {
                submap = LinearChassisSubmapManager.chassisSide1;
                submapSmall = LinearChassisSubmapManager.chassisSide1Small;
            }
        }
    }

    private SubSide rotateSubSide(SubSide side) {
        try {
            Field xmin = SubSide.class.getDeclaredField("xmin");
            xmin.setAccessible(true);
            Field xmax = SubSide.class.getDeclaredField("xmax");
            xmax.setAccessible(true);
            Field ymin = SubSide.class.getDeclaredField("ymin");
            ymin.setAccessible(true);
            Field ymax = SubSide.class.getDeclaredField("ymax");
            ymax.setAccessible(true);
            Vert[] vertices = new Vert[] { (Vert) xmin.get(side), (Vert) ymin.get(side), (Vert) ymax.get(side),
                (Vert) xmax.get(side) };
            if (!storedVertices.containsKey(side)) storedVertices.put(side, vertices);
            xmin.set(side, vertices[3]);
            ymin.set(side, vertices[0]);
            ymax.set(side, vertices[1]);
            xmax.set(side, vertices[2]);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return side;
    }

    private SubSide resetSubSide(SubSide side) {
        try {
            Field xmin = SubSide.class.getDeclaredField("xmin");
            xmin.setAccessible(true);
            Field xmax = SubSide.class.getDeclaredField("xmax");
            xmax.setAccessible(true);
            Field ymin = SubSide.class.getDeclaredField("ymin");
            ymin.setAccessible(true);
            Field ymax = SubSide.class.getDeclaredField("ymax");
            ymax.setAccessible(true);
            Vert[] storedValues = storedVertices.get(side);
            xmin.set(side, storedValues[0]);
            ymin.set(side, storedValues[1]);
            ymax.set(side, storedValues[2]);
            xmax.set(side, storedValues[3]);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return side;
    }

    protected void side(Block block, SubSide side, int iconIndex) {
        int rotCount = 0;
        try {
            Field normal;
            normal = SubSide.class.getDeclaredField("normal");
            normal.setAccessible(true);
            switch ((ForgeDirection) normal.get(side)) {
                case DOWN:
                    rotCount = uvRotateBottom % 4;
                    break;
                case EAST:
                    rotCount = uvRotateEast % 4;
                    break;
                case NORTH:
                    rotCount = uvRotateNorth % 4;
                    break;
                case SOUTH:
                    rotCount = uvRotateSouth % 4;
                    break;
                case UP:
                    rotCount = uvRotateTop % 4;
                    break;
                case WEST:
                    rotCount = uvRotateWest % 4;
                    break;
                default:
                case UNKNOWN:
                    break;
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < rotCount; i++) rotateSubSide(side);
        super.side(block, side, iconIndex);
        if (rotCount > 0) resetSubSide(side);
    }

    protected void fillLightmap(int bottomLeft, int bottomRight, int topRight, int topLeft) {
        int temp;
        for (int i = 0; i < getRotationCount(currentFace); i++) {
            temp = topLeft;
            topLeft = topRight;
            topRight = bottomRight;
            bottomRight = bottomLeft;
            bottomLeft = temp;
        }
        super.fillLightmap(bottomLeft, bottomRight, topRight, topLeft);
    }

    protected void fillColormap(float bottomLeft, float bottomRight, float topRight, float topLeft, float[][] map) {
        float temp;
        for (int i = 0; i < getRotationCount(currentFace); i++) {
            temp = topLeft;
            topLeft = topRight;
            topRight = bottomRight;
            bottomRight = bottomLeft;
            bottomLeft = temp;
        }
        super.fillColormap(bottomLeft, bottomRight, topRight, topLeft, map);
    }

    protected void getLight(int x, int y) {
        int lightType = 0;
        for (int i = 1; i < lightVertices.length; i++) {
            if (lightVertices[i][0] == x && lightVertices[i][1] == y) {
                lightType = i;
                break;
            }
        }

        lightType = (lightType + getRotationCount(currentFace)) % 4;
        super.getLight(lightVertices[lightType][0], lightVertices[lightType][1]);
    }

    public int getRotationCount(Direction dir) {
        int result = 0;
        switch (dir) {
            case DOWN:
                result = uvRotateBottom % 4;
                break;
            case EAST:
                result = uvRotateEast % 4;
                break;
            case NORTH:
                result = uvRotateNorth % 4;
                break;
            case SOUTH:
                result = uvRotateSouth % 4;
                break;
            case UP:
                result = uvRotateTop % 4;
                break;
            case WEST:
                result = uvRotateWest % 4;
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public void renderFaceXNeg(Block block, double x, double y, double z, IIcon icon) {
        currentFace = Direction.WEST;
        selectCurrentSubmap(block, currentFace);
        super.renderFaceXNeg(block, x, y, z, icon);
    }

    @Override
    public void renderFaceXPos(Block block, double x, double y, double z, IIcon icon) {
        currentFace = Direction.EAST;
        selectCurrentSubmap(block, currentFace);
        super.renderFaceXPos(block, x, y, z, icon);
    }

    @Override
    public void renderFaceZNeg(Block block, double x, double y, double z, IIcon icon) {
        currentFace = Direction.NORTH;
        selectCurrentSubmap(block, currentFace);
        super.renderFaceZNeg(block, x, y, z, icon);
    }

    @Override
    public void renderFaceZPos(Block block, double x, double y, double z, IIcon icon) {
        currentFace = Direction.SOUTH;
        selectCurrentSubmap(block, currentFace);
        super.renderFaceZPos(block, x, y, z, icon);
    }

    @Override
    public void renderFaceYNeg(Block block, double x, double y, double z, IIcon icon) {
        currentFace = Direction.DOWN;
        selectCurrentSubmap(block, currentFace);
        super.renderFaceYNeg(block, x, y, z, icon);
    }

    @Override
    public void renderFaceYPos(Block block, double x, double y, double z, IIcon icon) {
        currentFace = Direction.UP;
        selectCurrentSubmap(block, currentFace);
        super.renderFaceYPos(block, x, y, z, icon);
    }

}
