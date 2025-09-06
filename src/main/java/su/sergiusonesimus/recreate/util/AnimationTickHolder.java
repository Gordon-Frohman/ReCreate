package su.sergiusonesimus.recreate.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.IBlockAccess;

public class AnimationTickHolder {

    private static int ticks;
    private static int paused_ticks;

    public static void reset() {
        ticks = 0;
        paused_ticks = 0;
    }

    public static void tick() {
        if (!Minecraft.getMinecraft()
            .isGamePaused()) {
            ticks = (ticks + 1) % 1_728_000; // wrap around every 24 hours so we maintain enough floating point
                                             // precision
        } else {
            paused_ticks = (paused_ticks + 1) % 1_728_000;
        }
    }

    public static int getTicks() {
        return getTicks(false);
    }

    public static int getTicks(boolean includePaused) {
        return includePaused ? ticks + paused_ticks : ticks;
    }

    public static float getRenderTime() {
        return getTicks() + getPartialTicks();
    }

    public static float getPartialTicks() {
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.isGamePaused() ? mc.timer.renderPartialTicks : mc.timer.elapsedPartialTicks);
    }

    public static int getTicks(IBlockAccess world) {
        // TODO
        // if (world instanceof WrappedClientWorld)
        // return getTicks(((WrappedClientWorld) world).getWrappedWorld());
        return /* world instanceof PonderWorld ? PonderUI.ponderTicks : */ getTicks();
    }

    public static float getRenderTime(IBlockAccess world) {
        return getTicks(world) + getPartialTicks(world);
    }

    public static float getPartialTicks(IBlockAccess world) {
        // TODO
        return /* world instanceof PonderWorld ? PonderUI.getPartialTicks() : */ getPartialTicks();
    }
}
