package su.sergiusonesimus.recreate.content;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.foundation.item.ItemDescription.Palette;
import su.sergiusonesimus.recreate.foundation.utility.Pair;

public enum AllSections {

    /** Create's kinetic mechanisms */
    KINETICS(Palette.Red),

    /** Item transport and other Utility */
    LOGISTICS(Palette.Yellow),

    /** Tools for strucuture movement and replication */
    SCHEMATICS(Palette.Blue),

    /** Decorative blocks */
    PALETTES(Palette.Green),

    /** Helpful gadgets and other shenanigans */
    CURIOSITIES(Palette.Purple),

    /** Base materials, ingredients and tools */
    MATERIALS(Palette.Green),

    /** Fallback section */
    UNASSIGNED(Palette.Gray)

    ;

    private Palette tooltipPalette;

    private AllSections(Palette tooltipPalette) {
        this.tooltipPalette = tooltipPalette;
    }

    public Palette getTooltipPalette() {
        return tooltipPalette;
    }

    public static AllSections of(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock) return ofBlock(((ItemBlock) item).field_150939_a);
        return ofItem(item, item.isDamageable() ? 0 : stack.getItemDamage());
    }

    static AllSections ofItem(Item item) {
        return ofItem(item, 0);
    }

    static AllSections ofItem(Item item, Integer meta) {
        return ReCreate.REGISTRATE.getSection(Pair.of(item, meta));
    }

    static AllSections ofBlock(Block block) {
        return ofBlock(block, 0);
    }

    static AllSections ofBlock(Block block, Integer meta) {
        return ReCreate.REGISTRATE.getSection(Pair.of(block, meta));
    }

}
