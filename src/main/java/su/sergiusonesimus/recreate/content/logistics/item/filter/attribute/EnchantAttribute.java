package su.sergiusonesimus.recreate.content.logistics.item.filter.attribute;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;

import su.sergiusonesimus.recreate.content.logistics.item.filter.ItemAttribute;

public class EnchantAttribute implements ItemAttribute {

    public static final EnchantAttribute EMPTY = new EnchantAttribute(null);

    private final Enchantment enchantment;

    public EnchantAttribute(@Nullable Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        return EnchantmentHelper.getEnchantments(itemStack)
            .containsKey(enchantment);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        return ((Map<Integer, Integer>) EnchantmentHelper.getEnchantments(itemStack)).keySet()
            .stream()
            .map(enchId -> new EnchantAttribute(Enchantment.enchantmentsList[enchId]))
            .collect(Collectors.toList());
    }

    @Override
    public String getTranslationKey() {
        return "has_enchant";
    }

    @Override
    public Object[] getTranslationParameters() {
        String parameter = "";
        if (enchantment != null) parameter = new ChatComponentTranslation(enchantment.getName()).getUnformattedText();
        return new Object[] { parameter };
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        if (enchantment == null) return;
        Integer id = null;
        for (int i = 0; i < Enchantment.enchantmentsList.length; i++) {
            if (Enchantment.enchantmentsList[i] == enchantment) {
                id = i;
                break;
            }
        }
        if (id == null) return;
        nbt.setInteger("id", id);
    }

    @Override
    public ItemAttribute readNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("id")) {
            int id = nbt.getInteger("id");
            if (id >= 0 && id < Enchantment.enchantmentsList.length) {
                Enchantment enchantment = Enchantment.enchantmentsList[id];
                if (enchantment != null) {
                    return new EnchantAttribute(enchantment);
                }
            }
        }
        return EMPTY;
    }
}
