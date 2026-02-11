package su.sergiusonesimus.recreate;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import su.sergiusonesimus.recreate.compat.tebreaker.TileEntityBreakerIntegration;
import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorRenderBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.Contraption;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.ContraptionWorld;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.ContraptionWorldClient;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.BearingRenderBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.BearingTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.SailRenderBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue.SuperGlueRenderer;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonHeadRenderBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonRenderBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.PistonExtensionPoleRenderBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.waterwheel.WaterWheelRenderBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.waterwheel.WaterWheelTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.waterwheel.WaterWheelTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.goggles.GogglesModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelRenderBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftRenderBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.*;
import su.sergiusonesimus.recreate.content.contraptions.relays.gearbox.GearboxRenderBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.gearbox.GearboxTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.gearbox.GearboxTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.wrench.WrenchRenderItem;
import su.sergiusonesimus.recreate.events.ClientEvents;
import su.sergiusonesimus.recreate.events.InputEvents;
import su.sergiusonesimus.recreate.foundation.utility.ghost.GhostBlocks;
import su.sergiusonesimus.recreate.foundation.utility.outliner.Outliner;
import team.chisel.ctmlib.CTMRenderer;

public class ClientProxy extends CommonProxy {

    int shaftRenderID;
    int creativeMotorRenderID;
    int cogwheelRenderID;
    int bearingRenderID;
    int splitShaftRenderID;
    int gearboxRenderID;
    int waterWheelRenderID;
    int sailRenderID;
    int mechanicalPistonRenderID;
    int mechanicalPistonHeadRenderID;
    int pistonExtensionPoleRenderID;
    int linearChassisRenderID;

    ModelBiped gogglesArmorModel;

    // TODO
    public static final Outliner OUTLINER = new Outliner();
    public static final GhostBlocks GHOST_BLOCKS = new GhostBlocks();

    public void preInit(FMLPreInitializationEvent event) {

        AllKeys.register();

    }

    public void init(FMLInitializationEvent event) {
        final ClientEvents clientEvents = new ClientEvents();
        MinecraftForge.EVENT_BUS.register(clientEvents);
        FMLCommonHandler.instance()
            .bus()
            .register(clientEvents);
    }

    public void postInit(FMLPostInitializationEvent event) {
        if (ReCreate.isTileEntityBreakerLoaded) TileEntityBreakerIntegration.registerTileEntities();
    }

