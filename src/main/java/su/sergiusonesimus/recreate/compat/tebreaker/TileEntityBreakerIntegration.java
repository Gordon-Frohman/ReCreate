package su.sergiusonesimus.recreate.compat.tebreaker;

import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorModel;
import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.BearingModel;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.LargeCogWheelModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.SplitShaftTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.gearbox.GearboxTileEntity;
import su.sergiusonesimus.tebreaker.TileEntityBreaker;
import su.sergiusonesimus.tebreaker.mixin.interfaces.IMixinTileEntitySpecialRenderer;

public class TileEntityBreakerIntegration {

    public static final String SHAFT = "shaft";
    public static final String CREATIVE_MOTOR = "creative_motor";
    public static final String COGWHEEL = "cogwheel";
    public static final String LARGE_COGWHEEL = "large_cogwheel";
    public static final String BEARING = "bearing";

    public static void registerTileEntities() {
        ShaftModel shaft = new ShaftModel();
        shaft.shaft.rotationPointY += 8;
        TileEntityBreaker.registerModel(SHAFT, 32, 32, shaft.shaft);
        TileEntityBreaker.registerTileEntity(ShaftTileEntity.class, SHAFT);

        CreativeMotorModel creativeMotor = new CreativeMotorModel();
        TileEntityBreaker.registerModel(
            CREATIVE_MOTOR,
            64,
            48,
            creativeMotor.sideCover1,
            creativeMotor.bottomCover,
            creativeMotor.rod1,
            creativeMotor.motor,
            creativeMotor.stand,
            creativeMotor.coiling,
            creativeMotor.wiper1);
        TileEntityBreaker.registerTileEntity(CreativeMotorTileEntity.class, SHAFT);

        CogWheelModel cogwheel = new CogWheelModel();
        TileEntityBreaker.registerModel(COGWHEEL, 48, 32, cogwheel.hub, cogwheel.disk, cogwheel.cogs[0]);
        LargeCogWheelModel largeCogwheel = new LargeCogWheelModel();
        TileEntityBreaker.registerModel(
            LARGE_COGWHEEL,
            80,
            48,
            largeCogwheel.hub1,
            largeCogwheel.hubParts[0],
            largeCogwheel.disk,
            largeCogwheel.cogs[0]);
        TileEntityBreaker.registerTileEntity(CogWheelTileEntity.class, SHAFT);

        BearingModel bearing = new BearingModel();
        bearing.sides[0].rotationPointZ = 8;
        bearing.top.rotationPointY = -4;
        bearing.blockIndicator.rotationPointY = 5;
        bearing.topIndicator.rotationPointY = 3;
        TileEntityBreaker.registerModel(
            BEARING,
            64,
            64,
            bearing.bearing,
            bearing.sides[0],
            bearing.blockIndicator,
            bearing.top,
            bearing.topIndicator);
        TileEntityBreaker.registerTileEntity(MechanicalBearingTileEntity.class, SHAFT);

        TileEntityBreaker.registerTileEntity(SplitShaftTileEntity.class, SHAFT);

        TileEntityBreaker.registerTileEntity(GearboxTileEntity.class, SHAFT);
    }

    public static boolean shouldRenderDamageTexture(TileEntitySpecialRenderer renderer) {
        return ((IMixinTileEntitySpecialRenderer) renderer).getBreakTexture() != null;
    }

    public static void setBreakTexture(TileEntitySpecialRenderer renderer, String texture,
        DestroyBlockProgress destroyblockprogress) {
        ((IMixinTileEntitySpecialRenderer) renderer).setBreakTexture(
            TileEntityBreaker.getDestructionTexture(texture, destroyblockprogress.getPartialBlockDamage()));
    }

    public static void setBreakTexture(TileEntitySpecialRenderer renderer, ResourceLocation texture) {
        ((IMixinTileEntitySpecialRenderer) renderer).setBreakTexture(texture);
    }

    public static DestroyBlockProgress getTileEntityDestroyProgress(TileEntity te) {
        return TileEntityBreaker.getTileEntityDestroyProgress(te);
    }

}
