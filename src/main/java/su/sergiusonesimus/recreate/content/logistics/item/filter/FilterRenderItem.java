package su.sergiusonesimus.recreate.content.logistics.item.filter;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.recreate.AllModelTextures;
import su.sergiusonesimus.recreate.content.logistics.item.filter.FilterItem.FilterType;

public class FilterRenderItem implements IItemRenderer {

    private FilterModel model = new FilterModel();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper != ItemRendererHelper.BLOCK_3D;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();

        double scale;
        switch (type) {
            case ENTITY:
                if (RenderItem.renderInFrame) {
                    GL11.glRotated(90, 1D, 0, 0);
                    GL11.glRotated(90, 0, 1D, 0);
                    GL11.glTranslated(0.15D, 0D, 0D);
                }
                break;
            case EQUIPPED_FIRST_PERSON:
                scale = 1.19D;
                GL11.glScaled(scale, scale, scale);
                GL11.glTranslated(0.5D, 0.5D, 0.5D);
                GL11.glRotated(45, 0, 1D, 0);
                GL11.glRotated(80, 0, 0, 1D);
                GL11.glTranslated(0.3D, 0.3D, -0.6D);
                break;
            case EQUIPPED:
                GL11.glEnable(GL11.GL_CULL_FACE);
                GL11.glCullFace(GL11.GL_FRONT);

                GL11.glTranslated(0.5D, 0.5D, 0.5D);
                GL11.glRotated(45, 0, 1D, 0);
                GL11.glRotated(90, 0, 0, 1D);
                GL11.glTranslated(0.55D, 0.11D, 0.35D);
                break;
            case INVENTORY:
                scale = 1.19D;
                GL11.glScaled(scale, scale, scale);
                break;
            default:
                break;
        }

        model.render(
            ((FilterItem) item.getItem()).getType() == FilterType.REGULAR ? AllModelTextures.FILTER
                : AllModelTextures.ATTRIBUTE_FILTER);

        if (type == ItemRenderType.EQUIPPED) {
            GL11.glCullFace(GL11.GL_BACK);
            GL11.glDisable(GL11.GL_CULL_FACE);
        }

        GL11.glPopMatrix();
    }

}
