package su.sergiusonesimus.recreate.foundation.utility.animation;

import su.sergiusonesimus.recreate.foundation.utility.AngleHelper;

/**
 * Use {@link LerpedFloat} instead.
 */
@Deprecated
public class InterpolatedChasingAngle extends InterpolatedChasingValue {

    public float get(float partialTicks) {
        return AngleHelper.angleLerp(partialTicks, lastValue, value);
    }

    @Override
    protected float getCurrentDiff() {
        return AngleHelper.getShortestAngleDiff(value, getTarget());
    }

}
