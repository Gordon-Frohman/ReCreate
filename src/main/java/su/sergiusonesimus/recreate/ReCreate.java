package su.sergiusonesimus.recreate;

import java.io.File;
import java.util.Random;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.RotationHelper;
import su.sergiusonesimus.recreate.compat.tebreaker.TileEntityBreakerIntegration;
import su.sergiusonesimus.recreate.content.contraptions.TorquePropagator;
import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.AllSubWorldTypes;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue.SuperGlueHandler;
import su.sergiusonesimus.recreate.content.contraptions.components.waterwheel.WaterWheelTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.ClutchTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.GearshiftTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.gearbox.GearboxTileEntity;
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
    public static final Random RANDOM = new Random();

    public static final ReCreateRegistrate REGISTRATE = new ReCreateRegistrate();

    public static int idEntitySuperGlue;

    public static boolean isTileEntityBreakerLoaded = false;

    @Instance(ID)
    public static ReCreate instance;

    @SidedProxy(
        clientSide = "su.sergiusonesimus.recreate.ClientProxy",
        serverSide = "su.sergiusonesimus.recreate.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);

        AllPackets.registerPackets();

        // initialize & register blocks
        AllBlocks.registerBlocks();

        // items
        AllItems.registerItems();

        AllTags.register();

        // Reading config file after registering blocks, because it needs a generated default stress list
        AllConfigs.init(new File(event.getModConfigurationDirectory(), "ReCreate.cfg"));
        idEntitySuperGlue = 500;

        AllSubWorldTypes.register();

        // check if various integrations are required
        isTileEntityBreakerLoaded = Loader.isModLoaded("tebreaker");
    }

    @EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {

        // event listeners
        final ClientEvents clientEvents = new ClientEvents();
        MinecraftForge.EVENT_BUS.register(clientEvents);
        FMLCommonHandler.instance()
            .bus()
            .register(clientEvents);

        final CommonEvents commonEvents = new CommonEvents();
        MinecraftForge.EVENT_BUS.register(commonEvents);
        FMLCommonHandler.instance()
            .bus()
            .register(commonEvents);

        final SuperGlueHandler superGlueHandler = new SuperGlueHandler();
        MinecraftForge.EVENT_BUS.register(superGlueHandler);
        FMLCommonHandler.instance()
            .bus()
            .register(superGlueHandler);

        // entities
        registerEntities();

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

        if (isTileEntityBreakerLoaded) TileEntityBreakerIntegration.registerTileEntities();

        registerRotators();
    }

    @EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    private void registerEntities() {
        EntityRegistry
            .registerModEntity(SuperGlueEntity.class, "Super Glue", idEntitySuperGlue, instance, 64, 1, false);
    }

    private void registerTileEntities() {
        GameRegistry.registerTileEntity(ShaftTileEntity.class, "Shaft");
        GameRegistry.registerTileEntity(CreativeMotorTileEntity.class, "Creative Motor");
        GameRegistry.registerTileEntity(CogWheelTileEntity.class, "Cogwheel");
        GameRegistry.registerTileEntity(MechanicalBearingTileEntity.class, "Mechanical Bearing");
        GameRegistry.registerTileEntity(GearshiftTileEntity.class, "Gearshift");
        GameRegistry.registerTileEntity(ClutchTileEntity.class, "Clutch");
        GameRegistry.registerTileEntity(GearboxTileEntity.class, "Gearbox");
        GameRegistry.registerTileEntity(WaterWheelTileEntity.class, "Water Wheel");
    }

    private void registerRotators() {
        // Directional kinetic blocks
        RotationHelper.registerBlocks("3d_directional", AllBlocks.creative_motor, AllBlocks.mechanical_bearing);

        // Rotated pillar kinetic blocks
        RotationHelper.registerBlockRotator("rotated_pillar", (meta) -> {
            Vec3 dir = Vec3.createVectorHelper(0, 0, 0);
            switch (meta % 3) {
                default:
                case 0:
                    dir.yCoord = 1;
                    break;
                case 1:
                    dir.xCoord = 1;
                    break;
                case 2:
                    dir.zCoord = 1;
                    break;
            }
            return dir;
        }, (originalMeta, vec) -> {
            if (vec != null) {
                Direction dir = Direction.getNearest(vec);
                switch (dir.getAxis()) {
                    default:
                    case Y:
                        return 0;
                    case X:
                        return 1;
                    case Z:
                        return 2;
                }
            }
            return originalMeta;
        });
        RotationHelper.registerBlocks("rotated_pillar", AllBlocks.shaft, AllBlocks.cogwheel, AllBlocks.large_cogwheel);
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(ID, path);
    }
}
