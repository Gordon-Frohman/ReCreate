package su.sergiusonesimus.recreate.content.contraptions.relays.elementary;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.AllModelTextures;

public abstract class AbstractShaftModel extends ModelBase {

    AllModelTextures texture;
    protected int textureWidth;
    protected int textureHeight;
    protected ModelRenderer core;
    protected Axis axis;

    public AbstractShaftModel(AllModelTextures texture, int textureWidth, int textureHeight) {
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        core = new ModelRenderer(this).setTextureSize(textureWidth, textureHeight);
        this.axis = Axis.Y;
        setRotation(0);
    }

    public AbstractShaftModel setAxis(Axis axis) {
        this.axis = axis;
        return this;
    }

    public AbstractShaftModel setRotation(float angle) {
        core.rotateAngleY = angle;
        return this;
    }

    private void renderCore() {
        float angle = 0;
        float x = 0;
        float y = 0;
        float z = 0;
        switch (axis) {
            case X:
                angle = -90;
                z = 1;
                break;
            case Y:
                break;
            case Z:
                angle = 90;
                x = 1;
                break;
        }

        GL11.glPushMatrix();

        GL11.glRotatef(angle, x, y, z);
        core.render(0.0625f);

        GL11.glPopMatrix();
    }

    public void render() {
        texture.bind();
        renderCore();
    }

    public void render(TileEntitySpecialRenderer renderer) {
        renderer.bindTexture(texture.getLocation());
        renderCore();
    }

}
