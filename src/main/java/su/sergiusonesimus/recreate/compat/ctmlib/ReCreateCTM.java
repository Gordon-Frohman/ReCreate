package su.sergiusonesimus.recreate.compat.ctmlib;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis.LinearChassisRenderBlocks;
import team.chisel.ctmlib.CTM;
import team.chisel.ctmlib.Dir;

public class ReCreateCTM extends CTM {

    LinearChassisRenderBlocks renderBlocks;

    public ReCreateCTM(LinearChassisRenderBlocks renderBlocks) {
        this.renderBlocks = renderBlocks;
    }

    public static CTM getInstance(LinearChassisRenderBlocks renderBlocks) {
        return new ReCreateCTM(renderBlocks);
    }

    public int[] getSubmapIndices(IBlockAccess world, int x, int y, int z, int side) {
        int[] ret = super.getSubmapIndices(world, x, y, z, side);

        int rotCount = 0;
        switch (Direction.from3DDataValue(side)) {
            case DOWN:
                rotCount = renderBlocks.uvRotateBottom % 4;
                break;
            case EAST:
                rotCount = renderBlocks.uvRotateEast % 4;
                break;
            case NORTH:
                rotCount = renderBlocks.uvRotateNorth % 4;
                break;
            case SOUTH:
                rotCount = renderBlocks.uvRotateSouth % 4;
                break;
            case UP:
                rotCount = renderBlocks.uvRotateTop % 4;
                break;
            case WEST:
                rotCount = renderBlocks.uvRotateWest % 4;
                break;
            default:
                break;
        }
        for (int i = 0; i < rotCount; i++) {
            int temp = ret[0];
            for (int j = 0; j < 3; j++) ret[j] = ret[j + 1];
            ret[3] = temp;
        }

        return ret;
    }

    public void buildConnectionMap(IBlockAccess world, int x, int y, int z, int side, Block block, int meta) {
        super.buildConnectionMap(world, x, y, z, side, block, meta);

        Boolean temp1;
        Boolean temp2;
        Dir dir;
        for (int i = 0; i < renderBlocks.getRotationCount(Direction.from3DDataValue(side)); i++) {
            temp1 = connectionMap.get(Dir.TOP);
            temp2 = connectionMap.get(Dir.TOP_RIGHT);
            for (int j = 0; j < Dir.VALUES.length - 2; j++) {
                dir = Dir.VALUES[j];
                connectionMap.put(dir, connectionMap.get(Dir.values()[dir.ordinal() + 2]));
            }
            connectionMap.put(Dir.LEFT, temp1);
            connectionMap.put(Dir.TOP_LEFT, temp2);
        }
    }

}
