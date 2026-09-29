package su.sergiusonesimus.recreate.foundation.gui.container;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiScreenAccessor;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget.SlotConsumer;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.recreate.foundation.gui.AllGuiTextures;
import su.sergiusonesimus.recreate.foundation.item.ItemDescription.Palette;
import su.sergiusonesimus.recreate.foundation.item.TooltipHelper;
import su.sergiusonesimus.recreate.foundation.utility.Lang;

public abstract class AbstractSimiContainerScreen {

    protected static SlotGroupWidget previewSlotGroupWidget(AllGuiTextures background, ItemStack previewItem,
        String title) {
        SlotGroupWidget slotGroupWidget = new SlotGroupWidget().disableThemeBackground(true)
            .disableHoverThemeBackground(true)
            .background(background.asUITexture())
            .size(background.width, background.height);
        ItemDrawable preview = new ItemDrawable(previewItem) {

            @SideOnly(Side.CLIENT)
            @Override
            public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
                int padding = 8;
                if (getItem() == null) return;
                GlStateManager.pushMatrix();
                Platform.setupDrawItem();
                GlStateManager.translate(x - padding, y - padding, 0);
                GlStateManager
                    .scale((width + padding * 2) / 16f, (height + padding * 2) / 16f, (height + padding * 2) / 16f);
                RenderItem renderItem = GuiScreenAccessor.getItemRender();
                renderItem.zLevel = context.getCurrentDrawingZ() + 100;
                renderItem.renderItemAndEffectIntoGUI(
                    Minecraft.getMinecraft().fontRenderer,
                    Minecraft.getMinecraft()
                        .getTextureManager(),
                    getItem(),
                    0,
                    0);
                GuiDraw.afterRenderItemAndEffectIntoGUI(getItem());
                GlStateManager.disableRescaleNormal();
                renderItem.zLevel = 0;
                Platform.endDrawItem();
                GlStateManager.popMatrix();
            }

        };
        return slotGroupWidget.child(
            preview.asWidget()
                .size(64)
                .left(background.width + 8)
                .bottom(-17)
                .excludeAreaInRecipeViewer())
            .child(
                IKey.lang(title)
                    .asWidget()
                    .color(Color.WHITE.main)
                    .shadow(true)
                    .textAlign(Alignment.TopCenter)
                    .top(3)
                    .width(background.width - 8))
            .excludeAreaInRecipeViewer();
    }

    public static SlotGroupWidget playerInventory() {
        return playerInventory((index, slot) -> slot);
    }

    public static SlotGroupWidget playerInventory(SlotConsumer slotConsumer) {
        SlotGroupWidget slotGroupWidget = new SlotGroupWidget().disableHoverThemeBackground(true)
            .disableThemeBackground(true)
            .size(AllGuiTextures.PLAYER_INVENTORY.width, AllGuiTextures.PLAYER_INVENTORY.height)
            .background(AllGuiTextures.PLAYER_INVENTORY.asUITexture())
            .bottom(0)
            .child(
                IKey.lang("container.inventory")
                    .asWidget()
                    .pos(8, 5));
        slotGroupWidget.name("player_inventory");
        String key = "player";
        for (int i = 0; i < 9; i++) {
            slotGroupWidget.child(
                slotConsumer.apply(i, new ItemSlot())
                    .syncHandler(key, i)
                    .pos(7 + i * 18, 17 + 3 * 18 + 4)
                    .name("slot_" + i));
        }
        for (int i = 0; i < 27; i++) {
            slotGroupWidget.child(
                slotConsumer.apply(i + 9, new ItemSlot())
                    .syncHandler(key, i + 9)
                    .pos(7 + i % 9 * 18, 17 + i / 9 * 18)
                    .name("slot_" + (i + 9)));
        }

        return slotGroupWidget.excludeAreaInRecipeViewer();
    }

    protected static void generateTooltip(RichTooltip tooltip, String prefix, String buttonName,
        PlayerInventoryGuiData guiData) {
        generateTooltip(tooltip, prefix, buttonName, buttonName + ".description", guiData);
    }

    protected static void generateTooltip(RichTooltip tooltip, String prefix, String title, String description,
        PlayerInventoryGuiData guiData) {
        tooltip.addLine(
            Lang.translate(prefix + title)
                .getFormattedText());
        if (description != null) {
            tooltip.setAutoUpdate(true);
            tooltip.addLine(
                TooltipHelper.holdShift(Palette.Yellow, true)
                    .getFormattedText());
            if (guiData.isClient() && Interactable.hasShiftDown()) {
                tooltip.emptyLine();
                for (IChatComponent component : TooltipHelper.cutTextComponent(
                    Lang.translate(prefix + description),
                    EnumChatFormatting.GRAY,
                    EnumChatFormatting.GRAY)) {
                    tooltip.addLine(component.getFormattedText());
                }
            }
        }
    }

}
