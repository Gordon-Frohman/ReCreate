package su.sergiusonesimus.recreate.content.contraptions.relays.belt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.content.contraptions.base.CasingBlock.CasingType;
import su.sergiusonesimus.recreate.foundation.utility.OffsetModelBox;

public class BeltCasingModel extends ModelBase {

    public ModelRenderer casing, pulley, back, diagonal;
    public ModelRenderer[] bottom, sides;

    @SuppressWarnings("unchecked")
    public BeltCasingModel(BeltSlope slopeType, BeltPart partType) {
        textureWidth = 64;
        textureHeight = 64;

        casing = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
        casing.setRotationPoint(0F, 0F, 0F);

        //@formatter:off
        if(partType != BeltPart.MIDDLE) {
			pulley = new ModelRenderer(this, 40, 0).setTextureSize(textureWidth, textureHeight);
			pulley.cubeList.add(new OffsetModelBox(pulley,
        		40, 0,
        		-4F, -3F, -7.1F,
        		8, 6, 0,
        		0));
			pulley.cubeList.add(new OffsetModelBox(pulley,
        		40, 0,
        		-4F, -3F, 7.1F,
        		8, 6, 0,
        		0));
			pulley.setRotationPoint(0F, 0F, 0F);
			pulley.rotateAngleY = (float) Math.PI / 2F;
            casing.addChild(pulley);
        }
        
        switch(slopeType) {
	        case HORIZONTAL:
	        	bottom = new ModelRenderer[1];
                bottom[0] = new ModelRenderer(this, 0, (partType == BeltPart.MIDDLE || partType == BeltPart.PULLEY)? 0 : 19).setTextureSize(textureWidth, textureHeight);
                bottom[0].cubeList.add(new OffsetModelBox(bottom[0],
            		0, (partType == BeltPart.MIDDLE || partType == BeltPart.PULLEY)? 0 : 19,
            		-8F, -8F, -8F,
            		16, 16, 3,
            		0));
                bottom[0].setRotationPoint(0F, 0F, 0F);
                bottom[0].rotateAngleX = (float) -Math.PI / 2F;
                casing.addChild(bottom[0]);

                sides = new ModelRenderer[3];
				sides[0] = new ModelRenderer(this, 38, 14).setTextureSize(textureWidth, textureHeight);
	        	switch(partType) {
					case MIDDLE:
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		38, 14,
		            		-8F, -8F, -3F,
		            		1, 16, 8,
		            		0.9F, 16F, 8F,
		            		0));
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		38, 14,
		            		7.1F, -8F, -3F,
		            		1, 16, 8,
		            		0.9F, 16F, 8F,
		            		0));
						break;
					default:
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		38, 14,
		            		-8F, -8F, -3F,
		            		1, 4, 8,
		            		0.9F, 4F, 8F,
		            		0));
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		38, 14,
		            		7.1F, -8F, -3F,
		            		1, 4, 8,
		            		0.9F, 4F, 8F,
		            		0));
						sides[1] = new ModelRenderer(this, 58, 0).setTextureSize(textureWidth, textureHeight);
						sides[1].cubeList.add(new OffsetModelBox(sides[1],
		            		58, 0,
		            		-8F, -4F, 3F,
		            		1, 8, 2,
		            		0.9F, 8F, 2F,
		            		0));
						sides[1].cubeList.add(new OffsetModelBox(sides[1],
		            		58, 0,
		            		7.1F, -4F, 3F,
		            		1, 8, 2,
		            		0.9F, 8F, 2F,
		            		0));
						sides[0].addChild(sides[1]);
						sides[2] = new ModelRenderer(this, 38, 14).setTextureSize(textureWidth, textureHeight);
						sides[2].cubeList.add(new OffsetModelBox(sides[2],
		            		38, 14,
		            		-8F, -8F, -3F,
		            		1, 4, 8,
		            		0.9F, 4F, 8F,
		            		0));
						sides[2].cubeList.add(new OffsetModelBox(sides[2],
		            		38, 14,
		            		7.1F, -8F, -3F,
		            		1, 4, 8,
		            		0.9F, 4F, 8F,
		            		0));
						sides[2].setRotationPoint(0F, 0F, 0F);
						sides[2].rotateAngleX = (float) Math.PI / 2F;
						sides[2].rotateAngleY = (float) Math.PI;
		                casing.addChild(sides[2]);
						break;
	        	}
				sides[0].setRotationPoint(0F, 0F, 0F);
				sides[0].rotateAngleX = (float) Math.PI / 2F;
                casing.addChild(sides[0]);
	        	break;
	        case VERTICAL, SIDEWAYS:
                sides = new ModelRenderer[4];
				sides[0] = new ModelRenderer(this, 0, 42).setTextureSize(textureWidth, textureHeight);
	        	switch(partType) {
					case MIDDLE:
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		0, 42,
		            		-8F, -8F, -3F,
		            		1, 16, 6,
		            		0.9F, 16F, 6F,
		            		0));
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		0, 42,
		            		7.1F, -8F, -3F,
		            		1, 16, 6,
		            		0.9F, 16F, 6F,
		            		0));
						break;
					default:
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		0, 42,
		            		-8F, -8F, -3F,
		            		1, 4, 6,
		            		0.9F, 4F, 6F,
		            		0));
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
							0, 42,
		            		7.1F, -8F, -3F,
		            		1, 4, 6,
		            		0.9F, 4F, 6F,
		            		0));
						sides[1] = new ModelRenderer(this, 0, 42).setTextureSize(textureWidth, textureHeight);
						sides[1].cubeList.add(new OffsetModelBox(sides[1],
							0, 42,
		            		-8F, -8F, -3F,
		            		1, 4, 6,
		            		0.9F, 4F, 6F,
		            		0));
						sides[1].cubeList.add(new OffsetModelBox(sides[1],
							0, 42,
		            		7.1F, -8F, -3F,
		            		1, 4, 6,
		            		0.9F, 4F, 6F,
		            		0));
						sides[1].setRotationPoint(0F, 0F, 0F);
						sides[1].rotateAngleX = (float) Math.PI / 2F;
						sides[1].rotateAngleY = (float) Math.PI;
		                casing.addChild(sides[1]);
						sides[2] = new ModelRenderer(this, 34, 59).setTextureSize(textureWidth, textureHeight);
						sides[2].addBox(
		            		-6F, -8F, -6F,
		            		12, 2, 3,
		            		0);
						sides[2].addBox(
		            		-6F, 6F, -6F,
		            		12, 2, 3,
		            		0);
						sides[2].setRotationPoint(0F, 0F, 0F);
						sides[2].rotateAngleZ = (float) Math.PI / 2F;
						sides[2].rotateAngleY = (float) Math.PI / 2F;
		                casing.addChild(sides[2]);
						sides[3] = new ModelRenderer(this, 34, 59).setTextureSize(textureWidth, textureHeight);
						sides[3].addBox(
		            		-6F, -8F, -6F,
		            		12, 2, 3,
		            		0);
						sides[3].addBox(
		            		-6F, 6F, -6F,
		            		12, 2, 3,
		            		0);
						sides[3].setRotationPoint(0F, 0F, 0F);
						sides[3].rotateAngleZ = (float) Math.PI / 2F;
						sides[3].rotateAngleY = (float) -Math.PI / 2F;
		                casing.addChild(sides[3]);
						break;
	        	}
				sides[0].setRotationPoint(0F, 0F, 0F);
				sides[0].rotateAngleX = (float) Math.PI / 2F;
	            casing.addChild(sides[0]);
	        	break;
	        default:
                sides = new ModelRenderer[4];
	        	sides[0] = new ModelRenderer(this, 38, 14).setTextureSize(textureWidth, textureHeight);
	        	switch(partType) {
					case START:
						bottom = new ModelRenderer[2];
		                bottom[0] = new ModelRenderer(this, 0, 19).setTextureSize(textureWidth, textureHeight);
		                bottom[0].addBox(
		            		-8F, -8F, -8F,
		            		16, 13, 3,
		            		0);
		                bottom[1] = new ModelRenderer(this, 26, 42).setTextureSize(textureWidth, textureHeight);
		                bottom[1].cubeList.add(new OffsetModelBox(bottom[1],
		            		26, 42,
		            		-8F, 5F, -8F,
		            		16, 3, 3,
		            		0));
		                bottom[0].addChild(bottom[1]);;
		                bottom[0].setRotationPoint(0F, 0F, 0F);
		                bottom[0].rotateAngleX = (float) -Math.PI / 2F;
		                casing.addChild(bottom[0]);
		                back = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
		                back.addBox(
		            		-8F, -5F, -8F,
		            		16, 8, 3,
		            		0);
		                back.setRotationPoint(0F, 0F, 0F);
		                casing.addChild(back);
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		38, 14,
		            		-8F, -5F, -3F,
		            		1, 1, 8,
		            		0.9F, 1F, 8F,
		            		0));
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		38, 14,
		            		7.1F, -5F, -3F,
		            		1, 1, 8,
		            		0.9F, 1F, 8F,
		            		0));
						sides[1] = new ModelRenderer(this, 58, 0).setTextureSize(textureWidth, textureHeight);
						sides[1].cubeList.add(new OffsetModelBox(sides[1],
		            		58, 0,
		            		-8F, -4F, 3F,
		            		1, 8, 2,
		            		0.9F, 8F, 2F,
		            		0));
						sides[1].cubeList.add(new OffsetModelBox(sides[1],
		            		58, 0,
		            		7.1F, -4F, 3F,
		            		1, 8, 2,
		            		0.9F, 8F, 2F,
		            		0));
						sides[0].addChild(sides[1]);;
						sides[2] = new ModelRenderer(this, 38, 14).setTextureSize(textureWidth, textureHeight);
						sides[2].cubeList.add(new OffsetModelBox(sides[2],
		            		38, 14,
		            		-8F, -8F, -3F,
		            		1, 4, 8,
		            		0.9F, 4F, 8F,
		            		0));
						sides[2].cubeList.add(new OffsetModelBox(sides[2],
		            		38, 14,
		            		7.1F, -8F, -3F,
		            		1, 4, 8,
		            		0.9F, 4F, 8F,
		            		0));
						sides[2].setRotationPoint(0F, 0F, 0F);
						sides[2].rotateAngleX = (float) Math.PI / 2F;
						sides[2].rotateAngleY = (float) Math.PI;
		                casing.addChild(sides[2]);

						diagonal = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
						diagonal.cubeList.add(new OffsetModelBox(diagonal,
		            		0, 0,
		            		-8.1F, 3.325F, -6F,
		            		16, 8, 3,
		            		16.2F, 8.025F, 2.9F,
		            		0));
						
						sides[3] = new ModelRenderer(this, 18, 42).setTextureSize(textureWidth, textureHeight);
						sides[3].cubeList.add(new OffsetModelBox(sides[3],
		            		18, 42,
		            		-8.1F, 3.325F, -3F,
		            		1, 8, 6,
		            		1.1F, 8.025F, 6.1F,
		            		0));
						sides[3].cubeList.add(new OffsetModelBox(sides[3],
		            		18, 42,
		            		7F, 3.325F, -3F,
		            		1, 8, 6,
		            		1.1F, 8.025F, 6.1F,
		            		0));
						sides[3].setRotationPoint(0F, 0F, 0F);
						sides[3].rotateAngleY = (float) Math.PI;
						diagonal.addChild(sides[3]);
						
						diagonal.setRotationPoint(0F, 0F, 0F);
						diagonal.rotateAngleX = (float) -Math.PI / 4F;
		                casing.addChild(diagonal);
						break;
					case END:
						bottom = new ModelRenderer[1];
		                bottom[0] = new ModelRenderer(this, 0, 19).setTextureSize(textureWidth, textureHeight);
		                bottom[0].addBox(
		            		-8F, -8F, -8F,
		            		16, 10, 3,
		            		0);
		                bottom[0].setRotationPoint(0F, 0F, 0F);
		                bottom[0].rotateAngleX = (float) -Math.PI / 2F;
		                bottom[0].rotateAngleY = (float) Math.PI;
		                casing.addChild(bottom[0]);
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		38, 14,
		            		-8F, -8F, -3F,
		            		1, 4, 8,
		            		0.9F, 4F, 8F,
		            		0));
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		38, 14,
		            		7.1F, -8F, -3F,
		            		1, 4, 8,
		            		0.9F, 4F, 8F,
		            		0));
						sides[1] = new ModelRenderer(this, 58, 0).setTextureSize(textureWidth, textureHeight);
						sides[1].cubeList.add(new OffsetModelBox(sides[1],
		            		58, 0,
		            		-8F, -4F, 3F,
		            		1, 8, 2,
		            		0.9F, 8F, 2F,
		            		0));
						sides[1].cubeList.add(new OffsetModelBox(sides[1],
		            		58, 0,
		            		7.1F, -4F, 3F,
		            		1, 8, 2,
		            		0.9F, 8F, 2F,
		            		0));
						sides[0].addChild(sides[1]);;

						diagonal = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
						diagonal.cubeList.add(new OffsetModelBox(diagonal,
		            		0, 0,
		            		-8.1F, -11.303F, -6F,
		            		16, 8, 3,
		            		16.2F, 8.025F, 2.9F,
		            		0));
						
						sides[2] = new ModelRenderer(this, 18, 42).setTextureSize(textureWidth, textureHeight);
						sides[2].cubeList.add(new OffsetModelBox(sides[2],
		            		18, 42,
		            		-8.1F, -11.303F, -3F,
		            		1, 8, 6,
		            		1.1F, 8.025F, 6.1F,
		            		0));
						sides[2].cubeList.add(new OffsetModelBox(sides[2],
		            		18, 42,
		            		7F, -11.303F, -3F,
		            		1, 8, 6,
		            		1.1F, 8.025F, 6.1F,
		            		0));
						sides[2].setRotationPoint(0F, 0F, 0F);
						sides[2].rotateAngleY = (float) Math.PI;
						diagonal.addChild(sides[2]);
						
						diagonal.setRotationPoint(0F, 0F, 0F);
						diagonal.rotateAngleX = (float) -Math.PI / 4F;
		                casing.addChild(diagonal);
						break;
					case MIDDLE:
						diagonal = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
						diagonal.cubeList.add(new OffsetModelBox(diagonal,
		            		0, 0,
		            		-8.1F, -11.3F, -6F,
		            		16, 9, 3,
		            		16.2F, 9.3F, 2.9F,
		            		0));
						diagonal.cubeList.add(new OffsetModelBox(diagonal,
		            		0, 0,
		            		-8.1F, -2F, -6F,
		            		16, 13, 3,
		            		16.2F, 13.325F, 2.9F,
		            		0));
						
						sides[1] = new ModelRenderer(this, 18, 42).setTextureSize(textureWidth, textureHeight);
						sides[1].cubeList.add(new OffsetModelBox(sides[1],
		            		18, 42,
		            		-8.1F, -11.3F, -3F,
		            		1, 9, 6,
		            		1.1F, 9.3F, 6.1F,
		            		0));
						sides[1].cubeList.add(new OffsetModelBox(sides[1],
		            		18, 42,
		            		7F, -11.3F, -3F,
		            		1, 9, 6,
		            		1.1F, 9.3F, 6.1F,
		            		0));
						diagonal.addChild(sides[1]);
						sides[2] = new ModelRenderer(this, 18, 42).setTextureSize(textureWidth, textureHeight);
						sides[2].cubeList.add(new OffsetModelBox(sides[2],
		            		18, 42,
		            		-8.1F, -2F, -3F,
		            		1, 13, 6,
		            		1.1F, 13.325F, 6.1F,
		            		0));
						sides[2].cubeList.add(new OffsetModelBox(sides[2],
		            		18, 42,
		            		7F, -2F, -3F,
		            		1, 13, 6,
		            		1.1F, 13.325F, 6.1F,
		            		0));
						sides[1].addChild(sides[2]);
						sides[1].setRotationPoint(0F, 0F, 0F);
						sides[1].rotateAngleY = (float) Math.PI;
						
						diagonal.setRotationPoint(0F, 0F, 0F);
						diagonal.rotateAngleX = (float) -Math.PI / 4F;
		                casing.addChild(diagonal);
						break;
					case PULLEY:
						bottom = new ModelRenderer[2];
		                bottom[0] = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
		                bottom[0].cubeList.add(new OffsetModelBox(bottom[0],
		            		0, 0,
		            		-8F, -2F, -8F,
		            		16, 7, 3,
		            		0));
		                bottom[1] = new ModelRenderer(this, 26, 42).setTextureSize(textureWidth, textureHeight);
		                bottom[1].cubeList.add(new OffsetModelBox(bottom[1],
		            		26, 42,
		            		-8F, 5F, -8F,
		            		16, 3, 3,
		            		0));
		                bottom[0].addChild(bottom[1]);
		                bottom[0].setRotationPoint(0F, 0F, 0F);
		                bottom[0].rotateAngleX = (float) -Math.PI / 2F;
		                casing.addChild(bottom[0]);
		                back = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
		                back.cubeList.add(new OffsetModelBox(back,
		            		0, 0,
		            		-8F, -5F, -8F,
		            		16, 8, 3,
		            		0));
		                back.setRotationPoint(0F, 0F, 0F);
		                casing.addChild(back);
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		38, 14,
		            		-8F, -5F, -3F,
		            		1, 1, 8,
		            		0.9F, 1F, 8F,
		            		0));
						sides[0].cubeList.add(new OffsetModelBox(sides[0],
		            		38, 14,
		            		7.1F, -5F, -3F,
		            		1, 1, 8,
		            		0.9F, 1F, 8F,
		            		0));
						sides[1] = new ModelRenderer(this, 58, 0).setTextureSize(textureWidth, textureHeight);
						sides[1].cubeList.add(new OffsetModelBox(sides[1],
		            		58, 0,
		            		-8F, -4F, 3F,
		            		1, 8, 2,
		            		0.9F, 8F, 2F,
		            		0));
						sides[1].cubeList.add(new OffsetModelBox(sides[1],
		            		58, 0,
		            		7.1F, -4F, 3F,
		            		1, 8, 2,
		            		0.9F, 8F, 2F,
		            		0));
						sides[0].addChild(sides[1]);

						diagonal = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
						diagonal.cubeList.add(new OffsetModelBox(diagonal,
		            		0, 0,
		            		-8.1F, -11.303F, -6F,
		            		16, 8, 3,
		            		16.2F, 8.025F, 2.9F,
		            		0));
						diagonal.cubeList.add(new OffsetModelBox(diagonal,
		            		0, 0,
		            		-8.1F, 3.325F, -6F,
		            		16, 8, 3,
		            		16.2F, 8.025F, 2.9F,
		            		0));
						
						sides[2] = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
						sides[2].cubeList.add(new OffsetModelBox(sides[2],
		            		18, 42,
		            		-8.1F, -11.303F, -3F,
		            		1, 8, 6,
		            		1.1F, 8.025F, 6.1F,
		            		0));
						sides[2].cubeList.add(new OffsetModelBox(sides[2],
		            		18, 42,
		            		7F, -11.303F, -3F,
		            		1, 8, 6,
		            		1.1F, 8.025F, 6.1F,
		            		0));
						sides[2].cubeList.add(new OffsetModelBox(sides[2],
		            		18, 42,
		            		-8.1F, 3.325F, -3F,
		            		1, 8, 6,
		            		1.1F, 8.025F, 6.1F,
		            		0));
						sides[2].cubeList.add(new OffsetModelBox(sides[2],
		            		18, 42,
		            		7F, 3.325F, -3F,
		            		1, 8, 6,
		            		1.1F, 8.025F, 6.1F,
		            		0));
						sides[2].setRotationPoint(0F, 0F, 0F);
						sides[2].rotateAngleY = (float) Math.PI;
						diagonal.addChild(sides[2]);
						
						diagonal.setRotationPoint(0F, 0F, 0F);
						diagonal.rotateAngleX = (float) -Math.PI / 4F;
		                casing.addChild(diagonal);
						break;
	        	}
				sides[0].setRotationPoint(0F, 0F, 0F);
				sides[0].rotateAngleX = (float) Math.PI / 2F;
	            casing.addChild(sides[0]);
	        	break;
        }
        //@formatter:on
    }

    public BeltCasingModel setFace(Direction direction) {
        switch (direction) {
            default:
            case SOUTH:
                break;
            case WEST:
                casing.rotateAngleY = (float) Math.PI / 2F;
                break;
            case NORTH:
                casing.rotateAngleY = (float) Math.PI;
                break;
            case EAST:
                casing.rotateAngleY = (float) -Math.PI / 2F;
                break;
        }
        return this;
    }

    public BeltCasingModel setRotation(float angle) {
        return this;
    }

    public void renderCore() {
        GL11.glPushMatrix();

        casing.render(0.0625F);

        GL11.glPopMatrix();
    }

    public void render(CasingType casing) {
        Minecraft.getMinecraft().renderEngine.bindTexture(getTexture(casing));
        renderCore();
    }

    public void render(CasingType casing, TileEntitySpecialRenderer renderer) {
        renderer.bindTexture(getTexture(casing));
        renderCore();
    }

    private ResourceLocation getTexture(CasingType casing) {
        if (casing == CasingType.BRASS) {
            return BeltBlock.brassCasingLocation;
        } else {
            return BeltBlock.andesiteCasingLocation;
        }
    }

}
