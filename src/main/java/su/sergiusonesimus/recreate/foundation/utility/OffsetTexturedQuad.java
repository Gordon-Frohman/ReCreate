package su.sergiusonesimus.recreate.foundation.utility;

import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;

public class OffsetTexturedQuad extends TexturedQuad {

    public OffsetTexturedQuad(PositionTextureVertex[] vertexPositions, float minU, float minV, float maxU, float maxV,
        float textureWidth, float textureHeight) {
        super(vertexPositions);
        float f2 = 0.0F / textureWidth;
        float f3 = 0.0F / textureHeight;
        vertexPositions[0] = vertexPositions[0].setTexturePosition(maxU / textureWidth - f2, minV / textureHeight + f3);
        vertexPositions[1] = vertexPositions[1].setTexturePosition(minU / textureWidth + f2, minV / textureHeight + f3);
        vertexPositions[2] = vertexPositions[2].setTexturePosition(minU / textureWidth + f2, maxV / textureHeight - f3);
        vertexPositions[3] = vertexPositions[3].setTexturePosition(maxU / textureWidth - f2, maxV / textureHeight - f3);
    }

}
