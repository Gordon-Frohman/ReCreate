package su.sergiusonesimus.recreate.foundation.networking;

import net.minecraft.world.World;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.ContraptionStallPacket;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.ContraptionWorldCreatePacket;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.ContraptionWorldUpdatePacket;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue.GlueEffectPacket;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.filtering.FilteringCountUpdatePacket;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.scrollvalue.ScrollValueUpdatePacket;
import su.sergiusonesimus.recreate.foundation.utility.ServerSpeedProvider;

@SuppressWarnings("unchecked")
public enum AllPackets {

    // Client to Server
    // TODO
    // CONFIGURE_SCHEMATICANNON(ConfigureSchematicannonPacket.class, ConfigureSchematicannonPacket.Handler.class,
    // Side.SERVER),
    // CONFIGURE_STOCKSWITCH(ConfigureStockswitchPacket.class, ConfigureStockswitchPacket.Handler.class, Side.SERVER),
    // CONFIGURE_SEQUENCER(ConfigureSequencedGearshiftPacket.class, ConfigureSequencedGearshiftPacket.Handler.class,
    // Side.SERVER),
    // PLACE_SCHEMATIC(SchematicPlacePacket.class, SchematicPlacePacket.Handler.class, Side.SERVER),
    // UPLOAD_SCHEMATIC(SchematicUploadPacket.class, SchematicUploadPacket.Handler.class, Side.SERVER),
    CONFIGURE_FILTERING_AMOUNT(FilteringCountUpdatePacket.class, FilteringCountUpdatePacket.Handler.class, Side.SERVER),
    CONFIGURE_SCROLLABLE(ScrollValueUpdatePacket.class, ScrollValueUpdatePacket.Handler.class, Side.SERVER),
    // TODO
    // EXTENDO_INTERACT(ExtendoGripInteractionPacket.class, ExtendoGripInteractionPacket.Handler.class, Side.SERVER),
    // CONTRAPTION_INTERACT(ContraptionInteractionPacket.class, ContraptionInteractionPacket.Handler.class,
    // Side.SERVER),
    // CLIENT_MOTION(ClientMotionPacket.class, ClientMotionPacket.Handler.class, Side.SERVER),
    // PLACE_ARM(ArmPlacementPacket.class, ArmPlacementPacket.Handler.class, Side.SERVER),
    // MINECART_COUPLING_CREATION(CouplingCreationPacket.class, CouplingCreationPacket.Handler.class, Side.SERVER),
    // INSTANT_SCHEMATIC(InstantSchematicPacket.class, InstantSchematicPacket.Handler.class, Side.SERVER),
    // SYNC_SCHEMATIC(SchematicSyncPacket.class, SchematicSyncPacket.Handler.class, Side.SERVER),
    // LEFT_CLICK(LeftClickPacket.class, LeftClickPacket.Handler.class, Side.SERVER),
    // PLACE_EJECTOR(EjectorPlacementPacket.class, EjectorPlacementPacket.Handler.class, Side.SERVER),
    // TRIGGER_EJECTOR(EjectorTriggerPacket.class, EjectorTriggerPacket.Handler.class, Side.SERVER),
    // EJECTOR_ELYTRA(EjectorElytraPacket.class, EjectorElytraPacket.Handler.class, Side.SERVER),
    // LINKED_CONTROLLER_INPUT(LinkedControllerInputPacket.class, LinkedControllerInputPacket.Handler.class,
    // Side.SERVER),
    // LINKED_CONTROLLER_BIND(LinkedControllerBindPacket.class, LinkedControllerBindPacket.Handler.class, Side.SERVER),
    // LINKED_CONTROLLER_USE_LECTERN(LinkedControllerStopLecternPacket.class,
    // LinkedControllerStopLecternPacket.Handler.class, Side.SERVER),
    // C_CONFIGURE_CONFIG(CConfigureConfigPacket.class, CConfigureConfigPacket.Handler.class, Side.SERVER),
    // SUBMIT_GHOST_ITEM(GhostItemSubmitPacket.class, GhostItemSubmitPacket.Handler.class, Side.SERVER),
    // BLUEPRINT_COMPLETE_RECIPE(BlueprintAssignCompleteRecipePacket.class,
    // BlueprintAssignCompleteRecipePacket.Handler.class, Side.SERVER),
    // CONFIGURE_SYMMETRY_WAND(ConfigureSymmetryWandPacket.class, ConfigureSymmetryWandPacket.Handler.class,
    // Side.SERVER),
    // CONFIGURE_WORLDSHAPER(ConfigureWorldshaperPacket.class, ConfigureWorldshaperPacket.Handler.class, Side.SERVER),
    // TOOLBOX_EQUIP(ToolboxEquipPacket.class, ToolboxEquipPacket.Handler.class, Side.SERVER),
    // TOOLBOX_DISPOSE_ALL(ToolboxDisposeAllPacket.class, ToolboxDisposeAllPacket.Handler.class, Side.SERVER),

