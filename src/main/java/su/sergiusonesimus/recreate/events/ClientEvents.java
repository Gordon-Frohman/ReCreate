package su.sergiusonesimus.recreate.events;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import su.sergiusonesimus.recreate.ClientProxy;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.KineticDebugger;
import su.sergiusonesimus.recreate.content.contraptions.base.IRotate;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis.ChassisRangeDisplay;
import su.sergiusonesimus.recreate.content.contraptions.goggles.GoggleOverlayRenderer;
import su.sergiusonesimus.recreate.foundation.config.AllConfigs;
import su.sergiusonesimus.recreate.foundation.item.ItemDescription;
import su.sergiusonesimus.recreate.foundation.item.TooltipHelper;
import su.sergiusonesimus.recreate.foundation.sound.SoundScapes;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.scrollvalue.ScrollValueHandler;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.scrollvalue.ScrollValueRenderer;
import su.sergiusonesimus.recreate.foundation.utility.ServerSpeedProvider;
import su.sergiusonesimus.recreate.foundation.utility.placement.PlacementHelpers;
import su.sergiusonesimus.recreate.util.AnimationTickHolder;

public class ClientEvents {

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (!isGameActive()) return;

        // TODO
        // World world = Minecraft.getMinecraft().theWorld;
        // if (event.phase == Phase.START) {
        // LinkedControllerClientHandler.tick();
        // AirCurrent.tickClientPlayerSounds();
        // return;
        // }
        //
        SoundScapes.tick();
        AnimationTickHolder.tick();
        ScrollValueHandler.tick();

