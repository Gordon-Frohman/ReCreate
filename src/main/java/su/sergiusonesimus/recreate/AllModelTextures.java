package su.sergiusonesimus.recreate;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public enum AllModelTextures {

    SHAFT("axis.png"),
    CREATIVE_MOTOR("creative_motor.png"),
    COGWHEEL("cogwheel.png"),
    LARGE_COGWHEEL("large_cogwheel.png"),
    MECHANICAL_BEARING("mechanical_bearing.png"),
    GEARSHIFT("gearshift.png"),
    LIT_GEARSHIFT("lit_gearshift.png"),
    CLUTCH("clutch.png"),
    LIT_CLUTCH("lit_clutch.png"),
    GEARBOX("gearbox.png"),
    WATER_WHEEL("water_wheel.png"),

    WRENCH("wrench.png"),

    ;

    public static final String ASSET_PATH = "textures/models/";
    private ResourceLocation location;

    private AllModelTextures(String filename) {
        location = ReCreate.asResource(ASSET_PATH + filename);
    }

    public void bind() {
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
    }

    public ResourceLocation getLocation() {
        return location;
    }

}
