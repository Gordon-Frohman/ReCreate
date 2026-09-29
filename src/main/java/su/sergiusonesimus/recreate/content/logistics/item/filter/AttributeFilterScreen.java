package su.sergiusonesimus.recreate.content.logistics.item.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.text.LangKey;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import com.cleanroommc.modularui.utils.serialization.IEquals;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.GenericListSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;

import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.recreate.AllItems;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.foundation.gui.AllGuiTextures;
import su.sergiusonesimus.recreate.foundation.gui.AllIcons;
import su.sergiusonesimus.recreate.foundation.gui.widget.IconButton;
import su.sergiusonesimus.recreate.foundation.gui.widget.Indicator;
import su.sergiusonesimus.recreate.foundation.gui.widget.Indicator.State;
import su.sergiusonesimus.recreate.foundation.gui.widget.SelectionScrollInput;
import su.sergiusonesimus.recreate.foundation.utility.Pair;

public class AttributeFilterScreen extends AbstractFilterScreen {

    public enum WhitelistMode {
        WHITELIST_DISJ,
        WHITELIST_CONJ,
        BLACKLIST;
    }

    public static class WhitelistModeSyncValue extends EnumSyncValue<WhitelistMode, WhitelistModeSyncValue> {

        public WhitelistModeSyncValue(@NotNull Supplier<WhitelistMode> getter,
            @Nullable Consumer<WhitelistMode> setter) {
            super(WhitelistMode.class, getter, setter);
        }

    }

    private static final String PREFIX = "gui.attribute_filter.";

