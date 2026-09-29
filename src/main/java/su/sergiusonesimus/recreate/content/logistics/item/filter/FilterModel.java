package su.sergiusonesimus.recreate.content.logistics.item.filter;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

import su.sergiusonesimus.recreate.AllModelTextures;

public class FilterModel extends ModelBase {

    protected int textureWidth;
    protected int textureHeight;

    protected ModelRenderer filter;
    protected ModelRenderer[] sideRim = new ModelRenderer[2];
    protected ModelRenderer[] topRim = new ModelRenderer[2];
    protected ModelRenderer net;

    public FilterModel() {
        textureWidth = 32;
        textureHeight = 16;

        filter = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);

        for (int i = 0; i < 2; i++) {
            sideRim[i] = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
            sideRim[i].addBox(-7F, -5F, -1F, 2, 10, 2);
            sideRim[i].setRotationPoint(0F, 0F, 0F);
            sideRim[i].rotateAngleZ = i * (float) Math.PI;
            filter.addChild(sideRim[i]);

            topRim[i] = new ModelRenderer(this, 26, 0).setTextureSize(textureWidth, textureHeight);
            topRim[i].addBox(-5F, -5F, -0.5F, 1, 10, 1);
            topRim[i].setRotationPoint(0F, 0F, 0F);
            topRim[i].rotateAngleZ = (i + 0.5f) * (float) Math.PI;
            filter.addChild(topRim[i]);
        }

        net = new ModelRenderer(this, 8, 0).setTextureSize(textureWidth, textureHeight);
        net.addBox(-4.5F, -5.5F, 0F, 9, 11, 0);
        net.setRotationPoint(0F, 0F, 0F);
        net.rotateAngleZ = (float) Math.PI / 2f;
        filter.addChild(net);

        filter.rotateAngleX = (float) Math.PI / 2f;
        filter.rotateAngleY = (float) Math.PI / 2f;
    }

    public void render(AllModelTextures texture) {
        texture.bind();
        filter.render(0.0625f);
    }

}
