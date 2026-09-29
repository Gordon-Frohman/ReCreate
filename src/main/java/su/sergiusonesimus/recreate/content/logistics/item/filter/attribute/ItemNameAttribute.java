package su.sergiusonesimus.recreate.content.logistics.item.filter.attribute;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import su.sergiusonesimus.recreate.content.logistics.item.filter.ItemAttribute;

public class ItemNameAttribute implements ItemAttribute {

    String itemName;

    public ItemNameAttribute(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        return extractCustomName(itemStack).equals(itemName);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        String name = extractCustomName(itemStack);

        List<ItemAttribute> atts = new ArrayList<>();
        if (name.length() > 0) {
            atts.add(new ItemNameAttribute(name));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "has_name";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[] { itemName };
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        nbt.setString("name", this.itemName);
    }

    @Override
    public ItemAttribute readNBT(NBTTagCompound nbt) {
        return new ItemNameAttribute(nbt.getString("name"));
    }

    private String extractCustomName(ItemStack stack) {
        if (stack.hasTagCompound()) {
            NBTTagCompound compoundnbt = stack.getTagCompound()
                .getCompoundTag("display");
            if (compoundnbt != null && compoundnbt.hasKey("Name", 8)) {
                return compoundnbt.getString("Name");
            }
        }
        return "";
    }
}
