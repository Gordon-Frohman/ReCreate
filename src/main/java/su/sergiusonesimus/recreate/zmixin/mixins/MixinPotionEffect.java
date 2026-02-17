package su.sergiusonesimus.recreate.zmixin.mixins;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import su.sergiusonesimus.recreate.zmixin.interfaces.IMixinPotionEffect;

@Mixin(PotionEffect.class)
public class MixinPotionEffect implements IMixinPotionEffect {

    private boolean isVisible = true;

    @Override
    public boolean getIsVisible() {
        return isVisible;
    }

    @Override
    public PotionEffect setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
        return (PotionEffect) (Object) this;
    }

    @Inject(method = "equals", at = @At(value = "RETURN"), cancellable = true)
    public void equals(Object p_equals_1_, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            cir.setReturnValue(this.isVisible == ((IMixinPotionEffect) p_equals_1_).getIsVisible());
        }
    }

    @Inject(method = "writeCustomPotionEffectToNBT", at = @At("RETURN"))
    public void writeCustomPotionEffectToNBT(NBTTagCompound nbt, CallbackInfoReturnable<NBTTagCompound> cir) {
        nbt.setBoolean("Visible", this.isVisible);
    }

    @Inject(method = "readCustomPotionEffectFromNBT", at = @At("RETURN"), cancellable = true)
    public static void readCustomPotionEffectFromNBT(NBTTagCompound nbt, CallbackInfoReturnable<PotionEffect> cir) {
        PotionEffect result = cir.getReturnValue();
        if (result != null && nbt.hasKey("Visible")) {
            ((IMixinPotionEffect) result).setIsVisible(nbt.getBoolean("Visible"));
            cir.setReturnValue(result);
        }
    }

}
