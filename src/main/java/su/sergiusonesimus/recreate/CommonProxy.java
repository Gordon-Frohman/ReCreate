package su.sergiusonesimus.recreate;

import net.minecraft.client.model.ModelBiped;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {

    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {}

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}

    public void doOnLoadRegistration() {

    }

    public int getShaftBlockRenderID() {
        return 0;
    }

    public int getCreativeMotorBlockRenderID() {
        return 0;
    }

    public int getCogWheelBlockRenderID() {
        return 0;
    }

    public int getBearingBlockRenderID() {
        return 0;
    }

    public int getGearshiftBlockRenderID() {
        return 0;
    }

    public int getClutchBlockRenderID() {
        return 0;
    }

    public int getGearboxBlockRenderID() {
        return 0;
    }

    public int getWaterWheelBlockRenderID() {
        return 0;
    }

    public int registerArmorRenderID(String prefix) {
        return 0;
    }

    public ModelBiped getGogglesArmorModel() {
        return null;
    }
}
