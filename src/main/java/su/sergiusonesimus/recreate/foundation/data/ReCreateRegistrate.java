package su.sergiusonesimus.recreate.foundation.data;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import su.sergiusonesimus.recreate.content.AllSections;
import su.sergiusonesimus.recreate.foundation.utility.Pair;

public class ReCreateRegistrate {

    /* Section Tracking */

    private static Map<Object, AllSections> sectionLookup = new HashMap<>();
    private AllSections section;

    public ReCreateRegistrate startSection(AllSections section) {
        this.section = section;
        return this;
    }

    public AllSections currentSection() {
        return section;
    }

    public void addToSection(Object entry, AllSections section) {
        Object first = null;
        int second = 0;
        if (entry instanceof ItemStack stack) {
            Item item = stack.getItem();
            second = stack.getItemDamage();
            if (item instanceof ItemBlock ib) first = Block.getBlockFromItem(ib);
            else first = item;
        } else {
            if (entry instanceof Item || entry instanceof Block) first = entry;
        }
        if (first != null) sectionLookup.put(Pair.of(first, second), section);
    }

    public AllSections getSection(Object entry) {
        return sectionLookup.getOrDefault(entry, AllSections.UNASSIGNED);
    }

}
