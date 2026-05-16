package su.sergiusonesimus.recreate.zmixin.mixins.minecraft;

import java.util.Collection;
import java.util.Iterator;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import su.sergiusonesimus.recreate.zmixin.interfaces.IMixinPotionEffect;

@Mixin(PotionHelper.class)
public class MixinPotionHelper {

    /**
     * Given a {@link Collection}<{@link PotionEffect}> will return an Integer color.
     */
    @Overwrite
    public static int calcPotionLiquidColor(Collection<PotionEffect> potionEffects) {
        int i = 3694022;

        if (potionEffects != null && !potionEffects.isEmpty()) {
            float f = 0.0F;
            float f1 = 0.0F;
            float f2 = 0.0F;
            float f3 = 0.0F;
            Iterator<PotionEffect> iterator = potionEffects.iterator();

            while (iterator.hasNext()) {
                PotionEffect potioneffect = iterator.next();
                if (!((IMixinPotionEffect) potioneffect).getIsVisible()) continue;
                int j = Potion.potionTypes[potioneffect.getPotionID()].getLiquidColor();

                for (int k = 0; k <= potioneffect.getAmplifier(); ++k) {
                    f += (float) (j >> 16 & 255) / 255.0F;
                    f1 += (float) (j >> 8 & 255) / 255.0F;
                    f2 += (float) (j >> 0 & 255) / 255.0F;
                    ++f3;
                }
            }
            if (f3 == 0) return 0;

            f = f / f3 * 255.0F;
            f1 = f1 / f3 * 255.0F;
            f2 = f2 / f3 * 255.0F;
            return (int) f << 16 | (int) f1 << 8 | (int) f2;
        } else {
            return i;
        }
    }

}
