package su.sergiusonesimus.recreate.foundation.utility.outliner;

import net.minecraft.util.Vec3;

import su.sergiusonesimus.recreate.util.ReCreateMath;

public class LineOutline extends Outline {

    protected Vec3 start = Vec3.createVectorHelper(0, 0, 0);
    protected Vec3 end = Vec3.createVectorHelper(0, 0, 0);

    public LineOutline set(Vec3 start, Vec3 end) {
        this.start = start;
        this.end = end;
        return this;
    }

    @Override
    public void render(float pt) {
        renderCuboidLine(start, end);
    }

    public static class EndChasingLineOutline extends LineOutline {

        float prevProgress = 0;
        float progress = 0;

        @Override
        public void tick() {}

        public EndChasingLineOutline setProgress(float progress) {
            prevProgress = this.progress;
            this.progress = progress;
            return this;
        }

        @Override
        public LineOutline set(Vec3 start, Vec3 end) {
            if (!end.equals(this.end)) super.set(start, end);
            return this;
        }

        @Override
        public void render(float pt) {
            float distanceToTarget = 1 - ReCreateMath.lerp(pt, prevProgress, progress);
            Vec3 vecToAdd = this.end.subtract(start);
            Vec3 start = end.addVector(vecToAdd.xCoord, vecToAdd.yCoord, vecToAdd.zCoord);
            start = Vec3.createVectorHelper(
                start.xCoord * distanceToTarget,
                start.yCoord * distanceToTarget,
                start.zCoord * distanceToTarget);
            renderCuboidLine(start, end);
        }

    }

}
