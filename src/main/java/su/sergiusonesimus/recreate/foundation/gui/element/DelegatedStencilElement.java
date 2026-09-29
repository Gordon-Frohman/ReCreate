package su.sergiusonesimus.recreate.foundation.gui.element;

import com.cleanroommc.modularui.drawable.UITexture;

import su.sergiusonesimus.recreate.foundation.gui.UIRenderHelper;
import su.sergiusonesimus.recreate.foundation.utility.Color;

public class DelegatedStencilElement extends StencilElement {

    protected static final ElementRenderer EMPTY_RENDERER = (width, height, alpha) -> {};
    protected static final ElementRenderer DEFAULT_ELEMENT = (width, height, alpha) -> UIRenderHelper.angledGradient(
        0,
        -3,
        5,
        height + 4,
        width + 6,
        new Color(0xff_10dd10).scaleAlpha(alpha),
        new Color(0xff_1010dd).scaleAlpha(alpha));

    protected ElementRenderer stencil;
    protected ElementRenderer element;

    public DelegatedStencilElement() {
        stencil = EMPTY_RENDERER;
        element = DEFAULT_ELEMENT;
    }

    public DelegatedStencilElement(ElementRenderer stencil, ElementRenderer element) {
        this.stencil = stencil;
        this.element = element;
    }

    public <T extends DelegatedStencilElement> T withStencilRenderer(ElementRenderer renderer) {
        stencil = renderer;
        // noinspection unchecked
        return (T) this;
    }

    public <T extends DelegatedStencilElement> T withElementRenderer(ElementRenderer renderer) {
        element = renderer;
        // noinspection unchecked
        return (T) this;
    }

    @Override
    protected void renderStencil() {
        stencil.render(width, height, 1);
    }

    @Override
    protected void renderElement() {
        element.render(width, height, alpha);
    }

    @FunctionalInterface
    public interface ElementRenderer {

        void render(int width, int height, float alpha);
    }

    @Override
    public UITexture asUITexture() {
        // TODO Auto-generated method stub
        return null;
    }

}
