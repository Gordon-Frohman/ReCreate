package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class LinearChassisItemBlock extends ItemBlock {

    public LinearChassisItemBlock(Block block) {
        super(block);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
    }

    @Override
    public int getMetadata(int metadata) {
        return metadata & 1;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack.getItemDamage();
        if ((meta & 1) == 1) {
            return "tile.secondary_linear_chassis";
        }
        return super.getUnlocalizedName();
    }

}