    @Override
    public void doOnLoadRegistration() {
        InputEvents inputEvents = new InputEvents();
        FMLCommonHandler.instance()
            .bus()
            .register(inputEvents);
        MinecraftForge.EVENT_BUS.register(inputEvents);

        // items
        MinecraftForgeClient.registerItemRenderer(AllItems.wrench, new WrenchRenderItem());

        // entities
        RenderingRegistry.registerEntityRenderingHandler(SuperGlueEntity.class, new SuperGlueRenderer());

        // tile entities
        ClientRegistry.bindTileEntitySpecialRenderer(ShaftTileEntity.class, new ShaftTileEntityRenderer());
        ClientRegistry
            .bindTileEntitySpecialRenderer(CreativeMotorTileEntity.class, new CreativeMotorTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(CogWheelTileEntity.class, new CogWheelTileEntityRenderer());
        ClientRegistry
            .bindTileEntitySpecialRenderer(MechanicalBearingTileEntity.class, new BearingTileEntityRenderer());
        SplitShaftTileEntityRenderer sster = new SplitShaftTileEntityRenderer();
        ClientRegistry.bindTileEntitySpecialRenderer(GearshiftTileEntity.class, sster);
        ClientRegistry.bindTileEntitySpecialRenderer(ClutchTileEntity.class, sster);
        ClientRegistry.bindTileEntitySpecialRenderer(GearboxTileEntity.class, new GearboxTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(WaterWheelTileEntity.class, new WaterWheelTileEntityRenderer());
        ClientRegistry
            .bindTileEntitySpecialRenderer(MechanicalPistonTileEntity.class, new MechanicalPistonTileEntityRenderer());

        // block render ids
        shaftRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new ShaftRenderBlock(shaftRenderID));
        creativeMotorRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new CreativeMotorRenderBlock(creativeMotorRenderID));
        cogwheelRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new CogWheelRenderBlock(cogwheelRenderID));
        bearingRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new BearingRenderBlock(bearingRenderID));
        splitShaftRenderID = RenderingRegistry.getNextAvailableRenderId();
        SplitShaftRenderBlock ssrb = new SplitShaftRenderBlock(splitShaftRenderID);
        RenderingRegistry.registerBlockHandler(ssrb);
        RenderingRegistry.registerBlockHandler(ssrb);
        gearboxRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new GearboxRenderBlock(gearboxRenderID));
        waterWheelRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new WaterWheelRenderBlock(waterWheelRenderID));
        sailRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new SailRenderBlock(sailRenderID));
        mechanicalPistonRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new MechanicalPistonRenderBlock(mechanicalPistonRenderID));
        mechanicalPistonHeadRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new MechanicalPistonHeadRenderBlock(mechanicalPistonHeadRenderID));
        pistonExtensionPoleRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new PistonExtensionPoleRenderBlock(pistonExtensionPoleRenderID));
        linearChassisRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new CTMRenderer(linearChassisRenderID));

        // armor model
        gogglesArmorModel = new GogglesModel();
    }

    public static void invalidateRenderers() {
        // TODO
        // BUFFER_CACHE.invalidate();
        //
        // ContraptionRenderDispatcher.reset();
    }

    public static void checkGraphicsFanciness(EntityPlayer player) {
        // TODO This method is probably useless, since there're no fabulous settings on 1.7.10
        // if (!Minecraft.isFancyGraphicsEnabled()) return;
        //
        // if (AllConfigs.CLIENT.ignoreFabulousWarning) return;
        //
        // IChatComponent text = new ChatComponentText("[WARN]");
        // text.getChatStyle()
        // .setColor(EnumChatFormatting.GOLD);
        // text.appendSibling(
        // new ChatComponentText(
        // " Some of Create's visual features will not be available while Fabulous graphics are enabled!"));
        // text.getChatStyle()
        // .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/create dismissFabulousWarning"))
        // .setChatHoverEvent(
        // new HoverEvent(
        // HoverEvent.Action.SHOW_TEXT,
        // new ChatComponentText("Click here to disable this warning")));
        //
        // player.addChatMessage(text);
    }

    public World createContraptionWorld(World parentWorld, int newSubWorldID, Contraption contraption) {
        World subWorld = null;
        if (parentWorld.isRemote) {
            subWorld = new ContraptionWorldClient(
                (WorldClient) parentWorld,
                newSubWorldID,
                ((WorldClient) parentWorld).sendQueue,
                new WorldSettings(
                    0L,
                    parentWorld.getWorldInfo()
                        .getGameType(),
                    false,
                    parentWorld.getWorldInfo()
                        .isHardcoreModeEnabled(),
                    parentWorld.getWorldInfo()
                        .getTerrainType()),
                ((WorldClient) parentWorld).provider.dimensionId,
                parentWorld.difficultySetting,
                parentWorld.theProfiler,
                contraption);
        } else {
            subWorld = super.createContraptionWorld(parentWorld, newSubWorldID, contraption);
        }
        if (contraption != null) ((ContraptionWorld) subWorld).setSubWorldType(contraption.getSubWorldType());
        return subWorld;
    }

    public int getShaftBlockRenderID() {
        return shaftRenderID;
    }

    public int getCreativeMotorBlockRenderID() {
        return creativeMotorRenderID;
    }

    public int getCogWheelBlockRenderID() {
        return cogwheelRenderID;
    }

    public int getBearingBlockRenderID() {
        return bearingRenderID;
    }

    public int getSplitShaftBlockRenderID() {
        return splitShaftRenderID;
    }

    public int getGearboxBlockRenderID() {
        return gearboxRenderID;
    }

    public int getWaterWheelBlockRenderID() {
        return waterWheelRenderID;
    }

    public int getSailBlockRenderID() {
        return sailRenderID;
    }

    public int getMechanicalPistonBlockRenderID() {
        return mechanicalPistonRenderID;
    }

    public int getMechanicalPistonHeadBlockRenderID() {
        return mechanicalPistonHeadRenderID;
    }

    public int getPistonExtensionPoleBlockRenderID() {
        return pistonExtensionPoleRenderID;
    }

    public int getLinearChassisBlockRenderID() {
        return linearChassisRenderID;
    }

    public ModelBiped getGogglesArmorModel() {
        return gogglesArmorModel;
    }

    public int registerArmorRenderID(String prefix) {
        return RenderingRegistry.addNewArmourRendererPrefix(prefix);
    }

}
