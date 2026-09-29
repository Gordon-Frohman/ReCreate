package su.sergiusonesimus.recreate.foundation.gui.widget;

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.cleanroommc.modularui.api.UpOrDown;

import su.sergiusonesimus.recreate.AllKeys;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.scrollvalue.ScrollValueBehaviour.StepContext;
import su.sergiusonesimus.recreate.foundation.utility.Lang;

public class ScrollInput extends Label {

    protected Consumer<Integer> onScroll;
    protected int state;
    protected IChatComponent title = Lang.translate("gui.scrollInput.defaultTitle");
    protected final IChatComponent scrollToModify = Lang.translate("gui.scrollInput.scrollToModify");
    protected final IChatComponent shiftScrollsFaster = Lang.translate("gui.scrollInput.shiftScrollsFaster");

    protected int min, max;
    protected int shiftStep;
    Function<StepContext, Integer> step;

    public ScrollInput() {
        state = 0;
        min = 0;
        max = 1;
        shiftStep = 5;
        step = standardStep();
    }

    public Function<StepContext, Integer> standardStep() {
        return c -> c.shift ? shiftStep : 1;
    }

    public ScrollInput withRange(int min, int max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public ScrollInput calling(Consumer<Integer> onScroll) {
        this.onScroll = onScroll;
        return this;
    }

    public ScrollInput removeCallback() {
        this.onScroll = null;
        return this;
    }

    public ScrollInput titled(String title) {
        return titled(new ChatComponentText(title));
    }

    public ScrollInput titled(IChatComponent title) {
        this.title = title;
        updateTooltip();
        return this;
    }

    public ScrollInput withStepFunction(Function<StepContext, Integer> step) {
        this.step = step;
        return this;
    }

    public int getState() {
        return state;
    }

    public ScrollInput setState(int state) {
        this.state = state;
        clampState();
        updateTooltip();
        writeToLabel();
        return this;
    }

    public ScrollInput withShiftStep(int step) {
        shiftStep = step;
        return this;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        StepContext context = new StepContext();
        context.control = AllKeys.ctrlDown();
        context.shift = AllKeys.shiftDown();
        context.currentValue = state;
        context.forward = scrollDirection.isUp();

        int priorState = state;
        boolean shifted = AllKeys.shiftDown();
        int step = (int) scrollDirection.modifier * this.step.apply(context);

        state += step;
        if (shifted) state -= state % shiftStep;

        clampState();

        if (priorState != state) onChanged();

        return priorState != state;
    }

    protected void clampState() {
        if (state >= max) state = max - 1;
        if (state < min) state = min;
    }

    public void onChanged() {
        writeToLabel();
        if (onScroll != null) onScroll.accept(state);
        updateTooltip();
    }

    protected void writeToLabel() {
        getRichText().clearText()
            .addLine(String.valueOf(state));
    }

    protected void updateTooltip() {
        tooltip().getRichText()
            .clearText()
            .addLine(
                title.createCopy()
                    .setChatStyle(
                        title.getChatStyle()
                            .createDeepCopy()
                            .setColor(EnumChatFormatting.BLUE))
                    .getFormattedText())
            .addLine(
                scrollToModify.createCopy()
                    .setChatStyle(
                        scrollToModify.getChatStyle()
                            .createDeepCopy()
                            .setItalic(true)
                            .setColor(EnumChatFormatting.DARK_GRAY))
                    .getFormattedText())
            .addLine(
                shiftScrollsFaster.createCopy()
                    .setChatStyle(
                        shiftScrollsFaster.getChatStyle()
                            .createDeepCopy()
                            .setItalic(true)
                            .setColor(EnumChatFormatting.DARK_GRAY))
                    .getFormattedText());
    }

}
