package su.sergiusonesimus.recreate.zmixin.interfaces;

import net.minecraft.potion.PotionEffect;

public interface IMixinPotionEffect {

    boolean getIsVisible();

    PotionEffect setIsVisible(boolean isVisible);

}
