package su.sergiusonesimus.recreate;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import su.sergiusonesimus.recreate.content.contraptions.TorquePropagator;
import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftTileEntity;
import su.sergiusonesimus.recreate.events.ClientEvents;
import su.sergiusonesimus.recreate.events.CommonEvents;
import su.sergiusonesimus.recreate.foundation.config.AllConfigs;
import su.sergiusonesimus.recreate.foundation.data.ReCreateRegistrate;
import su.sergiusonesimus.recreate.foundation.networking.AllPackets;

@Mod(modid = Tags.MODID, version = ReCreate.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]")
public class ReCreate {

	public static final String ID = Tags.MODID;
	public static final String NAME = Tags.MODNAME;
	public static final String VERSION = "0.1";

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    public static final TorquePropagator TORQUE_PROPAGATOR = new TorquePropagator();

    public static final ReCreateRegistrate REGISTRATE = new ReCreateRegistrate();

    @SidedProxy(
        clientSide = "su.sergiusonesimus.recreate.ClientProxy",
        serverSide = "su.sergiusonesimus.recreate.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        AllConfigs.init(new File(event.getModConfigurationDirectory(), "ReCreate.cfg"));
        proxy.preInit(event);

        AllPackets.registerPackets();

        // initialize & register blocks
        AllBlocks.registerBlocks();
    }

    @EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {

        // event listeners
        final ClientEvents clientEvents = new ClientEvents();
        MinecraftForge.EVENT_BUS.register(clientEvents);
        FMLCommonHandler.instance().bus().register(clientEvents);
        
        final CommonEvents commonEvents = new CommonEvents();
        MinecraftForge.EVENT_BUS.register(commonEvents);
        FMLCommonHandler.instance().bus().register(commonEvents);

        // tile entities
        registerTileEntities();
        
        proxy.init(event);

        // render and other client stuff
        proxy.doOnLoadRegistration();
    }

    @EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    private void registerTileEntities() {
    	GameRegistry.registerTileEntity(ShaftTileEntity.class, "Shaft");
    	GameRegistry.registerTileEntity(CreativeMotorTileEntity.class, "Creative Motor");
    	GameRegistry.registerTileEntity(CogWheelTileEntity.class, "Cogwheel");
    }

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}
}
