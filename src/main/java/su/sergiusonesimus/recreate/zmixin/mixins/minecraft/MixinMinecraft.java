package su.sergiusonesimus.recreate.zmixin.mixins.minecraft;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.scrollvalue.ScrollValueHandler;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @WrapOperation(
        method = "runTick",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I",
            opcode = Opcodes.INVOKESTATIC,
            remap = false))
    private int onMouseScrolled(Operation<Integer> original) {
        int delta = original.call();
        if (delta != 0) {
            delta = delta > 0 ? 1 : -1;
            boolean cancelled = /*
                                 * TODO ClientProxy.SCHEMATIC_HANDLER.mouseScrolled(delta)
                                 * || ClientProxy.SCHEMATIC_AND_QUILL_HANDLER.mouseScrolled(delta)
                                 * || FilteringHandler.onScroll(delta)
                                 * ||
                                 */ScrollValueHandler.onScroll(delta);

            if (cancelled) delta = 0;
        }
        return delta;
    }

}
