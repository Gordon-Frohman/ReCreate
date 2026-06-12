package su.sergiusonesimus.recreate.content.contraptions.relays.belt;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.foundation.utility.animation.ScrollingModelBox;

public class BeltModel extends ModelBase {

    private boolean isDiagonal;

    public ModelRenderer belt;
    public ModelRenderer top;
    public ModelRenderer bottom;
    public ModelRenderer side;

    @SuppressWarnings("unchecked")
    public BeltModel(boolean isEnd, boolean isDiagonal) {
        this.isDiagonal = isDiagonal;

        textureWidth = 16;
        textureHeight = 32;

        belt = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
        belt.setRotationPoint(0F, 0F, 0F);

        //@formatter:off
        if(isDiagonal) {
        	if(isEnd) {
                ModelRenderer topDiagonal = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                topDiagonal.cubeList.add(new ScrollingModelBox(topDiagonal,
            		0, 1,
            		-7F, 3F, -2.1F,
            		14, 2, 13,
            		14F, 2F, 13.428F,
            		0, false));
                topDiagonal.cubeList.add(new ScrollingModelBox(topDiagonal,
            		3, 2,
            		-5F, 2F, -1.1F,
            		10, 1, 12,
            		10F, 1F, 12.428F,
            		0, false));
                topDiagonal.setRotationPoint(0F, 0F, 0F);
                topDiagonal.rotateAngleX = (float) Math.PI / 4F;
                belt.addChild(topDiagonal);

                top = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                top.cubeList.add(new ScrollingModelBox(top,
            		0, 4,
            		-7.1F, 3F, -7F,
            		14, 2, 9,
            		14.2F, 2F, 9.1F,
            		0, false));
                top.cubeList.add(new ScrollingModelBox(top,
            		3, 5,
            		-5.1F, 2F, -6F,
            		10, 1, 8,
            		10.2F, 1F, 8.1F,
            		0, false));
                top.setRotationPoint(0F, 0F, 0F);
                belt.addChild(top);
                
                side = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                side.cubeList.add(new ScrollingModelBox(side,
            		0, 6,
            		-7F, 6F, -4F,
            		14, 2, 8,
            		0, false));
                side.cubeList.add(new ScrollingModelBox(side,
            		3, 8,
            		-5.1F, 5F, -2F,
            		10, 1, 4,
            		10.2F, 1F, 4F,
            		0, false));
                side.setRotationPoint(0F, 0F, 0F);
                side.rotateAngleX = (float) -Math.PI / 2F;
                belt.addChild(side);

                bottom = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                bottom.cubeList.add(new ScrollingModelBox(bottom,
            		0, 10.2F,
            		-7.1F, 3F, 1.2F,
            		14, 2, 5.8F,
            		14.2F, 2F, 5.8F,
            		0, false));
                bottom.cubeList.add(new ScrollingModelBox(bottom,
            		3, 10.2F,
            		-5.1F, 2F, 1.2F,
            		10, 1, 4.8F,
            		10.2F, 1F, 4.8F,
            		0, false));
                bottom.setRotationPoint(0F, 0F, 0F);
                bottom.rotateAngleX = (float) Math.PI;
                belt.addChild(bottom);

                ModelRenderer bottomDiagonal = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                bottomDiagonal.cubeList.add(new ScrollingModelBox(bottomDiagonal,
            		0, 2,
            		-7F, 3F, -11.325F,
            		14, 2, 10,
            		14F, 2F, 10.125F,
            		0, false));
                bottomDiagonal.cubeList.add(new ScrollingModelBox(bottomDiagonal,
            		3, 2,
            		-5F, 2F, -11.325F,
            		10, 1, 10,
            		10F, 1F, 10.125F,
            		0, false));
                bottomDiagonal.setRotationPoint(0F, 0F, 0F);
                bottomDiagonal.rotateAngleX = (float) Math.PI / 4F + (float) Math.PI;
                belt.addChild(bottomDiagonal);
        	} else {
                ModelRenderer topDiagonal1 = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                topDiagonal1.cubeList.add(new ScrollingModelBox(topDiagonal1,
            		0, 2,
            		-7F, 3F, -11.3F,
            		14, 2, 12,
            		14F, 2F, 11.32F,
            		0, false));
                topDiagonal1.cubeList.add(new ScrollingModelBox(topDiagonal1,
            		3, 2,
            		-5F, 2F, -11.305F,
            		10, 1, 12,
            		10F, 1F, 11.325F,
            		0, false));
                topDiagonal1.setRotationPoint(0F, 0F, 0F);
                topDiagonal1.rotateAngleX = (float) Math.PI / 4F;
                belt.addChild(topDiagonal1);

                ModelRenderer topDiagonal2 = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                topDiagonal2.cubeList.add(new ScrollingModelBox(topDiagonal2,
            		0, 2,
            		-7F, 3F, 0F,
            		14, 2, 12,
            		14F, 2F, 11.32F,
            		0, false));
                topDiagonal2.cubeList.add(new ScrollingModelBox(topDiagonal2,
            		3, 2,
            		-5F, 2F, 0F,
            		10, 1, 12,
            		10F, 1F, 11.325F,
            		0, false));
                topDiagonal2.setRotationPoint(0F, 0F, 0F);
                topDiagonal2.rotateAngleX = (float) Math.PI / 4F;
                belt.addChild(topDiagonal2);

                ModelRenderer bottomDiagonal1 = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                bottomDiagonal1.cubeList.add(new ScrollingModelBox(bottomDiagonal1,
            		0, 1,
            		-7F, 3F, -11.3F,
            		14, 2, 12,
            		14F, 2F, 11.32F,
            		0, false));
                bottomDiagonal1.cubeList.add(new ScrollingModelBox(bottomDiagonal1,
            		3, 2,
            		-5F, 2F, -11.305F,
            		10, 1, 12,
            		10F, 1F, 11.325F,
            		0, false));
                bottomDiagonal1.setRotationPoint(0F, 0F, 0F);
                bottomDiagonal1.rotateAngleX = (float) Math.PI / 4F + (float) Math.PI;
                belt.addChild(bottomDiagonal1);

                ModelRenderer bottomDiagonal2 = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                bottomDiagonal2.cubeList.add(new ScrollingModelBox(bottomDiagonal2,
            		0, 1,
            		-7F, 3F, 0F,
            		14, 2, 12,
            		14F, 2F, 11.32F,
            		0, false));
                bottomDiagonal2.cubeList.add(new ScrollingModelBox(bottomDiagonal2,
            		3, 2,
            		-5F, 2F, 0F,
            		10, 1, 12,
            		10F, 1F, 11.325F,
            		0, false));
                bottomDiagonal2.setRotationPoint(0F, 0F, 0F);
                bottomDiagonal2.rotateAngleX = (float) Math.PI / 4F + (float) Math.PI;
                belt.addChild(bottomDiagonal2);
        	}
        } else {
        	if(isEnd) {
                top = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                top.cubeList.add(new ScrollingModelBox(top,
            		0, 0,
            		-7F, 3F, -7F,
            		14, 2, 15,
            		0));
                top.cubeList.add(new ScrollingModelBox(top,
            		3, 1,
            		-5F, 2F, -6F,
            		10, 1, 14,
            		0));
                top.setRotationPoint(0F, 0F, 0F);
                belt.addChild(top);
                
                side = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                side.cubeList.add(new ScrollingModelBox(side,
            		0, 7,
            		-7F, 6F, -4F,
            		14, 2, 8,
            		0));
                side.cubeList.add(new ScrollingModelBox(side,
            		3, -7,
            		-5.1F, 5F, -2F,
            		10, 1, 4,
            		10.2F, 1F, 4F,
            		0));
                side.setRotationPoint(0F, 0F, 0F);
                side.rotateAngleX = (float) -Math.PI / 2F;
                belt.addChild(side);

                bottom = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                bottom.cubeList.add(new ScrollingModelBox(bottom,
            		0, -1,
            		-7F, 3F, -8F,
            		14, 2, 15,
            		0));
                bottom.cubeList.add(new ScrollingModelBox(bottom,
            		3, -1,
            		-5F, 2F, -8F,
            		10, 1, 14,
            		0));
                bottom.setRotationPoint(0F, 0F, 0F);
                bottom.rotateAngleX = (float) Math.PI;
                belt.addChild(bottom);
        	} else {
                top = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                top.cubeList.add(new ScrollingModelBox(top,
            		0, -1,
            		-7F, 3F, -8F,
            		14, 2, 16,
            		0));
                top.cubeList.add(new ScrollingModelBox(top,
            		3, -1,
            		-5F, 2F, -8F,
            		10, 1, 16,
            		0));
                top.setRotationPoint(0F, 0F, 0F);
                belt.addChild(top);

                bottom = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
                bottom.cubeList.add(new ScrollingModelBox(bottom,
            		0, -1,
            		-7F, 3F, -8F,
            		14, 2, 16,
            		0));
                bottom.cubeList.add(new ScrollingModelBox(bottom,
            		3, -1,
            		-5F, 2F, -8F,
            		10, 1, 16,
            		0));
                bottom.setRotationPoint(0F, 0F, 0F);
                bottom.rotateAngleX = (float) Math.PI;
                belt.addChild(bottom);
        	}
        }
        //@formatter:on
    }

