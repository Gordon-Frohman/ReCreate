package su.sergiusonesimus.recreate.content.contraptions.relays.gearbox;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.AbstractEncasedShaftBlock;
import su.sergiusonesimus.recreate.foundation.block.ITE;
import su.sergiusonesimus.recreate.util.Direction;

public class GearboxBlock extends AbstractEncasedShaftBlock implements ITE<GearboxTileEntity> {

    public GearboxBlock(Material materialIn) {
        super(materialIn);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
        this.setStepSound(soundTypeWood);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        this.setBlockTextureName("planks_spruce");

    }

    @Override
    public Class<GearboxTileEntity> getTileEntityClass() {
        return GearboxTileEntity.class;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean hasShaftTowards(IBlockAccess world, int x, int y, int z, Direction face) {
        int meta = world.getBlockMetadata(x, y, z);
        if (face.getAxis() == this.getAxis(meta) || face.getAxis() == this.getSecondAxis(meta)) {
            return true;
        }
        return false;
    }

    public Direction.Axis getSecondAxis(int meta) {
        switch (meta) {
            default:
            case 0:
                return Direction.Axis.Z;
            case 1:
                return Direction.Axis.Y;
            case 2:
                return Direction.Axis.X;
        }
    }

    @Override
    public int getRenderType() {
        return ReCreate.proxy.getGearboxBlockRenderID();
    }
}