    public static SlotGroupWidget attributeFilterInventory(PlayerInventoryGuiData guiData,
        PanelSyncManager guiSyncManager) {
        ItemStack stack = guiData.getUsedItemStack();
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

        String whitelistModeKey = "WhitelistMode";
        String matchedAttributesKey = "MatchedAttributes";
        String invertedKey = "Inverted";

        WhitelistModeSyncValue whitelistMode = new WhitelistModeSyncValue(() -> {
            NBTTagCompound tag = guiData.getUsedItemStack()
                .getTagCompound();
            if (!tag.hasKey(whitelistModeKey)) tag.setInteger(whitelistModeKey, 0);
            return WhitelistMode.values()[tag.getInteger(whitelistModeKey)];
        }, (value) -> {
            ItemStack itemStack = guiData.getUsedItemStack();
            itemStack.getTagCompound()
                .setInteger(whitelistModeKey, value.ordinal());
            guiData.setUsedItemStack(itemStack);
        }).allowC2S();
        GenericListSyncHandler<Pair<ItemAttribute, Boolean>> selectedAttributesSynced = new GenericListSyncHandler<Pair<ItemAttribute, Boolean>>(
            () -> {
                List<Pair<ItemAttribute, Boolean>> result = new ArrayList<Pair<ItemAttribute, Boolean>>();
                NBTTagCompound tag = guiData.getUsedItemStack()
                    .getTagCompound();
                if (!tag.hasKey(matchedAttributesKey)) tag.setTag(matchedAttributesKey, new NBTTagList());
                NBTTagList attributes = tag.getTagList(matchedAttributesKey, 10);
                NBTTagCompound compound;
                for (int i = 0; i < attributes.tagCount(); i++) {
                    compound = attributes.getCompoundTagAt(i);
                    result.add(Pair.of(ItemAttribute.fromNBT(compound), compound.getBoolean(invertedKey)));
                }
                return result;
            },
            (value) -> {
                ItemStack itemStack = guiData.getUsedItemStack();
                NBTTagList attributes = new NBTTagList();
                NBTTagCompound compound;
                NBTTagCompound itemAttribute;
                ItemAttribute ia;
                for (Pair<ItemAttribute, Boolean> attribute : value) {
                    compound = new NBTTagCompound();
                    itemAttribute = new NBTTagCompound();
                    ia = attribute.getFirst();
                    ia.writeNBT(itemAttribute);
                    compound.setTag(ia.getNBTKey(), itemAttribute);
                    compound.setBoolean(invertedKey, attribute.getSecond());
                    attributes.appendTag(compound);
                }
                itemStack.getTagCompound()
                    .setTag(matchedAttributesKey, attributes);
                guiData.setUsedItemStack(itemStack);
            },
            new IByteBufAdapter<Pair<ItemAttribute, Boolean>>() {

                @Override
                public Pair<ItemAttribute, Boolean> deserialize(PacketBuffer buffer) throws IOException {
                    NBTTagCompound compound = buffer.readNBTTagCompoundFromBuffer();
                    return Pair.of(ItemAttribute.fromNBT(compound), compound.getBoolean(invertedKey));
                }

                @Override
                public void serialize(PacketBuffer buffer, Pair<ItemAttribute, Boolean> value) throws IOException {
                    NBTTagCompound compound = new NBTTagCompound();
                    value.getFirst()
                        .serializeNBT(compound);
                    compound.setBoolean(invertedKey, value.getSecond());
                    buffer.writeNBTTagCompoundToBuffer(compound);
                }

                @Override
                public boolean areEqual(@NotNull Pair<ItemAttribute, Boolean> t1,
                    @NotNull Pair<ItemAttribute, Boolean> t2) {
                    return t1.equals(t2);
                }

            },
            new IByteBufSerializer<Pair<ItemAttribute, Boolean>>() {

                @Override
                public void serialize(PacketBuffer buffer, Pair<ItemAttribute, Boolean> value) throws IOException {
                    NBTTagCompound compound = new NBTTagCompound();
                    value.getFirst()
                        .serializeNBT(compound);
                    compound.setBoolean(invertedKey, value.getSecond());
                    buffer.writeNBTTagCompoundToBuffer(compound);
                }

            },
            new IEquals<Pair<ItemAttribute, Boolean>>() {

                @Override
                public boolean areEqual(@NotNull Pair<ItemAttribute, Boolean> a,
                    @NotNull Pair<ItemAttribute, Boolean> b) {
                    NBTTagCompound nbt1 = new NBTTagCompound();
                    NBTTagCompound nbt2 = new NBTTagCompound();
                    a.getFirst()
                        .serializeNBT(nbt1);
                    b.getFirst()
                        .serializeNBT(nbt2);
                    return nbt1.equals(nbt2) && a.getSecond() == b.getSecond();
                }

            },
            null).allowC2S();

        LangKey referenceH = (LangKey) IKey.lang(ReCreate.ID + "." + PREFIX + "add_reference_item")
            .style(EnumChatFormatting.ITALIC);
        LangKey selectedT = (LangKey) IKey.lang(ReCreate.ID + "." + PREFIX + "selected_attributes")
            .style(EnumChatFormatting.YELLOW);
        LangKey noSelectedT = (LangKey) IKey.lang(ReCreate.ID + "." + PREFIX + "no_selected_attributes")
            .style(EnumChatFormatting.YELLOW);

        guiSyncManager.syncValue(whitelistModeKey, 0, whitelistMode);
        guiSyncManager.syncValue(matchedAttributesKey, 1, selectedAttributesSynced);

        WhitelistMode whitelist = whitelistMode.getValue();
        List<IKey> selectedAttributes = new ArrayList<IKey>();
        selectedAttributes.add(
            selectedAttributesSynced.getValue()
                .size() > 0 ? selectedT : noSelectedT);
        for (Pair<ItemAttribute, Boolean> pair : selectedAttributesSynced.getValue()) selectedAttributes.add(
            IKey.str(
                "- " + pair.getFirst()
                    .format(pair.getSecond())
                    .getUnformattedText())
                .style(EnumChatFormatting.GRAY));
        ItemStack[] lastItemScanned = new ItemStack[1];
        List<ItemAttribute> attributesOfItem = new ArrayList<ItemAttribute>();

        IconButton addButton = new IconButton(AllIcons.I_ADD).setPressed(true)
            .pos(182, 21)
            .tooltipDynamic(tooltip -> generateTooltip(tooltip, PREFIX, "add_attribute", null, guiData));
        IconButton addInvertedButton = new IconButton(AllIcons.I_ADD_INVERTED_ATTRIBUTE).setPressed(true)
            .pos(200, 21)
            .tooltipDynamic(tooltip -> generateTooltip(tooltip, PREFIX, "add_inverted_attribute", null, guiData));

        SlotGroupWidget slotGroupWidget = abstractFilterInventory(
            AllGuiTextures.ATTRIBUTE_FILTER,
            new ItemStack(AllItems.attribute_filter),
            AllItems.attribute_filter.getUnlocalizedName() + ".name",
            (mouseButton) -> {
                selectedAttributesSynced.modifyValue(attributes -> attributes.clear());
                selectedAttributesSynced.notifyUpdate();
                selectedAttributes.clear();
                selectedAttributes.add(noSelectedT);
                if (lastItemScanned[0] != null) {
                    addButton.setPressed(false);
                    addInvertedButton.setPressed(false);
                }
                return true;
            },
            guiSyncManager,
            -43);

        SelectionScrollInput label = (SelectionScrollInput) new SelectionScrollInput().textColor(0xF3EBDE)
            .textShadow(true)
            .alignment(Alignment.CenterLeft)
            .pos(39, 21)
            .size(137, 18)
            .padding(4);
        label.getRichText()
            .add(referenceH);

        IconButton whitelistDisButton = new IconButton(AllIcons.I_WHITELIST_OR)
            .setPressed(whitelist == WhitelistMode.WHITELIST_DISJ)
            .pos(47, 59)
            .tooltipDynamic(tooltip -> generateTooltip(tooltip, PREFIX, "allow_list_disjunctive", guiData));
        Indicator whitelistDisIndicator = new Indicator()
            .setState(whitelist == WhitelistMode.WHITELIST_DISJ ? State.ON : State.OFF)
            .pos(47, 53);
        IconButton whitelistConButton = new IconButton(AllIcons.I_WHITELIST_AND)
            .setPressed(whitelist == WhitelistMode.WHITELIST_CONJ)
            .pos(65, 59)
            .tooltipDynamic(tooltip -> generateTooltip(tooltip, PREFIX, "allow_list_conjunctive", guiData));
        Indicator whitelistConIndicator = new Indicator()
            .setState(whitelist == WhitelistMode.WHITELIST_CONJ ? State.ON : State.OFF)
            .pos(65, 53);
        IconButton blacklistButton = new IconButton(AllIcons.I_WHITELIST_NOT)
            .setPressed(whitelist == WhitelistMode.BLACKLIST)
            .pos(83, 59)
            .tooltipDynamic(tooltip -> generateTooltip(tooltip, PREFIX, "deny_list", guiData));
        Indicator blacklistIndicator = new Indicator()
            .setState(whitelist == WhitelistMode.BLACKLIST ? State.ON : State.OFF)
            .pos(83, 53);

        whitelistDisButton.onMouseTapped((button) -> {
            onAttributeFilterButtonPressed(
                whitelistDisButton,
                new IconButton[] { whitelistConButton, blacklistButton },
                whitelistDisIndicator,
                new Indicator[] { whitelistConIndicator, blacklistIndicator },
                whitelistMode,
                WhitelistMode.WHITELIST_DISJ);
            return true;
        });
        whitelistConButton.onMouseTapped((button) -> {
            onAttributeFilterButtonPressed(
                whitelistConButton,
                new IconButton[] { whitelistDisButton, blacklistButton },
                whitelistConIndicator,
                new Indicator[] { whitelistDisIndicator, blacklistIndicator },
                whitelistMode,
                WhitelistMode.WHITELIST_CONJ);
            return true;
        });
        blacklistButton.onMouseTapped((button) -> {
            onAttributeFilterButtonPressed(
                blacklistButton,
                new IconButton[] { whitelistConButton, whitelistDisButton },
                blacklistIndicator,
                new Indicator[] { whitelistConIndicator, whitelistDisIndicator },
                whitelistMode,
                WhitelistMode.BLACKLIST);
            return true;
        });

        addButton.onMouseTapped((button) -> {
            return handleAddedAttibute(
                selectedAttributesSynced,
                label,
                attributesOfItem,
                selectedAttributes,
                addButton,
                addInvertedButton,
                selectedT,
                false);
        });
        addInvertedButton.onMouseTapped((button) -> {
            return handleAddedAttibute(
                selectedAttributesSynced,
                label,
                attributesOfItem,
                selectedAttributes,
                addButton,
                addInvertedButton,
                selectedT,
                true);
        });

        slotGroupWidget.child(label)
            .child(whitelistDisButton)
            .child(whitelistDisIndicator)
            .child(whitelistConButton)
            .child(whitelistConIndicator)
            .child(blacklistButton)
            .child(blacklistIndicator)
            .child(addButton)
            .child(addInvertedButton);

        ItemStackHandler itemHandler = new ItemStackHandler(2) {

            @Override
            public ItemStack getStackInSlot(int slot) {
                if (slot == 1) {
                    return new ItemStack(
                        Items.name_tag,
                        selectedAttributesSynced.getValue()
                            .size());
                } else {
                    return super.getStackInSlot(slot);
                }
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                if (slot == 0) {
                    super.setStackInSlot(slot, stack);
                    lastItemScanned[0] = stack;

                    if (stack == null) {
                        label.clearText()
                            .add(referenceH);
                        label.forOptions(new ArrayList<IChatComponent>())
                            .tooltip()
                            .clearText();
                        addButton.setPressed(true);
                        addInvertedButton.setPressed(true);
                        label.calling(s -> {});
                        return;
                    }

                    addButton.setPressed(false);

                    addInvertedButton.setPressed(false);
                    label.titled(stack.getDisplayName());
                    attributesOfItem.clear();
                    for (ItemAttribute itemAttribute : ItemAttribute.types) attributesOfItem
                        .addAll(itemAttribute.listAttributesOf(stack, MetaworldsMod.proxy.getMainWorld()));
                    List<IChatComponent> options = attributesOfItem.stream()
                        .map(a -> a.format(false))
                        .collect(Collectors.toList());
                    label.forOptions(options);
                    label.setState(0);
                    label.calling(i -> {
                        label.setTextAndTrim(options.get(i), true, 112);
                        ItemAttribute selected = attributesOfItem.get(i);
                        for (Pair<ItemAttribute, Boolean> existing : selectedAttributesSynced.getValue()) {
                            NBTTagCompound testTag = new NBTTagCompound();
                            NBTTagCompound testTag2 = new NBTTagCompound();
                            existing.getFirst()
                                .serializeNBT(testTag);
                            selected.serializeNBT(testTag2);
                            if (testTag.equals(testTag2)) {
                                addButton.setPressed(true);
                                addInvertedButton.setPressed(true);
                                return;
                            }
                        }
                        addButton.setPressed(false);
                        addInvertedButton.setPressed(false);
                    });
                    label.onChanged();
                }
            }

        };

        slotGroupWidget.child(
            new PhantomItemSlot().slot(SyncHandlers.itemSlot(itemHandler, 0))
                .pos(15, 21)
                .name("item_to_add"));
        PhantomItemSlot attributesSlot = (PhantomItemSlot) new PhantomItemSlot() {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
                super.draw(context, widgetTheme);
                int amount = selectedAttributesSynced.getValue()
                    .size();
                if (amount < 2) {
                    Area area = this.getArea();
                    GuiDraw.drawAmountText(amount, null, 0, 0, area.width, area.height, Alignment.BottomRight);
                }
            }

        }.slot(SyncHandlers.itemSlot(itemHandler, 1))
            .pos(21, 56)
            .name("current_attributes")
            .disableThemeBackground(true)
            .disableHoverThemeBackground(true);
        attributesSlot.itemTooltip()
            .tooltipBuilder(tooltip -> {
                tooltip.clearText();
                for (IKey attribute : selectedAttributes) {
                    tooltip.addLine(attribute);
                }
            });
        slotGroupWidget.child(attributesSlot);

