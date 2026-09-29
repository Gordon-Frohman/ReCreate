package su.sergiusonesimus.recreate.content.logistics.item.filter.attribute;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemFireworkCharge;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;

import su.sergiusonesimus.recreate.content.logistics.item.filter.ItemAttribute;

public class ColorAttribute implements ItemAttribute {

    public static final ColorAttribute EMPTY = new ColorAttribute(5 /* purple */);

    public final int color;

    public ColorAttribute(int color) {
        this.color = color;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        return findMatchingDyeColors(itemStack).stream()
            .anyMatch(otherColor -> otherColor == color);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        return findMatchingDyeColors(itemStack).stream()
            .map(ColorAttribute::new)
            .collect(Collectors.toList());
    }

    private Collection<Integer> findMatchingDyeColors(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();

        boolean returnSingleton = false;
        Item item = stack.getItem();
        if (item instanceof ItemDye) {
            returnSingleton = true;
        } else if (item instanceof ItemBlock) {
            Block block = Block.getBlockFromItem(item);
            returnSingleton = block instanceof BlockColored || block instanceof BlockStainedGlass
                || block instanceof BlockStainedGlassPane
                || block instanceof BlockCarpet;
        }
        if (returnSingleton) return Collections.singletonList(stack.getItemDamage());

        Set<Integer> colors = new HashSet<>();
        if (stack.getItem() instanceof ItemFirework && nbt != null) {
            NBTTagList listnbt = nbt.getCompoundTag("Fireworks")
                .getTagList("Explosions", 10);
            for (int i = 0; i < listnbt.tagCount(); i++) {
                colors.addAll(getFireworkStarColors(listnbt.getCompoundTagAt(i)));
            }
        }

        if (stack.getItem() instanceof ItemFireworkCharge && nbt != null) {
            colors.addAll(getFireworkStarColors(nbt.getCompoundTag("Explosion")));
        }

        for (int i = 0; i < ItemDye.field_150923_a.length; i++) {
            if (StatCollector.translateToFallback(
                stack.getItem()
                    .getUnlocalizedName(stack))
                .startsWith(StatCollector.translateToFallback("item.fireworksCharge." + ItemDye.field_150923_a[i]))) {
                colors.add(i);
            }
        }

        return colors;
    }

    private Collection<Integer> getFireworkStarColors(NBTTagCompound compound) {
        Set<Integer> colors = new HashSet<>();
        Arrays.stream(compound.getIntArray("Colors"))
            .forEach(colors::add);
        Arrays.stream(compound.getIntArray("FadeColors"))
            .forEach(colors::add);
        return colors;
    }

    @Override
    public String getTranslationKey() {
        return "color";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[] { /* TODO new ChatComponentTranslation( */"item.fireworksCharge."
            + ItemDye.field_150923_a[color]/* TODO ).getContents() */ };
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        nbt.setInteger("id", color);
    }

    @Override
    public ItemAttribute readNBT(NBTTagCompound nbt) {
        return nbt.hasKey("id") ? new ColorAttribute(nbt.getInteger("id")) : EMPTY;
    }
}
