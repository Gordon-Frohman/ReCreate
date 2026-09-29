package su.sergiusonesimus.recreate.content.schematics;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import su.sergiusonesimus.recreate.foundation.tileentity.SmartTileEntity;

public class ItemRequirement {

    public enum ItemUseType {
        CONSUME,
        DAMAGE
    }

    public static class StackRequirement {

        public final ItemStack item;
        public final ItemUseType usage;

        public StackRequirement(ItemUseType usage, ItemStack item) {
            this.item = item;
            this.usage = usage;
        }
    }

    List<StackRequirement> requiredItems;

    public static ItemRequirement INVALID = new ItemRequirement();
    public static ItemRequirement NONE = new ItemRequirement();

    private ItemRequirement() {}

    public ItemRequirement(List<StackRequirement> requiredItems) {
        this.requiredItems = requiredItems;
    }

    public ItemRequirement(ItemUseType usage, ItemStack items) {
        this(Arrays.asList(new StackRequirement(usage, items)));
    }

    public ItemRequirement(ItemUseType usage, Item item) {
        this(usage, new ItemStack(item));
    }

    public ItemRequirement(ItemUseType usage, List<ItemStack> requiredItems) {
        this(
            requiredItems.stream()
                .map(req -> new StackRequirement(usage, req))
                .collect(Collectors.toList()));
    }

    public static ItemRequirement of(Block block, int meta, TileEntity te) {
        ItemRequirement baseRequirement;
        if (block instanceof ISpecialBlockItemRequirement sbir) {
            baseRequirement = sbir.getRequiredItems(block, meta, te);
        } else {
            baseRequirement = ofBlockState(block, meta);
        }

        // Behaviours can add additional required items
        if (te instanceof SmartTileEntity ste) baseRequirement = baseRequirement.with(ste.getRequiredItems());

        return baseRequirement;
    }

    private static ItemRequirement ofBlockState(Block block, int meta) {
        if (block == Blocks.air) return NONE;

        Item item = Item.getItemFromBlock(block);

        // double slab needs two items
        if (block instanceof BlockSlab slab && slab.field_150004_a)
            return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(item, 2)));
        // TODO
        // if (block instanceof TurtleEggBlock)
        // return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(item,
        // state.getValue(TurtleEggBlock.EGGS).intValue())));
        // if (block instanceof SeaPickleBlock)
        // return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(item,
        // state.getValue(SeaPickleBlock.PICKLES).intValue())));
        if (block instanceof BlockSnow)
            return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(item, meta)));
        if (block instanceof BlockFarmland /* TODO || block instanceof DirtPathBlock */) return new ItemRequirement(
            ItemUseType.CONSUME,
            Arrays.asList(new ItemStack(Item.getItemFromBlock(Blocks.dirt))));

        return item == null ? INVALID : new ItemRequirement(ItemUseType.CONSUME, item);
    }

    public static ItemRequirement of(Entity entity) {
        if (entity instanceof ISpecialEntityItemRequirement)
            return ((ISpecialEntityItemRequirement) entity).getRequiredItems();

        if (entity instanceof EntityItemFrame ife) {
            ItemStack frame = new ItemStack(Items.item_frame);
            ItemStack displayedItem = ife.getDisplayedItem();
            if (displayedItem == null) return new ItemRequirement(ItemUseType.CONSUME, Items.item_frame);
            return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(frame, displayedItem));
        }

        if (entity instanceof EntityPainting) return new ItemRequirement(ItemUseType.CONSUME, Items.painting);

        // TODO
        // if (type == EntityType.ARMOR_STAND) {
        // List<ItemStack> requirements = new ArrayList<>();
        // ArmorStand armorStandEntity = (ArmorStand) entity;
        // armorStandEntity.getAllSlots().forEach(requirements::add);
        // requirements.add(new ItemStack(Items.ARMOR_STAND));
        // return new ItemRequirement(ItemUseType.CONSUME, requirements);
        // }

        if (entity instanceof EntityMinecart minecartEntity) {
            return new ItemRequirement(
                ItemUseType.CONSUME,
                minecartEntity.getCartItem()
                    .getItem());
        }

        if (entity instanceof EntityBoat boatEntity) {
            return new ItemRequirement(ItemUseType.CONSUME, /* TODO More boat types for EFR */ Items.boat);
        }

        // TODO
        // if (entity instanceof EntityEnderCrystal)
        // return new ItemRequirement(ItemUseType.CONSUME, Items.END_CRYSTAL);

        return INVALID;
    }

    public boolean isEmpty() {
        return NONE == this;
    }

    public boolean isInvalid() {
        return INVALID == this;
    }

    public List<StackRequirement> getRequiredItems() {
        return requiredItems;
    }

    public static boolean validate(ItemStack required, ItemStack present) {
        return required == null || (present != null && required.getItem() == present.getItem());
    }

    public ItemRequirement with(ItemRequirement other) {
        if (this.isInvalid() || other.isInvalid()) return INVALID;
        if (this.isEmpty()) return other;
        if (other.isEmpty()) return this;

        return new ItemRequirement(
            Stream.concat(requiredItems.stream(), other.requiredItems.stream())
                .collect(Collectors.toList()));
    }

}
