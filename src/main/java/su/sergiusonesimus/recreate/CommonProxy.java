package su.sergiusonesimus.recreate;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import su.sergiusonesimus.metaworlds.world.SubWorldServer;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.Contraption;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.ContraptionWorld;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.ContraptionWorldServer;

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

    public World createContraptionWorld(World parentWorld, int newSubWorldID, Contraption contraption) {
        World subWorld = null;
        if (!parentWorld.isRemote) {
            SubWorldServer.global_newSubWorldID = newSubWorldID;
            subWorld = new ContraptionWorldServer(
                (WorldServer) parentWorld,
                newSubWorldID,
                ((WorldServer) parentWorld).func_73046_m(),
                parentWorld.getSaveHandler(),
                parentWorld.getWorldInfo()
                    .getWorldName(),
                parentWorld.provider.dimensionId,
                new WorldSettings(
                    0L,
                    parentWorld.getWorldInfo()
                        .getGameType(),
                    false,
                    parentWorld.getWorldInfo()
                        .isHardcoreModeEnabled(),
                    parentWorld.getWorldInfo()
                        .getTerrainType()),
                parentWorld.theProfiler,
                contraption);
        }
        if (contraption != null) ((ContraptionWorld) subWorld).setSubWorldType(contraption.getSubWorldType());
        return subWorld;
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

    public int getSplitShaftBlockRenderID() {
        return 0;
    }

    public int getGearboxBlockRenderID() {
        return 0;
    }

    public int getWaterWheelBlockRenderID() {
        return 0;
    }

    public int getSailBlockRenderID() {
        return 0;
    }

    public int getMechanicalPistonBlockRenderID() {
        return 0;
    }

    public int getMechanicalPistonHeadBlockRenderID() {
        return 0;
    }

    public int getPistonExtensionPoleBlockRenderID() {
        return 0;
    }

    public int getLinearChassisBlockRenderID() {
        return 0;
    }

    public int registerArmorRenderID(String prefix) {
        return 0;
    }

    public ModelBiped getGogglesArmorModel() {
        return null;
    }
}
