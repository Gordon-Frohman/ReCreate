package su.sergiusonesimus.recreate.compat.tebreaker;

import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorModel;
import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.BearingModel;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.waterwheel.WaterWheelTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltCasingModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltPart;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltPulleyModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltSlope;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.LargeCogWheelModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.SplitShaftTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.gearbox.GearboxTileEntity;
import su.sergiusonesimus.tebreaker.TileEntityBreaker;
import su.sergiusonesimus.tebreaker.mixin.interfaces.IMixinTileEntitySpecialRenderer;

@SideOnly(Side.CLIENT)
public class TileEntityBreakerIntegration {

    public static final String TEBREAKER = "tebreaker";

    public static final String SHAFT = "shaft";
    public static final String CREATIVE_MOTOR = "creative_motor";
    public static final String COGWHEEL = "cogwheel";
    public static final String LARGE_COGWHEEL = "large_cogwheel";
    public static final String BEARING = "bearing";
    public static final String WATER_WHEEL = "water_wheel";
    public static final String BELT = "belt";
    public static final String DIAGONAL_BELT = "diagonal_belt";
    public static final String BELT_PULLEY = "belt_pulley";
    public static final String BELT_CASING = "belt_casing";

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

        TileEntityBreaker.registerModel(WATER_WHEEL, TEBREAKER);
        TileEntityBreaker.registerTileEntity(WaterWheelTileEntity.class, SHAFT);

        TileEntityBreaker.registerTileEntity(MechanicalPistonTileEntity.class, SHAFT);

        TileEntityBreaker.registerModel(BELT, TEBREAKER);
        TileEntityBreaker.registerModel(DIAGONAL_BELT, TEBREAKER);
        BeltPulleyModel pulley = new BeltPulleyModel();
        TileEntityBreaker.registerModel(BELT_PULLEY, 16, 16, pulley.pulleyParts[0]);
        BeltCasingModel horizontalMiddleCasing = new BeltCasingModel(BeltSlope.HORIZONTAL, BeltPart.MIDDLE);
        BeltCasingModel horizontalEndCasing = new BeltCasingModel(BeltSlope.HORIZONTAL, BeltPart.END);
        BeltCasingModel diagonalMiddleCasing = new BeltCasingModel(BeltSlope.UPWARD, BeltPart.MIDDLE);
        BeltCasingModel diagonalStartCasing = new BeltCasingModel(BeltSlope.UPWARD, BeltPart.START);
        BeltCasingModel sidewaysMiddleCasing = new BeltCasingModel(BeltSlope.SIDEWAYS, BeltPart.MIDDLE);
        BeltCasingModel sidewaysPulleyCasing = new BeltCasingModel(BeltSlope.SIDEWAYS, BeltPart.PULLEY);
        TileEntityBreaker.registerModel(
            BELT_CASING,
            64,
            64,
            horizontalMiddleCasing.bottom[0],
            horizontalMiddleCasing.sides[0],
            horizontalEndCasing.bottom[0],
            horizontalEndCasing.sides[1],
            horizontalEndCasing.pulley,
            diagonalStartCasing.bottom[1],
            diagonalMiddleCasing.sides[1],
            sidewaysMiddleCasing.sides[0],
            sidewaysPulleyCasing.sides[2]);
        TileEntityBreaker.registerTileEntity(BeltTileEntity.class, SHAFT);
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
