package su.sergiusonesimus.recreate.foundation.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.widgets.RichTextWidget;

public class Label extends RichTextWidget {

    public Label setTextAndTrim(IChatComponent newText, boolean trimFront, int maxWidthPx) {
        IRichTextBuilder<?> text = getRichText().clearText();

        if (getStringWidthUnicode(newText.getUnformattedText()) <= maxWidthPx) {
            text.addLine(newText.getFormattedText());
            return this;
        }

        String trim = "...";
        int trimWidth = getStringWidthUnicode(trim);

        String raw = newText.getUnformattedText();
        StringBuilder builder = new StringBuilder(raw);
        int startIndex = trimFront ? 0 : raw.length() - 1;
        int endIndex = !trimFront ? 0 : raw.length() - 1;
        int step = (int) Math.signum(endIndex - startIndex);

        for (int i = startIndex; i != endIndex; i += step) {
            String sub = builder.substring(trimFront ? i : startIndex, trimFront ? endIndex + 1 : i + 1);
            if (sub.length() + trimWidth <= maxWidthPx) {
                text.addLine(
                    new ChatComponentText(trimFront ? trim + sub : sub + trim).setChatStyle(newText.getChatStyle())
                        .getFormattedText());
                return this;
            }
        }

        return this;
    }

    private int getStringWidthUnicode(String text) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int width = fontRenderer.getStringWidth(text);
        return fontRenderer.getUnicodeFlag() ? width / 2 : width;
    }

}
