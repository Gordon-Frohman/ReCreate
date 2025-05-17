package su.sergiusonesimus.recreate.zmixin.mixins;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import su.sergiusonesimus.metaworlds.util.GeometryHelper3D;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftTileEntityRenderer;
import su.sergiusonesimus.recreate.zmixin.interfaces.IMixinTexturedQuad;

@Mixin(TexturedQuad.class)
public class MixinTexturedQuad implements IMixinTexturedQuad {

	@Shadow(remap = true)
    public PositionTextureVertex[] vertexPositions;
	
	private static int destructionPhase = -1;

	@Override
	public void setDestructionPhase(int phase) {
		destructionPhase = phase;
	}
	
	@Inject(method = "draw", at = @At(value = "HEAD"))
	public void draw(Tessellator tessellator, float scale, CallbackInfo ci) {
		RenderGlobal rg = Minecraft.getMinecraft().renderGlobal;
		if(destructionPhase > -1 && destructionPhase < rg.destroyBlockIcons.length) {
			IIcon destroyIcon = rg.destroyBlockIcons[destructionPhase];
			Vector3D[] v3 = new Vector3D[4];
			Vector2D[] v2 = new Vector2D[4];
			for(int i = 0; i < 4; i++) {
				v3[i] = GeometryHelper3D.transformVector(vertexPositions[i].vector3D);
			}
			Vector3D center = new Vector3D(0.5, 0.5, 0.5);
			Plane plane = new Plane(v3[1], v3[2], v3[3]);
			Vector2D projectedCenter = plane.toSubSpace(center);
			Vector2D d = new Vector2D(0.5, 0.5);
			for(int i = 0; i < 4; i++) {
				v2[i] = plane.toSubSpace(v3[i]).subtract(projectedCenter).add(d);
				vertexPositions[i].texturePositionX = (float) (destroyIcon.getMinU() + v2[i].getX());
				vertexPositions[i].texturePositionY = (float) (destroyIcon.getMinV() + v2[i].getY());
			}
		}
	}

}
