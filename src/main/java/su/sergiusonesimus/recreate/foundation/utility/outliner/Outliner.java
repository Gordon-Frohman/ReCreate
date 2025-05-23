package su.sergiusonesimus.recreate.foundation.utility.outliner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;

import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.ValueBox;
import su.sergiusonesimus.recreate.foundation.utility.outliner.LineOutline.EndChasingLineOutline;
import su.sergiusonesimus.recreate.foundation.utility.outliner.Outline.OutlineParams;
import su.sergiusonesimus.recreate.util.ReCreateMath;

public class Outliner {

    private final Map<Object, OutlineEntry> outlines = Collections.synchronizedMap(new HashMap<>());
    private final Map<Object, OutlineEntry> outlinesView = Collections.unmodifiableMap(outlines);

    // Facade

    public OutlineParams showValueBox(Object slot, ValueBox box) {
        outlines.put(slot, new OutlineEntry(box));
        return box.getParams();
    }

    public OutlineParams showLine(Object slot, Vec3 start, Vec3 end) {
        if (!outlines.containsKey(slot)) {
            LineOutline outline = new LineOutline();
            outlines.put(slot, new OutlineEntry(outline));
        }
        OutlineEntry entry = outlines.get(slot);
        entry.ticksTillRemoval = 1;
        ((LineOutline) entry.outline).set(start, end);
        return entry.outline.getParams();
    }

    public OutlineParams endChasingLine(Object slot, Vec3 start, Vec3 end, float chasingProgress) {
        if (!outlines.containsKey(slot)) {
            EndChasingLineOutline outline = new EndChasingLineOutline();
            outlines.put(slot, new OutlineEntry(outline));
        }
        OutlineEntry entry = outlines.get(slot);
        entry.ticksTillRemoval = 1;
        ((EndChasingLineOutline) entry.outline).setProgress(chasingProgress)
            .set(start, end);
        return entry.outline.getParams();
    }

    public OutlineParams showAABB(Object slot, AxisAlignedBB bb, int ttl) {
        createAABBOutlineIfMissing(slot, bb);
        ChasingAABBOutline outline = getAndRefreshAABB(slot, ttl);
        outline.prevBB = outline.targetBB = outline.bb = bb;
        return outline.getParams();
    }

    public OutlineParams showAABB(Object slot, AxisAlignedBB bb) {
        createAABBOutlineIfMissing(slot, bb);
        ChasingAABBOutline outline = getAndRefreshAABB(slot);
        outline.prevBB = outline.targetBB = outline.bb = bb;
        return outline.getParams();
    }

    public OutlineParams chaseAABB(Object slot, AxisAlignedBB bb) {
        createAABBOutlineIfMissing(slot, bb);
        ChasingAABBOutline outline = getAndRefreshAABB(slot);
        outline.targetBB = bb;
        return outline.getParams();
    }

    public OutlineParams showCluster(Object slot, Iterable<ChunkCoordinates> selection) {
        BlockClusterOutline outline = new BlockClusterOutline(selection);
        OutlineEntry entry = new OutlineEntry(outline);
        outlines.put(slot, entry);
        return entry.getOutline()
            .getParams();
    }

    public void keep(Object slot) {
        if (outlines.containsKey(slot)) outlines.get(slot).ticksTillRemoval = 1;
    }

    public void remove(Object slot) {
        outlines.remove(slot);
    }

    public Optional<OutlineParams> edit(Object slot) {
        keep(slot);
        if (outlines.containsKey(slot)) return Optional.of(
            outlines.get(slot)
                .getOutline()
                .getParams());
        return Optional.empty();
    }

    public Map<Object, OutlineEntry> getOutlines() {
        return outlinesView;
    }

    // Utility

    private void createAABBOutlineIfMissing(Object slot, AxisAlignedBB bb) {
        if (!outlines.containsKey(slot) || !(outlines.get(slot).outline instanceof AABBOutline)) {
            ChasingAABBOutline outline = new ChasingAABBOutline(bb);
            outlines.put(slot, new OutlineEntry(outline));
        }
    }

    private ChasingAABBOutline getAndRefreshAABB(Object slot) {
        OutlineEntry entry = outlines.get(slot);
        entry.ticksTillRemoval = 1;
        return (ChasingAABBOutline) entry.getOutline();
    }

    private ChasingAABBOutline getAndRefreshAABB(Object slot, int ttl) {
        OutlineEntry entry = outlines.get(slot);
        entry.ticksTillRemoval = ttl;
        return (ChasingAABBOutline) entry.getOutline();
    }

    // Maintenance

    public void tickOutlines() {
        Iterator<OutlineEntry> iterator = outlines.values()
            .iterator();
        while (iterator.hasNext()) {
            OutlineEntry entry = iterator.next();
            entry.tick();
            if (!entry.isAlive()) iterator.remove();
        }
    }

    public void renderOutlines(float pt) {
        outlines.forEach((key, entry) -> {
            Outline outline = entry.getOutline();
            OutlineParams params = outline.getParams();
            params.alpha = 1;
            if (entry.isFading()) {
                int prevTicks = entry.ticksTillRemoval + 1;
                float fadeticks = OutlineEntry.fadeTicks;
                float lastAlpha = prevTicks >= 0 ? 1 : 1 + (prevTicks / fadeticks);
                float currentAlpha = 1 + (entry.ticksTillRemoval / fadeticks);
                float alpha = ReCreateMath.lerp(pt, lastAlpha, currentAlpha);

                params.alpha = alpha * alpha * alpha;
                if (params.alpha < 1 / 8f) return;
            }
            outline.render(pt);
        });
    }

    public static class OutlineEntry {

        static final int fadeTicks = 8;
        private Outline outline;
        private int ticksTillRemoval;

        public OutlineEntry(Outline outline) {
            this.outline = outline;
            ticksTillRemoval = 1;
        }

        public void tick() {
            ticksTillRemoval--;
            outline.tick();
        }

        public boolean isAlive() {
            return ticksTillRemoval >= -fadeTicks;
        }

        public boolean isFading() {
            return ticksTillRemoval < 0;
        }

        public Outline getOutline() {
            return outline;
        }

    }

}