    // Server to Client
    // TODO
    // SYMMETRY_EFFECT(SymmetryEffectPacket.class, SymmetryEffectPacket.Handler.class, Side.CLIENT),
    SERVER_SPEED(ServerSpeedProvider.Packet.class, ServerSpeedProvider.Packet.Handler.class, Side.CLIENT),
    // TODO
    // BEAM_EFFECT(ZapperBeamPacket.class, ZapperBeamPacket.Handler.class, Side.CLIENT),
    // S_CONFIGURE_CONFIG(SConfigureConfigPacket.class, SConfigureConfigPacket.Handler.class, Side.CLIENT),
    CONTRAPTION_STALL(ContraptionStallPacket.class, ContraptionStallPacket.Handler.class, Side.CLIENT),
    // CONTRAPTION_DISASSEMBLE(ContraptionDisassemblyPacket.class, ContraptionDisassemblyPacket.Handler.class,
    // Side.CLIENT),
    GLUE_EFFECT(GlueEffectPacket.class, GlueEffectPacket.Handler.class, Side.CLIENT),
    // TODO
    // CONTRAPTION_SEAT_MAPPING(ContraptionSeatMappingPacket.class, ContraptionSeatMappingPacket.Handler.class,
    // Side.CLIENT),
    // LIMBSWING_UPDATE(LimbSwingUpdatePacket.class, LimbSwingUpdatePacket.Handler.class, Side.CLIENT),
    // MINECART_CONTROLLER(MinecartControllerUpdatePacket.class, MinecartControllerUpdatePacket.Handler.class,
    // Side.CLIENT),
    // FLUID_SPLASH(FluidSplashPacket.class, FluidSplashPacket.Handler.class, Side.CLIENT),
    // CONTRAPTION_FLUID(ContraptionFluidPacket.class, ContraptionFluidPacket.Handler.class, Side.CLIENT),
    // GANTRY_UPDATE(GantryContraptionUpdatePacket.class, GantryContraptionUpdatePacket.Handler.class, Side.CLIENT),
    // BLOCK_HIGHLIGHT(HighlightPacket.class, HighlightPacket.Handler.class, Side.CLIENT),
    // TODO
    // TUNNEL_FLAP(TunnelFlapPacket.class, TunnelFlapPacket.Handler.class, Side.CLIENT),
    // TODO
    // FUNNEL_FLAP(FunnelFlapPacket.class, FunnelFlapPacket.Handler.class, Side.CLIENT),
    // POTATO_CANNON(PotatoCannonPacket.class, PotatoCannonPacket.Handler.class, Side.CLIENT),
    // SOUL_PULSE(SoulPulseEffectPacket.class, SoulPulseEffectPacket.Handler.class, Side.CLIENT),
    // PERSISTENT_DATA(ISyncPersistentData.PersistentDataPacket.class,
    // ISyncPersistentData.PersistentDataPacket.Handler.class, Side.CLIENT),
    // SYNC_POTATO_PROJECTILE_TYPES(PotatoProjectileTypeManager.SyncPacket.class,
    // PotatoProjectileTypeManager.SyncPacket.Handler.class, Side.CLIENT),

    ;

    public static final String CHANNEL_NAME = ReCreate.asResource("main")
        .toString();
    public static final int NETWORK_VERSION = 1;
    public static final String NETWORK_VERSION_STR = String.valueOf(NETWORK_VERSION);
    public static SimpleNetworkWrapper channel;

    private LoadedPacket<?, ?> packet;

    <T extends IMessage, H extends IMessageHandler<T, IMessage>> AllPackets(Class<T> type, Class<H> handler,
        Side direction) {
        packet = new LoadedPacket<T, H>(handler, type, direction);
    }

    public static void registerPackets() {
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);
        for (AllPackets packet : values()) packet.packet.register();

        // MetaWorlds packets
        MetaMagicNetwork.registerPacket(
            ContraptionWorldCreatePacket.Handler.class,
            ContraptionWorldCreatePacket.class,
            Side.CLIENT);
        MetaMagicNetwork.registerPacket(
            ContraptionWorldUpdatePacket.Handler.class,
            ContraptionWorldUpdatePacket.class,
            Side.CLIENT);
    }

    public static void sendToNear(World world, int x, int y, int z, int range, IMessage message) {
        channel
            .sendToAllAround(message, new TargetPoint(world.provider.dimensionId, x + 0.5D, y + 0.5D, z + 0.5D, range));
    }

    private static class LoadedPacket<T extends IMessage, H extends IMessageHandler<T, IMessage>> {

        private static int index = 0;

        private Class<H> handler;
        private Class<T> type;
        private Side direction;

        private LoadedPacket(Class<H> handler, Class<T> type, Side direction) {
            this.handler = handler;
            this.type = type;
            this.direction = direction;
        }

        private void register() {
            channel.registerMessage(handler, type, index++, direction);
        }
    }

}
