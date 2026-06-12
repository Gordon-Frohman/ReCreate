package su.sergiusonesimus.recreate.foundation.utility;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;

public class OffsetModelBox extends ModelBox {

    public OffsetModelBox(ModelRenderer renderer, float texU, float texV, float x, float y, float z, float sizeX,
        float sizeY, float sizeZ, float scale) {
        this(renderer, texU, texV, x, y, z, sizeX, sizeY, sizeZ, sizeX, sizeY, sizeZ, scale);
    }

    public OffsetModelBox(ModelRenderer renderer, float texU, float texV, float x, float y, float z, float sizeX,
        float sizeY, float sizeZ, float actualSizeX, float actualSizeY, float actualSizeZ, float scale) {
        super(renderer, (int) texU, (int) texV, x, y, z, (int) sizeX, (int) sizeY, (int) sizeZ, scale);

        this.posX2 = x + actualSizeX;
        this.posY2 = y + actualSizeY;
        this.posZ2 = z + actualSizeZ;
        float maxX = x + actualSizeX;
        float maxY = y + actualSizeY;
        float maxZ = z + actualSizeZ;
        x -= scale;
        y -= scale;
        z -= scale;
        maxX += scale;
        maxY += scale;
        maxZ += scale;

        if (renderer.mirror) {
            float f7 = maxX;
            maxX = x;
            x = f7;
        }

        PositionTextureVertex v0 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
        PositionTextureVertex v1 = new PositionTextureVertex(maxX, y, z, 0.0F, 8.0F);
        PositionTextureVertex v2 = new PositionTextureVertex(maxX, maxY, z, 8.0F, 8.0F);
        PositionTextureVertex v3 = new PositionTextureVertex(x, maxY, z, 8.0F, 0.0F);
        PositionTextureVertex v4 = new PositionTextureVertex(x, y, maxZ, 0.0F, 0.0F);
        PositionTextureVertex v5 = new PositionTextureVertex(maxX, y, maxZ, 0.0F, 8.0F);
        PositionTextureVertex v6 = new PositionTextureVertex(maxX, maxY, maxZ, 8.0F, 8.0F);
        PositionTextureVertex v7 = new PositionTextureVertex(x, maxY, maxZ, 8.0F, 0.0F);
        this.vertexPositions[0] = v0;
        this.vertexPositions[1] = v1;
        this.vertexPositions[2] = v2;
        this.vertexPositions[3] = v3;
        this.vertexPositions[4] = v4;
        this.vertexPositions[5] = v5;
        this.vertexPositions[6] = v6;
        this.vertexPositions[7] = v7;
        this.quadList[0] = new OffsetTexturedQuad(
            new PositionTextureVertex[] { v5, v1, v2, v6 },
            texU + sizeZ + sizeX,
            texV + sizeZ,
            texU + sizeZ + sizeX + sizeZ,
            texV + sizeZ + sizeY,
            renderer.textureWidth,
            renderer.textureHeight);
        this.quadList[1] = new OffsetTexturedQuad(
            new PositionTextureVertex[] { v0, v4, v7, v3 },
            texU,
            texV + sizeZ,
            texU + sizeZ,
            texV + sizeZ + sizeY,
            renderer.textureWidth,
            renderer.textureHeight);
        this.quadList[2] = new OffsetTexturedQuad(
            new PositionTextureVertex[] { v5, v4, v0, v1 },
            texU + sizeZ,
            texV,
            texU + sizeZ + sizeX,
            texV + sizeZ,
            renderer.textureWidth,
            renderer.textureHeight);
        this.quadList[3] = new OffsetTexturedQuad(
            new PositionTextureVertex[] { v2, v3, v7, v6 },
            texU + sizeZ + sizeX,
            texV + sizeZ,
            texU + sizeZ + sizeX + sizeX,
            texV,
            renderer.textureWidth,
            renderer.textureHeight);
        this.quadList[4] = new OffsetTexturedQuad(
            new PositionTextureVertex[] { v1, v0, v3, v2 },
            texU + sizeZ,
            texV + sizeZ,
            texU + sizeZ + sizeX,
            texV + sizeZ + sizeY,
            renderer.textureWidth,
            renderer.textureHeight);
        this.quadList[5] = new OffsetTexturedQuad(
            new PositionTextureVertex[] { v4, v5, v6, v7 },
            texU + sizeZ + sizeX + sizeZ,
            texV + sizeZ,
            texU + sizeZ + sizeX + sizeZ + sizeX,
            texV + sizeZ + sizeY,
            renderer.textureWidth,
            renderer.textureHeight);

        if (renderer.mirror) {
            for (int j1 = 0; j1 < this.quadList.length; ++j1) {
                this.quadList[j1].flipFace();
            }
        }
    }

}
