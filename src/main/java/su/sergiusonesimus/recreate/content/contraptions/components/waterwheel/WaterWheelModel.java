package su.sergiusonesimus.recreate.content.contraptions.components.waterwheel;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.AllModelTextures;

public class WaterWheelModel extends ModelBase {

    protected ModelRenderer core;
    protected Direction.Axis axis;
    private final ModelRenderer innerSegment_r1;
    private final ModelRenderer connector_r1;
    private final ModelRenderer connector_r2;
    private final ModelRenderer innerSegment_r2;
    AllModelTextures texture = AllModelTextures.WATER_WHEEL;

    public WaterWheelModel() {
        textureWidth = 256;
        textureHeight = 256;
        this.axis = Direction.Axis.Y;
        core = new ModelRenderer(this);

        setRotation(0);

        // core.setRotationPoint(0.0F, 24.0F, 0.0F);
        core.setRotationPoint(0.0F, 0.0F, 0.0F);
        core.cubeList.add(new ModelBox(core, 60, 97, 5.0F, -18.0F + 9F, 8.0F, 1, 18, 8, 0.0F));
        core.cubeList.add(new ModelBox(core, 96, 102, -6.0F, -18.0F + 9F, -16.0F, 1, 18, 8, 0.0F));
        core.cubeList.add(new ModelBox(core, 122, 57, -16.0F, -18.0F + 9F, 5.0F, 8, 18, 1, 0.0F));
        core.cubeList.add(new ModelBox(core, 132, 76, 8.0F, -18.0F + 9F, -6.0F, 8, 18, 1, 0.0F));
        core.cubeList.add(new ModelBox(core, 84, 52, -10.0F, -17.5F + 9F, -4.0F, 2, 17, 8, 0.0F));
        core.cubeList.add(new ModelBox(core, 84, 77, 7.9F, -17.5F + 9F, -4.0F, 2, 17, 8, 0.0F));
        core.cubeList.add(new ModelBox(core, 122, 19, -4.0F, -17.5F + 9F, -10.0F, 8, 17, 2, 0.0F));
        core.cubeList.add(new ModelBox(core, 0, 122, -4.0F, -17.5F + 9F, 7.9F, 8, 17, 2, 0.0F));
        core.cubeList.add(new ModelBox(core, 114, 104, -3.0F, -16.0F + 9F, -3.0F, 6, 14, 6, 0.0F));
        core.cubeList.add(new ModelBox(core, 132, 124, -2.0F, -17.0F + 9F, -2.0F, 4, 16, 4, 0.0F));
        core.cubeList.add(new ModelBox(core, 0, 26, -13.0F, -0.4F + 9F, -13.0F, 26, 0, 26, 0.0F));
        core.cubeList.add(new ModelBox(core, 0, 0, -13.0F, -17.7F + 9F, -13.0F, 26, 0, 26, 0.0F));

        innerSegment_r1 = new ModelRenderer(this);
        innerSegment_r1.setRotationPoint(0.0F, -9.1F, 0.0F);
        core.addChild(innerSegment_r1);
        setRotationAngle(innerSegment_r1, 0.0F, 0.7854F, 0.0F);
        innerSegment_r1.cubeList.add(new ModelBox(innerSegment_r1, 40, 97, -10.0F, -8.4F + 9F, -4.0F, 2, 17, 8, 0.0F));
        innerSegment_r1.cubeList.add(new ModelBox(innerSegment_r1, 20, 97, 7.9F, -8.4F + 9F, -4.0F, 2, 17, 8, 0.0F));
        innerSegment_r1.cubeList.add(new ModelBox(innerSegment_r1, 122, 0, -4.0F, -8.4F + 9F, 7.9F, 8, 17, 2, 0.0F));
        innerSegment_r1.cubeList.add(new ModelBox(innerSegment_r1, 92, 128, -16.0F, -8.9F + 9F, 5.0F, 8, 18, 1, 0.0F));
        innerSegment_r1.cubeList.add(new ModelBox(innerSegment_r1, 78, 102, 5.0F, -8.9F + 9F, 8.0F, 1, 18, 8, 0.0F));
        innerSegment_r1.cubeList.add(new ModelBox(innerSegment_r1, 122, 38, 8.0F, -8.9F + 9F, -6.0F, 8, 18, 1, 0.0F));

        connector_r1 = new ModelRenderer(this);
        connector_r1.setRotationPoint(0.0F, -9.0F, 0.0F);
        core.addChild(connector_r1);
        setRotationAngle(connector_r1, 0.0F, -0.3927F, 0.0F);
        connector_r1.cubeList.add(new ModelBox(connector_r1, 0, 82, -9.0F, -6.0F + 9F, -1.5F, 18, 12, 3, 0.0F));
        connector_r1.cubeList.add(new ModelBox(connector_r1, 0, 52, -1.5F, -6.0F + 9F, -9.0F, 3, 12, 18, 0.0F));
        connector_r1.cubeList.add(new ModelBox(connector_r1, 74, 128, -16.0F, -9.0F + 9F, 5.0F, 8, 18, 1, 0.0F));
        connector_r1.cubeList.add(new ModelBox(connector_r1, 114, 78, -6.0F, -9.0F + 9F, -16.0F, 1, 18, 8, 0.0F));
        connector_r1.cubeList.add(new ModelBox(connector_r1, 104, 0, 5.0F, -9.0F + 9F, 8.0F, 1, 18, 8, 0.0F));
        connector_r1.cubeList.add(new ModelBox(connector_r1, 38, 122, 8.0F, -9.0F + 9F, -6.0F, 8, 18, 1, 0.0F));

        connector_r2 = new ModelRenderer(this);
        connector_r2.setRotationPoint(0.0F, -9.0F, 0.0F);
        core.addChild(connector_r2);
        setRotationAngle(connector_r2, 0.0F, 0.3927F, 0.0F);
        connector_r2.cubeList.add(new ModelBox(connector_r2, 42, 82, -9.0F, -5.9F + 9F, -1.5F, 18, 11, 3, 0.0F));
        connector_r2.cubeList.add(new ModelBox(connector_r2, 42, 52, -1.5F, -5.9F + 9F, -9.0F, 3, 11, 18, 0.0F));
        connector_r2.cubeList.add(new ModelBox(connector_r2, 104, 52, 5.0F, -9.0F + 9F, 8.0F, 1, 18, 8, 0.0F));
        connector_r2.cubeList.add(new ModelBox(connector_r2, 104, 26, -6.0F, -9.0F + 9F, -16.0F, 1, 18, 8, 0.0F));
        connector_r2.cubeList.add(new ModelBox(connector_r2, 114, 124, 8.0F, -9.0F + 9F, -6.0F, 8, 18, 1, 0.0F));
        connector_r2.cubeList.add(new ModelBox(connector_r2, 56, 123, -16.0F, -9.0F + 9F, 5.0F, 8, 18, 1, 0.0F));

        innerSegment_r2 = new ModelRenderer(this);
        innerSegment_r2.setRotationPoint(0.0F, -9.1F, 0.0F);
        core.addChild(innerSegment_r2);
        setRotationAngle(innerSegment_r2, 0.0F, -0.7854F, 0.0F);
        innerSegment_r2.cubeList.add(new ModelBox(innerSegment_r2, 0, 97, -10.0F, -8.4F + 9F, -4.0F, 2, 17, 8, 0.0F));
        innerSegment_r2.cubeList.add(new ModelBox(innerSegment_r2, 20, 122, -16.0F, -8.9F + 9F, 5.0F, 8, 18, 1, 0.0F));

    }

    public WaterWheelModel setAxis(Direction.Axis axis) {
        this.axis = axis;
        switch (this.axis) {
            case X:
                core.rotateAngleX = (float) (-Math.PI);
                core.rotateAngleY = 0;
                core.rotateAngleZ = (float) (-Math.PI / 2);
                break;
            default:
            case Y:
                core.rotateAngleX = 0;
                core.rotateAngleY = 0;
                core.rotateAngleZ = 0;
                break;
            case Z:
                core.rotateAngleX = (float) (-Math.PI / 2);
                core.rotateAngleY = 0;
                core.rotateAngleZ = (float) (-Math.PI);
                break;
        }
        return this;
    }

    public WaterWheelModel setRotation(float angle) {
        switch (axis) {
            default:
            case X:
            case Y:
                core.rotateAngleY = angle;
                break;
            case Z:
                core.rotateAngleZ = angle;
                break;
        }
        return this;
    }

    public void render() {
        texture.bind();
        core.render(0.0625f);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
