package su.sergiusonesimus.recreate.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictHelper {

    public static List<String> getOreNames(ItemStack stack) {
        List<String> result = new ArrayList<String>();
        if (stack != null) {
            for (int id : OreDictionary.getOreIDs(stack)) {
                result.add(OreDictionary.getOreName(id));
            }
        }
        return result;
    }

}
