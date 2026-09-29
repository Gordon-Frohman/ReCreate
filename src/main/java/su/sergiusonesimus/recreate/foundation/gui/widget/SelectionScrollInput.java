package su.sergiusonesimus.recreate.foundation.gui.widget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.screen.RichTooltip;

import su.sergiusonesimus.recreate.foundation.utility.Lang;

public class SelectionScrollInput extends ScrollInput {

    private final IChatComponent scrollToSelect = Lang.translate("gui.scrollInput.scrollToSelect");
    protected List<IChatComponent> options;

    public SelectionScrollInput() {
        options = new ArrayList<>();
    }

    public ScrollInput forOptions(List<IChatComponent> options) {
        this.options = options;
        this.max = options.size();
        updateTooltip();
        return this;
    }

    @Override
    protected void writeToLabel() {
        getRichText().clearText()
            .addLine(
                options.get(state)
                    .getFormattedText());
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        return super.onMouseScroll(scrollDirection.isDown() ? UpOrDown.UP : UpOrDown.DOWN, amount);
    }

    @Override
    protected void updateTooltip() {
        RichTooltip tooltip = tooltip();
        tooltip.clearText()
            .addLine(
                title.createCopy()
                    .setChatStyle(
                        title.getChatStyle()
                            .setColor(EnumChatFormatting.BLUE))
                    .getFormattedText());
        int min = Math.min(this.max - 16, state - 7);
        int max = Math.max(this.min + 16, state + 8);
        min = Math.max(min, this.min);
        max = Math.min(max, this.max);
        if (this.min + 1 == min) min--;
        if (min > this.min) tooltip.addLine(EnumChatFormatting.GRAY.toString() + "> ...");
        if (this.max - 1 == max) max++;
        for (int i = min; i < max; i++) {
            if (i >= options.size()) continue;
            String line = options.get(i)
                .getUnformattedText();
            if (i == state) tooltip.addLine(EnumChatFormatting.WHITE.toString() + "-> " + line);
            else tooltip.addLine(EnumChatFormatting.GRAY.toString() + "> " + line);
        }
        if (max < this.max) tooltip.addLine(EnumChatFormatting.GRAY.toString() + "> ...");

        tooltip.addLine(
            scrollToSelect.createCopy()
                .setChatStyle(
                    new ChatStyle().setItalic(true)
                        .setColor(EnumChatFormatting.DARK_GRAY))
                .getFormattedText());
    }

}
