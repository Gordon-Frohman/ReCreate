package su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import su.sergiusonesimus.recreate.AllModelTextures;
import su.sergiusonesimus.recreate.util.Direction.Axis;
import su.sergiusonesimus.recreate.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.zmixin.interfaces.IMixinTexturedQuad;

public class ShaftModel extends ModelBase {

    ModelRenderer shaft;
    Axis axis;
    
    public ShaftModel() {
    	shaft = new ModelRenderer(this, 0, 0).setTextureSize(32, 32);
    	shaft.addBox(-2F, -8F, -2F, 4, 16, 4, 0F);
    	shaft.setRotationPoint(0F, 0F, 0F);
    	axis = Axis.Y;
    	setRotation(0);
    }
    
    public ShaftModel(AxisDirection direction) {
    	shaft = new ModelRenderer(this, 0, 0).setTextureSize(32, 32);
    	shaft.addBox(-2F, direction == AxisDirection.POSITIVE? -8F : 0, -2F, 4, 8, 4, 0F);
    	shaft.setRotationPoint(0F, 0F, 0F);
    	axis = Axis.Y;
    	setRotation(0);
    }
    
    public ShaftModel setAxis(Axis axis) {
    	this.axis = axis;
    	switch(this.axis) {
		case X:
			shaft.rotateAngleX = 0;
			shaft.rotateAngleY = 0;
			shaft.rotateAngleZ = (float) (- Math.PI / 2);
			break;
		default:
		case Y:
			shaft.rotateAngleX = 0;
			shaft.rotateAngleY = 0;
			shaft.rotateAngleZ = 0;
			break;
		case Z:
			shaft.rotateAngleX = (float) (- Math.PI / 2);
			shaft.rotateAngleY = 0;
			shaft.rotateAngleZ = 0;
			break;
    	}
    	return this;
    }
    
    public ShaftModel setRotation(float angle) {
    	switch(axis) {
    		default:
    		case X:
    		case Y:
    			shaft.rotateAngleY = angle;
    			break;
    		case Z:
    			shaft.rotateAngleZ = angle;
    			break;
    	}
    	return this;
    }
    
    public void render() {
        AllModelTextures.SHAFT.bind();
    	shaft.render(0.0625F);
    }
    
    public void renderDamageTexture(int progress) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        float alpha = (progress + 1) / 10.0F; // 0.1-1.0
        GL11.glColor4f(1, 1, 1, alpha);
        
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        ((IMixinTexturedQuad)((ModelBox)shaft.cubeList.get(0)).quadList[0]).setDestructionPhase(progress);
    	shaft.render(0.0625F);
        ((IMixinTexturedQuad)((ModelBox)shaft.cubeList.get(0)).quadList[0]).setDestructionPhase(-1);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1, 1, 1, 1);
    }

}