        return slotGroupWidget;
    }

    private static void onAttributeFilterButtonPressed(IconButton currentButton, IconButton[] oppositeButtons,
        Indicator currentIndicator, Indicator[] oppositeIndicators, WhitelistModeSyncValue syncVar,
        WhitelistMode valueToSet) {
        currentButton.setPressed(true);
        for (IconButton button : oppositeButtons) button.setPressed(false);
        currentIndicator.setState(State.ON);
        for (Indicator indicator : oppositeIndicators) indicator.setState(State.OFF);
        syncVar.setValue(valueToSet);
    }

    private static boolean handleAddedAttibute(
        GenericListSyncHandler<Pair<ItemAttribute, Boolean>> selectedAttributesSynced,
        SelectionScrollInput attributeSelector, List<ItemAttribute> attributesOfItem, List<IKey> selectedAttributes,
        IconButton add, IconButton addInverted, IKey selectedT, boolean inverted) {
        int index = attributeSelector.getState();
        if (index >= attributesOfItem.size()) return false;
        add.setPressed(true);
        addInverted.setPressed(true);
        NBTTagCompound tag = new NBTTagCompound();
        ItemAttribute itemAttribute = attributesOfItem.get(index);
        itemAttribute.serializeNBT(tag);
        selectedAttributesSynced.modifyValue(attributesList -> attributesList.add(Pair.of(itemAttribute, inverted)));
        if (selectedAttributesSynced.getValue()
            .size() == 1) {
            selectedAttributes.clear();
            selectedAttributes.add(selectedT);
        }
        selectedAttributes.add(
            IKey.str(
                "- " + itemAttribute.format(inverted)
                    .getUnformattedText())
                .style(EnumChatFormatting.GRAY));
        return true;
    }

}
