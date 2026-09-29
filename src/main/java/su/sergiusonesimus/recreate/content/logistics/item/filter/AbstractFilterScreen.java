package su.sergiusonesimus.recreate.content.logistics.item.filter;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.widget.IGuiAction.MousePressed;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

import su.sergiusonesimus.recreate.foundation.gui.AllGuiTextures;
import su.sergiusonesimus.recreate.foundation.gui.AllIcons;
import su.sergiusonesimus.recreate.foundation.gui.container.AbstractSimiContainerScreen;
import su.sergiusonesimus.recreate.foundation.gui.widget.IconButton;

public abstract class AbstractFilterScreen extends AbstractSimiContainerScreen {

    protected static SlotGroupWidget abstractFilterInventory(AllGuiTextures background, ItemStack previewItem,
        String title, MousePressed clearButtonClick, PanelSyncManager guiSyncManager, int offset) {
        SlotGroupWidget slotGroupWidget = previewSlotGroupWidget(background, previewItem, title).left(offset);
        slotGroupWidget.name("filter_inventory");

        IconButton confirmButton = new IconButton(AllIcons.I_CONFIRM).right(15)
            .bottom(6);
        confirmButton.onMouseTapped(mouseButton -> {
            if (mouseButton == 0 || mouseButton == 1) {
                confirmButton.getPanel()
                    .closeIfOpen();
                return true;
            }
            return false;
        });
        slotGroupWidget.child(confirmButton)
            .child(
                new IconButton(AllIcons.I_TRASH).right(44)
                    .bottom(6)
                    .onMouseTapped(clearButtonClick));

        return slotGroupWidget;
    }

}
