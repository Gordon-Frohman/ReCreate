package su.sergiusonesimus.recreate.content.schematics;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

public interface ISpecialBlockItemRequirement {

    default ItemRequirement getRequiredItems(Block block, int meta, TileEntity te) {
        return ItemRequirement.INVALID;
    }

}
