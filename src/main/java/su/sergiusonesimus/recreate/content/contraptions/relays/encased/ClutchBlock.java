package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.material.Material;

import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.foundation.block.ITE;

@ParametersAreNonnullByDefault
public class ClutchBlock extends AbstractRedstoneShaftBlock implements ITE<ClutchTileEntity> {

    public ClutchBlock(Material materialIn) {
        super(materialIn);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
        this.setStepSound(soundTypeWood);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        this.setBlockTextureName("planks_spruce");
    }

    @Override
    public Class<ClutchTileEntity> getTileEntityClass() {
        return ClutchTileEntity.class;
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
    public int getRenderType() {
        return ReCreate.proxy.getClutchBlockRenderID();
    }
}
