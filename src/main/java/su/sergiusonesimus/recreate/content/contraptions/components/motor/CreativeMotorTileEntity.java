package su.sergiusonesimus.recreate.content.contraptions.components.motor;

import java.util.List;

import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.content.contraptions.base.GeneratingKineticTileEntity;
import su.sergiusonesimus.recreate.foundation.config.AllConfigs;
import su.sergiusonesimus.recreate.foundation.tileentity.TileEntityBehaviour;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.CenteredSideValueBoxTransform;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.scrollvalue.ScrollValueBehaviour;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.scrollvalue.ScrollValueBehaviour.StepContext;
import su.sergiusonesimus.recreate.foundation.utility.Lang;

public class CreativeMotorTileEntity extends GeneratingKineticTileEntity {

    public static final int DEFAULT_SPEED = 16;
    protected ScrollValueBehaviour generatedSpeed;

    @SuppressWarnings("static-access")
    @Override
    public void addBehaviours(List<TileEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        Integer max = AllConfigs.SERVER.kinetics.maxMotorSpeed;

        CenteredSideValueBoxTransform slot = new CenteredSideValueBoxTransform(
            (motor, side) -> motor.getFirst() instanceof CreativeMotorBlock
                && ((CreativeMotorBlock) motor.getFirst()).getDirection(motor.getSecond()) == side.getOpposite());

        generatedSpeed = new ScrollValueBehaviour(Lang.translate("generic.speed"), this, slot);
        generatedSpeed.between(-max, max);
        generatedSpeed.value = DEFAULT_SPEED;
        generatedSpeed.scrollableValue = DEFAULT_SPEED;
        generatedSpeed.withUnit(i -> Lang.translate("generic.unit.rpm"));
        generatedSpeed.withCallback(i -> this.updateGeneratedRotation());
        generatedSpeed.withStepFunction(CreativeMotorTileEntity::step);
        behaviours.add(generatedSpeed);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed()) updateGeneratedRotation();
    }

    @Override
    public float getGeneratedSpeed() {
        if (this.getBlockType() != AllBlocks.creative_motor) return 0;
        return convertToDirection(
            generatedSpeed.getValue(),
            ((CreativeMotorBlock) getBlockType()).getDirection(this.getBlockMetadata()));
    }

    public static int step(StepContext context) {
        int current = context.currentValue;
        int step = 1;

        if (!context.shift) {
            int magnitude = Math.abs(current) - (context.forward == current > 0 ? 0 : 1);

            if (magnitude >= 4) step *= 4;
            if (magnitude >= 32) step *= 4;
            if (magnitude >= 128) step *= 4;
        }

        return (int) (current + (context.forward ? step : -step) == 0 ? step + 1 : step);
    }

}
