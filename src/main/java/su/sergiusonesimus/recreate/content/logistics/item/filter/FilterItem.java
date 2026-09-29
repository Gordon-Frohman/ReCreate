package su.sergiusonesimus.recreate.content.logistics.item.filter;

import static su.sergiusonesimus.recreate.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.recreate.AllItems;
import su.sergiusonesimus.recreate.AllKeys;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.processing.EmptyingByBasin;
import su.sergiusonesimus.recreate.content.logistics.item.filter.AttributeFilterScreen.WhitelistMode;
import su.sergiusonesimus.recreate.foundation.gui.AllGuiTextures;
import su.sergiusonesimus.recreate.foundation.utility.Lang;

public class FilterItem extends Item implements IGuiHolder<PlayerInventoryGuiData> {

    private FilterType type;

    public enum FilterType {
        REGULAR,
        ATTRIBUTE;
    }

    public static FilterItem regular() {
        return new FilterItem(FilterType.REGULAR);
    }

    public static FilterItem attribute() {
        return new FilterItem(FilterType.ATTRIBUTE);
    }

    private FilterItem(FilterType type) {
        this.type = type;
    }

    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    @Override
    public ModularScreen createScreen(PlayerInventoryGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(ReCreate.ID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        AllGuiTextures background;
        SlotGroupWidget filterInventory;

        if (this.type == FilterType.REGULAR) {
            background = AllGuiTextures.FILTER;
            filterInventory = FilterScreen.filterInventory(guiData, guiSyncManager);
        } else {
            background = AllGuiTextures.ATTRIBUTE_FILTER;
            filterInventory = AttributeFilterScreen.attributeFilterInventory(guiData, guiSyncManager);
        }

        ModularPanel mainPanel = ModularPanel
            .defaultPanel("filter_gui", PLAYER_INVENTORY.width, background.height + 4 + PLAYER_INVENTORY.height)
            .disableThemeBackground(true)
            .disableHoverThemeBackground(true);

        mainPanel.child(filterInventory);

        guiSyncManager.bindPlayerInventory(
            guiData.getPlayer(),
            (inv, index) -> index == guiData.getSlotIndex() ? new ModularSlot(inv, index).accessibility(false, false)
                : new ModularSlot(inv, index));
        mainPanel.child(AttributeFilterScreen.playerInventory());

        return mainPanel;
    }

    @Nonnull
    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (player != null) {
            if (!player.isSneaking()/* TODO && hand == InteractionHand.MAIN_HAND */) {
                if (!worldIn.isRemote) GuiFactories.playerInventory()
                    .openFromMainHand(player);
            }
        }
        return itemStackIn;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advancedTooltip) {
        if (!AllKeys.shiftDown()) {
            List<IChatComponent> makeSummary = makeSummary(stack);
            if (makeSummary.isEmpty()) return;
            tooltip.add(" ");
            for (IChatComponent line : makeSummary) tooltip.add(line.getFormattedText());
        }
    }

    private List<IChatComponent> makeSummary(ItemStack filter) {
        List<IChatComponent> list = new ArrayList<>();
        if (!filter.hasTagCompound()) filter.stackTagCompound = new NBTTagCompound();

        if (type == FilterType.REGULAR) {
            ItemStackHandler filterItems = getFilterItems(filter);
            boolean blacklist = filter.getTagCompound()
                .getBoolean("Blacklist");

            IChatComponent line = blacklist ? Lang.translate("gui.filter.deny_list")
                : Lang.translate("gui.filter.allow_list");
            line.getChatStyle()
                .setColor(EnumChatFormatting.GOLD);
            list.add(line);
            int count = 0;
            for (int i = 0; i < filterItems.getSlots(); i++) {
                if (count > 3) {
                    line = new ChatComponentText("- ...");
                    line.getChatStyle()
                        .setColor(EnumChatFormatting.DARK_GRAY);
                    list.add(line);
                    break;
                }

                ItemStack filterStack = filterItems.getStackInSlot(i);
                if (filterStack == null) continue;
                line = new ChatComponentText("- ").appendText(filterStack.getDisplayName());
                line.getChatStyle()
                    .setColor(EnumChatFormatting.GRAY);
                list.add(line);
                count++;
            }

            if (count == 0) return Collections.emptyList();
        }

        if (type == FilterType.ATTRIBUTE) {
            WhitelistMode whitelistMode = WhitelistMode.values()[filter.getTagCompound()
                .getInteger("WhitelistMode")];
            list.add(
                (whitelistMode == WhitelistMode.WHITELIST_CONJ
                    ? Lang.translate("gui.attribute_filter.allow_list_conjunctive")
                    : whitelistMode == WhitelistMode.WHITELIST_DISJ
                        ? Lang.translate("gui.attribute_filter.allow_list_disjunctive")
                        : Lang.translate("gui.attribute_filter.deny_list"))
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)));

