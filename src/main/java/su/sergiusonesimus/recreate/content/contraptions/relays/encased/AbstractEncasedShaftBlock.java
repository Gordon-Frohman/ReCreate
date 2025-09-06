package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.content.contraptions.base.RotatedPillarKineticBlock;

public class AbstractEncasedShaftBlock extends RotatedPillarKineticBlock {

    public AbstractEncasedShaftBlock(Material materialIn) {
        super(materialIn);
    }

    @Override
    public boolean hasShaftTowards(IBlockAccess world, int x, int y, int z, Direction face) {
        return face.getAxis() == this.getAxis(world.getBlockMetadata(x, y, z));
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        if (placer != null && placer.isSneaking()) super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
        Axis preferredAxis = getPreferredAxis(worldIn, x, y, z);
        worldIn.setBlockMetadataWithNotify(
            x,
            y,
            z,
            this.getMetaFromAxis(
                preferredAxis == null ? Direction.getNearest(placer.getLookVec())
                    .getAxis() : preferredAxis),
            2);
    }

}
