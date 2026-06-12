package su.sergiusonesimus.recreate.content.contraptions.base;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class CasingItemBlock extends ItemBlock {

    public CasingItemBlock(Block block) {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "tile." + CasingBlock.getTypeFromMeta(stack.getItemDamage())
            .getCasingName();
    }

    @Override
    public int getMetadata(int itemDamage) {
        return itemDamage;
    }

}