            int count = 0;
            NBTTagList attributes = filter.getTagCompound()
                .getTagList("MatchedAttributes", 10);
            for (int i = 0; i < attributes.tagCount(); i++) {
                NBTTagCompound compound = attributes.getCompoundTagAt(i);
                ItemAttribute attribute = ItemAttribute.fromNBT(compound);
                boolean inverted = compound.getBoolean("Inverted");
                if (count > 3) {
                    list.add(
                        new ChatComponentText("- ...")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_GRAY)));
                    break;
                }
                list.add(
                    new ChatComponentText(
                        "- " + attribute.format(inverted)
                            .getFormattedText()));
                count++;
            }

            if (count == 0) return Collections.emptyList();
        }

        return list;
    }

    public FilterType getType() {
        return this.type;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return new ChatComponentTranslation(getUnlocalizedName(stack) + ".name").getFormattedText();
    }

    public static ItemStackHandler getFilterItems(ItemStack stack) {
        ItemStackHandler newInv = new ItemStackHandler(18);
        if (AllItems.filter != stack.getItem())
            throw new IllegalArgumentException("Cannot get filter items from non-filter: " + stack);
        if (!stack.hasTagCompound()) stack.stackTagCompound = new NBTTagCompound();
        NBTTagList invNBT = stack.getTagCompound()
            .getTagList("Items", Constants.NBT.TAG_COMPOUND);
        if (invNBT != null) {
            for (int i = 0; i < invNBT.tagCount(); i++) {
                NBTTagCompound itemTag = invNBT.getCompoundTagAt(i);
                if (itemTag != null) newInv.setStackInSlot(i, ItemStack.loadItemStackFromNBT(itemTag));
            }
        }
        return newInv;
    }

    public static boolean test(World world, ItemStack stack, ItemStack filter) {
        return test(world, stack, filter, false);
    }

    public static boolean test(World world, FluidStack stack, ItemStack filter) {
        return test(world, stack, filter, true);
    }

    private static boolean test(World world, ItemStack stack, ItemStack filter, boolean matchNBT) {
        if (filter == null) return true;
        if (filter.stackTagCompound == null) filter.stackTagCompound = new NBTTagCompound();

        if (!(filter.getItem() instanceof FilterItem))
            return (matchNBT ? ItemHandlerHelper.canItemStacksStack(filter, stack)
                : ItemStack.areItemStacksEqual(filter, stack));

        if (AllItems.filter == filter.getItem()) {
            ItemStackHandler filterItems = getFilterItems(filter);
            boolean respectNBT = filter.stackTagCompound.getBoolean("RespectNBT");
            boolean blacklist = filter.stackTagCompound.getBoolean("Blacklist");
            for (int slot = 0; slot < filterItems.getSlots(); slot++) {
                ItemStack stackInSlot = filterItems.getStackInSlot(slot);
                if (stackInSlot == null) continue;
                boolean matches = test(world, stack, stackInSlot, respectNBT);
                if (matches) return !blacklist;
            }
            return blacklist;
        }

        if (AllItems.attribute_filter == filter.getItem()) {
            WhitelistMode whitelistMode = WhitelistMode.values()[filter.stackTagCompound.getInteger("WhitelistMode")];
            if (!filter.hasTagCompound()) filter.stackTagCompound = new NBTTagCompound();
            NBTTagList attributes = filter.getTagCompound()
                .getTagList("MatchedAttributes", 10);
            for (int i = 0; i < attributes.tagCount(); i++) {
                NBTTagCompound compound = attributes.getCompoundTagAt(i);
                ItemAttribute attribute = ItemAttribute.fromNBT(compound);
                if (attribute == null) continue;
                boolean matches = attribute.appliesTo(stack, world) != compound.getBoolean("Inverted");

                if (matches) {
                    switch (whitelistMode) {
                        case BLACKLIST:
                            return false;
                        case WHITELIST_CONJ:
                            continue;
                        case WHITELIST_DISJ:
                            return true;
                    }
                } else {
                    switch (whitelistMode) {
                        case BLACKLIST:
                            continue;
                        case WHITELIST_CONJ:
                            return false;
                        case WHITELIST_DISJ:
                            continue;
                    }
                }
            }

            switch (whitelistMode) {
                case BLACKLIST:
                    return true;
                case WHITELIST_CONJ:
                    return true;
                case WHITELIST_DISJ:
                    return false;
            }
        }

        return false;
    }

    private static boolean test(World world, FluidStack stack, ItemStack filter, boolean matchNBT) {
        if (filter == null) return true;
        if (stack == null) return false;

        if (!(filter.getItem() instanceof FilterItem)) {
            if (!EmptyingByBasin.canItemBeEmptied(world, filter)) return false;
            FluidStack fluidInFilter = EmptyingByBasin.emptyItem(world, filter, true)
                .getFirst();
            if (fluidInFilter == null) return false;
            if (!matchNBT) return fluidInFilter.getFluid()
                .equals(stack.getFluid());
            boolean fluidEqual = fluidInFilter.isFluidEqual(stack);
            return fluidEqual;
        }

        if (AllItems.filter == filter.getItem()) {
            ItemStackHandler filterItems = getFilterItems(filter);
            if (!filter.hasTagCompound()) filter.stackTagCompound = new NBTTagCompound();
            boolean respectNBT = filter.getTagCompound()
                .getBoolean("RespectNBT");
            boolean blacklist = filter.getTagCompound()
                .getBoolean("Blacklist");
            for (int slot = 0; slot < filterItems.getSlots(); slot++) {
                ItemStack stackInSlot = filterItems.getStackInSlot(slot);
                if (stackInSlot == null) continue;
                boolean matches = test(world, stack, stackInSlot, respectNBT);
                if (matches) return !blacklist;
            }
            return blacklist;
        }
        return false;
    }

}
