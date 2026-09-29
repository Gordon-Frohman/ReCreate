package su.sergiusonesimus.recreate.foundation.gui.widget;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;

import su.sergiusonesimus.recreate.foundation.gui.AllGuiTextures;

public class Indicator extends Widget<Indicator> {

    private State state;

    public Indicator() {
        size(AllGuiTextures.INDICATOR.width, AllGuiTextures.INDICATOR.height);
        this.state = State.OFF;
    }

    @Override
    public @Nullable IDrawable getCurrentBackground(WidgetThemeEntry<?> widgetTheme) {
        AllGuiTextures toDraw;
        switch (state) {
            case ON:
                toDraw = AllGuiTextures.INDICATOR_WHITE;
                break;
            case OFF:
                toDraw = AllGuiTextures.INDICATOR;
                break;
            case RED:
                toDraw = AllGuiTextures.INDICATOR_RED;
                break;
            case YELLOW:
                toDraw = AllGuiTextures.INDICATOR_YELLOW;
                break;
            case GREEN:
                toDraw = AllGuiTextures.INDICATOR_GREEN;
                break;
            default:
                toDraw = AllGuiTextures.INDICATOR;
                break;
        }
        return toDraw.asUITexture();
    }

    public Indicator setState(State state) {
        this.state = state;
        return this;
    }

    public enum State {
        OFF,
        ON,
        RED,
        YELLOW,
        GREEN;
    }

}
