package su.sergiusonesimus.recreate.content.logistics.item.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.utils.ItemStackItemHandler;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.PhantomItemSlotSH;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;

import su.sergiusonesimus.recreate.AllItems;
import su.sergiusonesimus.recreate.foundation.gui.AllGuiTextures;
import su.sergiusonesimus.recreate.foundation.gui.AllIcons;
import su.sergiusonesimus.recreate.foundation.gui.widget.IconButton;
import su.sergiusonesimus.recreate.foundation.gui.widget.Indicator;
import su.sergiusonesimus.recreate.foundation.gui.widget.Indicator.State;

public class FilterScreen extends AbstractFilterScreen {

    private static final String PREFIX = "gui.filter.";

    public static SlotGroupWidget filterInventory(PlayerInventoryGuiData guiData, PanelSyncManager guiSyncManager) {
        ItemStackItemHandler itemHandler = new ItemStackItemHandler(guiData, 18) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        guiSyncManager.registerSlotGroup("filter_items", 9);

        SlotGroupWidget slotGroupWidget = abstractFilterInventory(
            AllGuiTextures.FILTER,
            new ItemStack(AllItems.filter),
            AllItems.filter.getUnlocalizedName() + ".name",
            (mouseButton) -> {
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    itemHandler.setStackInSlot(i, null);
                    ((PhantomItemSlotSH) guiSyncManager.findSyncHandler("auto_sync:filter_gui", i))
                        .updateFromClient(null, 0);
                }
                return true;
            },
            guiSyncManager,
            -30);

        for (int i = 0; i < 18; i++) {
            slotGroupWidget.child(
                new PhantomItemSlot().slot(
                    SyncHandlers.itemSlot(itemHandler, i)
                        .slotGroup("filter_items"))
                    .pos(22 + i % 9 * 18, 19 + i / 9 * 18)
                    .name("slot_" + i));
        }

        ItemStack stack = guiData.getUsedItemStack();
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

        BooleanSyncValue respectNbtSynced = new BooleanSyncValue(() -> {
            NBTTagCompound tag = guiData.getUsedItemStack()
                .getTagCompound();
            if (!tag.hasKey("RespectNBT")) tag.setBoolean("RespectNBT", false);
            return tag.getBoolean("RespectNBT");
        }, (value) -> {
            ItemStack itemStack = guiData.getUsedItemStack();
            itemStack.getTagCompound()
                .setBoolean("RespectNBT", value);
            guiData.setUsedItemStack(itemStack);
        }).allowC2S();
        BooleanSyncValue blacklistSynced = new BooleanSyncValue(() -> {
            NBTTagCompound tag = guiData.getUsedItemStack()
                .getTagCompound();
            if (!tag.hasKey("Blacklist")) tag.setBoolean("Blacklist", false);
            return tag.getBoolean("Blacklist");
        }, (value) -> {
            ItemStack itemStack = guiData.getUsedItemStack();
            itemStack.getTagCompound()
                .setBoolean("Blacklist", value);
            guiData.setUsedItemStack(itemStack);
        }).allowC2S();

        guiSyncManager.syncValue("RespectNBT", 0, respectNbtSynced);
        guiSyncManager.syncValue("Blacklist", 1, blacklistSynced);

        boolean respectNbt = respectNbtSynced.getBoolValue();
        boolean blacklist = blacklistSynced.getBoolValue();

        IconButton blacklistButton = new IconButton(AllIcons.I_BLACKLIST).setPressed(blacklist)
            .pos(18, 73)
            .tooltipDynamic(tooltip -> generateTooltip(tooltip, PREFIX, "deny_list", guiData));
        Indicator blacklistIndicator = new Indicator().setState(blacklist ? State.ON : State.OFF)
            .pos(18, 67);
        IconButton whitelistButton = new IconButton(AllIcons.I_WHITELIST).setPressed(!blacklist)
            .pos(36, 73)
            .tooltipDynamic(tooltip -> generateTooltip(tooltip, PREFIX, "allow_list", guiData));
        Indicator whitelistIndicator = new Indicator().setState(blacklist ? State.OFF : State.ON)
            .pos(36, 67);

        IconButton respectNbtButton = new IconButton(AllIcons.I_RESPECT_NBT).setPressed(respectNbt)
            .pos(60, 73)
            .tooltipDynamic(tooltip -> generateTooltip(tooltip, PREFIX, "respect_data", guiData));
        Indicator respectNbtIndicator = new Indicator().setState(respectNbt ? State.ON : State.OFF)
            .pos(60, 67);
        IconButton ignoreNbtButton = new IconButton(AllIcons.I_IGNORE_NBT).setPressed(!respectNbt)
            .pos(78, 73)
            .tooltipDynamic(tooltip -> generateTooltip(tooltip, PREFIX, "ignore_data", guiData));
        Indicator ignoreNbtIndicator = new Indicator().setState(respectNbt ? State.OFF : State.ON)
            .pos(78, 67);

        blacklistButton.onMouseTapped((button) -> {
            onFilterButtonPressed(
                blacklistButton,
                whitelistButton,
                blacklistIndicator,
                whitelistIndicator,
                blacklistSynced,
                true);
            return true;
        });
        whitelistButton.onMouseTapped((button) -> {
            onFilterButtonPressed(
                whitelistButton,
                blacklistButton,
                whitelistIndicator,
                blacklistIndicator,
                blacklistSynced,
                false);
            return true;
        });

        respectNbtButton.onMouseTapped((button) -> {
            onFilterButtonPressed(
                respectNbtButton,
                ignoreNbtButton,
                respectNbtIndicator,
                ignoreNbtIndicator,
                respectNbtSynced,
                true);
            return true;
        });
        ignoreNbtButton.onMouseTapped((button) -> {
            onFilterButtonPressed(
                ignoreNbtButton,
                respectNbtButton,
                ignoreNbtIndicator,
                respectNbtIndicator,
                respectNbtSynced,
                false);
            return true;
        });

        slotGroupWidget.child(blacklistButton)
            .child(blacklistIndicator)
            .child(whitelistButton)
            .child(whitelistIndicator)
            .child(respectNbtButton)
            .child(respectNbtIndicator)
            .child(ignoreNbtButton)
            .child(ignoreNbtIndicator);

        return slotGroupWidget;
    }

    private static void onFilterButtonPressed(IconButton currentButton, IconButton oppositeButton,
        Indicator currentIndicator, Indicator oppositeIndicator, BooleanSyncValue syncVar, boolean valueToSet) {
        currentButton.setPressed(true);
        oppositeButton.setPressed(false);
        currentIndicator.setState(State.ON);
        oppositeIndicator.setState(State.OFF);
        syncVar.setBoolValue(valueToSet);
    }

}
