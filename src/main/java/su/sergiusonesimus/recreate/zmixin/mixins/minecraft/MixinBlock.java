package su.sergiusonesimus.recreate.zmixin.mixins.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.recreate.zmixin.interfaces.IMixinBlock;

@Mixin(Block.class)
public class MixinBlock implements IMixinBlock {

    @Shadow(remap = true)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        return null;
    }

    public List<AxisAlignedBB> getSelectedBoundingBoxesList(World worldIn, int x, int y, int z) {
        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        list.add(getSelectedBoundingBoxFromPool(worldIn, x, y, z));
        return list;
    }

}