        // ClientProxy.SCHEMATIC_SENDER.tick();
        // ClientProxy.SCHEMATIC_AND_QUILL_HANDLER.tick();
        // ClientProxy.SCHEMATIC_HANDLER.tick();
        // ClientProxy.ZAPPER_RENDER_HANDLER.tick();
        // ClientProxy.POTATO_CANNON_RENDER_HANDLER.tick();
        // ClientProxy.SOUL_PULSE_EFFECT_HANDLER.tick(world);
        //
        // CapabilityMinecartController.tick(world);
        // CouplingPhysics.tick(world);
        //
        // PonderTooltipHandler.tick();
        ServerSpeedProvider.clientTick();
        // BeltConnectorHandler.tick();
        // FilteringRenderer.tick();
        // LinkRenderer.tick();
        ScrollValueRenderer.tick();
        ChassisRangeDisplay.tick();
        // EdgeInteractionRenderer.tick();
        // WorldshaperRenderHandler.tick();
        // CouplingHandlerClient.tick();
        // CouplingRenderer.tickDebugModeRenders();
        KineticDebugger.tick();
        // ExtendoGripRenderHandler.tick();
        // ArmInteractionPointHandler.tick();
        // EjectorTargetHandler.tick();
        PlacementHelpers.tick();
        ClientProxy.OUTLINER.tickOutlines();
        ClientProxy.GHOST_BLOCKS.tickGhosts();
        // BlueprintOverlayRenderer.tick();
        // ToolboxHandlerClient.clientTick();
    }

    // TODO
    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityPlayerSP) ClientProxy.checkGraphicsFanciness((EntityPlayer) event.entity);
    }

    // TODO
    @SubscribeEvent
    public void onLoadWorld(WorldEvent.Load event) {
        World world = event.world;
        if (world.isRemote && world instanceof WorldClient /* TODO && !(world instanceof WrappedClientWorld) */) {
            ClientProxy.invalidateRenderers();
            AnimationTickHolder.reset();
        }
    }

    @SubscribeEvent
    public void onUnloadWorld(WorldEvent.Unload event) {
        if (event.world.isRemote) {
            ClientProxy.invalidateRenderers();
            // TODO
            // ClientProxy.SOUL_PULSE_EFFECT_HANDLER.refresh();
            AnimationTickHolder.reset();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        EntityLivingBase camera = Minecraft.getMinecraft().renderViewEntity;

        GL11.glPushMatrix();
        GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);

        // TODO
        // CouplingRenderer.renderAll();
        // ClientProxy.SCHEMATIC_HANDLER.render();
        ClientProxy.GHOST_BLOCKS.renderAll();

        ClientProxy.OUTLINER.renderOutlines(event.partialTicks);
        GL11.glEnable(GL11.GL_CULL_FACE);

        GL11.glPopMatrix();
    }

    // TODO
    // @SubscribeEvent
    // public void getItemTooltipColor(RenderTooltipEvent.Color event) {
    // PonderTooltipHandler.handleTooltipColor(event);
    // }

    @SuppressWarnings("static-access")
    @SubscribeEvent
    public void addToItemTooltip(ItemTooltipEvent event) {
        if (!AllConfigs.CLIENT.tooltips) return;
        if (event.entityPlayer == null) return;

        ItemStack stack = event.itemStack;

        if (GameRegistry.findUniqueIdentifierFor(stack.getItem()).modId.equals(ReCreate.ID))
            if (TooltipHelper.hasTooltip(stack, event.entityPlayer)) {
                List<String> itemTooltips = event.toolTip;
                List<IChatComponent> toolTips = new ArrayList<>();
                toolTips.add(new ChatComponentText(itemTooltips.remove(0)));
                TooltipHelper.getTooltip(stack)
                    .addInformation(toolTips);
                for (IChatComponent tooltip : toolTips) {
                    itemTooltips.add(tooltip.getFormattedText());
                }
            }

        if (stack.getItem() instanceof ItemBlock) {
            ItemBlock item = (ItemBlock) stack.getItem();
            if (item.field_150939_a instanceof IRotate/* TODO || item.field_150939_a instanceof EngineBlock */) {
                List<IChatComponent> kineticStats = ItemDescription.getKineticStats(item.field_150939_a);
                if (!kineticStats.isEmpty()) {
                    event.toolTip.add("");
                    for (IChatComponent kineticStat : kineticStats) {
                        event.toolTip.add(kineticStat.getFormattedText());
                    }
                }
            }
        }

        // TODO
        // PonderTooltipHandler.addToTooltip(event.getToolTip(), stack);
        // SequencedAssemblyRecipe.addToTooltip(event.getToolTip(), stack);
    }

    // TODO
    // @SubscribeEvent
    // public void onRenderTick(RenderTickEvent event) {
    // if (!isGameActive())
    // return;
    // TurntableHandler.gameRenderTick();
    // }

    protected boolean isGameActive() {
        return !(Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null);
    }

    // TODO
    // @SubscribeEvent
    // public void getFogDensity(EntityViewRenderEvent.FogDensity event) {
    // Camera info = event.getInfo();
    // World level = Minecraft.getMinecraft().level;
    // BlockPos blockPos = info.getBlockPosition();
    // FluidState fluidstate = level.getFluidState(blockPos);
    // if (info.getPosition().y > blockPos.getY() + fluidstate.getHeight(level, blockPos))
    // return;
    //
    // Fluid fluid = fluidstate.getType();
    //
    // if (fluid.isSame(AllFluids.CHOCOLATE.get())) {
    // event.setDensity(5f);
    // event.setCanceled(true);
    // return;
    // }
    //
    // if (fluid.isSame(AllFluids.HONEY.get())) {
    // event.setDensity(1.5f);
    // event.setCanceled(true);
    // return;
    // }
    //
    // if (FluidHelper.isWater(fluid) && AllItems.DIVING_HELMET.get()
    // .isWornBy(Minecraft.getMinecraft().cameraEntity)) {
    // event.setDensity(300f);
    // event.setCanceled(true);
    // return;
    // }
    // }

    // TODO
    // @SubscribeEvent
    // public void getFogColor(EntityViewRenderEvent.FogColors event) {
    // Camera info = event.getInfo();
    // World level = Minecraft.getMinecraft().theWorld;
    // BlockPos blockPos = info.getBlockPosition();
    // FluidState fluidstate = level.getFluidState(blockPos);
    // if (info.getPosition().y > blockPos.getY() + fluidstate.getHeight(level, blockPos))
    // return;
    //
    // Fluid fluid = fluidstate.getType();
    //
    // if (fluid.isSame(AllFluids.CHOCOLATE.get())) {
    // event.setRed(98 / 256f);
    // event.setGreen(32 / 256f);
    // event.setBlue(32 / 256f);
    // }
    //
    // if (fluid.isSame(AllFluids.HONEY.get())) {
    // event.setRed(234 / 256f);
    // event.setGreen(174 / 256f);
    // event.setBlue(47 / 256f);
    // }
    // }

    // TODO
    // @SubscribeEvent
    // public void leftClickEmpty(PlayerInteractEvent event) {
    // if(event.action != Action.LEFT_CLICK_BLOCK) return;
    // ItemStack stack = event.entityPlayer.getHeldItem();
    // if (stack.getItem() instanceof ZapperItem) {
    // AllPackets.channel.sendToServer(new LeftClickPacket());
    // }
    // }

    // TODO
    // @EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    // public static class ModBusEvents {
    //
    // @SubscribeEvent
    // public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
    // event.registerReloadListener(ClientProxy.RESOURCE_RELOAD_LISTENER);
    // }
    //
    // @SubscribeEvent
    // public static void addEntityRendererLayers(EntityRenderersEvent.AddLayers event) {
    // EntityRenderer dispatcher = Minecraft.getMinecraft().entityRenderer;
    // CopperBacktankArmorLayer.registerOnAll(dispatcher);
    // }
    //
    // @SubscribeEvent
    // public static void loadCompleted(FMLLoadCompleteEvent event) {
    // ModContainer createContainer = ModList.get()
    // .getModContainerById(ReCreate.ID)
    // .orElseThrow(() -> new IllegalStateException("Create Mod Container missing after loadCompleted"));
    // createContainer.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
    // () -> new ConfigGuiHandler.ConfigGuiFactory((mc, previousScreen) -> BaseConfigScreen.forCreate(previousScreen)));
    // }
    //
    // }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        // TODO
        GuiIngameForge gui = (GuiIngameForge) Minecraft.getMinecraft().ingameGUI;
        float partialTicks = event.partialTicks;
        int width = event.resolution.getScaledWidth();
        int height = event.resolution.getScaledHeight();
        if (event.type == ElementType.AIR) {
            // OverlayRegistry.registerOverlayAbove(ForgeIngameGui.AIR_LEVEL_ELEMENT, "Create's Remaining Air",
            // CopperBacktankArmorLayer.REMAINING_AIR_OVERLAY);
        }
        if (event.type == ElementType.HOTBAR) {
            // OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "Create's Toolboxes",
            // ToolboxHandlerClient.OVERLAY);
            GoggleOverlayRenderer.renderOverlay(gui, partialTicks, width, height);
            // OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "Create's Blueprints",
            // BlueprintOverlayRenderer.OVERLAY);
            // OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "Create's Linked Controller",
            // LinkedControllerClientHandler.OVERLAY);
            // OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "Create's Schematics",
            // SCHEMATIC_HANDLER.getOverlayRenderer());
        }
    }

}