    public BeltModel setFace(Direction direction) {
        switch (direction) {
            default:
            case SOUTH:
                break;
            case WEST:
                belt.rotateAngleY = (float) Math.PI / 2F;
                break;
            case NORTH:
                belt.rotateAngleY = (float) Math.PI;
                break;
            case EAST:
                belt.rotateAngleY = (float) -Math.PI / 2F;
                break;
        }
        return this;
    }

    public BeltModel setRotation(float angle) {
        return this;
    }

    @SuppressWarnings("unchecked")
    public void setOffset(float newOffset) {
        for (ModelRenderer renderer : (List<ModelRenderer>) belt.childModels) {
            for (ModelBox box : (List<ModelBox>) renderer.cubeList) {
                if (box instanceof ScrollingModelBox scrollingBox) scrollingBox.setOffset(newOffset);
            }
            renderer.compiled = false;
        }
    }

    public void renderCore() {
        GL11.glPushMatrix();

        belt.render(0.0625F);

        GL11.glPopMatrix();
    }

    public void render(Integer color) {
        Minecraft.getMinecraft().renderEngine.bindTexture(getTexture(color));
        renderCore();
    }

    public void render(Integer color, TileEntitySpecialRenderer renderer) {
        renderer.bindTexture(getTexture(color));
        renderCore();
    }

    private ResourceLocation getTexture(Integer color) {
        ResourceLocation texture;
        if (color == null || color < 0 || color >= BeltBlock.coloredScrollLocations.length) {
            if (isDiagonal) {
                texture = BeltBlock.beltScrollDiagonalLocation;
            } else {
                texture = BeltBlock.beltScrollLocation;
            }
        } else {
            if (isDiagonal) {
                texture = BeltBlock.coloredScrollDiagonalLocations[color];
            } else {
                texture = BeltBlock.coloredScrollLocations[color];
            }
        }
        return texture;
    }

}
