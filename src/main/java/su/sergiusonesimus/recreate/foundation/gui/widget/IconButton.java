package su.sergiusonesimus.recreate.foundation.gui.widget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Box;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import su.sergiusonesimus.recreate.foundation.gui.AllGuiTextures;
import su.sergiusonesimus.recreate.foundation.gui.AllIcons;

public class IconButton extends ButtonWidget<IconButton> {

    private boolean isPressed = false;

    private static final UITexture buttonBackGround = AllGuiTextures.BUTTON.asUITexture();
    private static final UITexture hoverBackGround = AllGuiTextures.BUTTON_HOVER.asUITexture();
    private static final UITexture pressedBackGround = AllGuiTextures.BUTTON_DOWN.asUITexture();

    public IconButton(AllIcons icon) {
        size(18, 18);
        overlay(icon.asUITexture());
    }

    @Override
    public @Nullable IDrawable getBackground() {
        return isPressed ? pressedBackGround : buttonBackGround;
    }

    @Override
    public @Nullable IDrawable getHoverBackground() {
        return isPressed ? pressedBackGround : hoverBackGround;
    }

    public IconButton setPressed(boolean pressed) {
        this.isPressed = pressed;
        return this;
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        IDrawable bg = getCurrentOverlay(widgetTheme);
        if (bg != null) {
            Area area = getArea().createCopy();
            area.getPadding()
                .set(Box.ONE);
            bg.drawAtZeroPadded(context, area, getActiveWidgetTheme(widgetTheme, isHovering()));
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        return isPressed ? Result.IGNORE : super.onMousePressed(mouseButton);
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return isPressed ? false : super.onMouseRelease(mouseButton);
    }

    @NotNull
    @Override
    public Result onMouseTapped(int mouseButton) {
        return isPressed ? Result.IGNORE : super.onMouseTapped(mouseButton);
    }

    @Override
    public @NotNull Result onKeyPressed(char typedChar, int keyCode) {
        return isPressed ? Result.IGNORE : super.onKeyPressed(typedChar, keyCode);
    }

    @Override
    public boolean onKeyRelease(char typedChar, int keyCode) {
        return isPressed ? false : super.onKeyRelease(typedChar, keyCode);
    }

    @NotNull
    @Override
    public Result onKeyTapped(char typedChar, int keyCode) {
        return isPressed ? Result.IGNORE : super.onKeyTapped(typedChar, keyCode);
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        return isPressed ? false : super.onMouseScroll(scrollDirection, amount);
    }
}
