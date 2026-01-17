package su.sergiusonesimus.recreate.zmixin.mixins;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.Contraption;

@Mixin(WorldClient.class)
public class MixinWorldClient extends MixinWorld {

    public World createContraptionWorld(int newSubWorldID, Contraption contraption) {
        return this.generateContraptionWorld(newSubWorldID, contraption);
    }

    @Override
    public World createContraptionWorld(int newSubWorldID, Contraption contraption, double centerX, double centerY,
        double centerZ, double translationX, double translationY, double translationZ, double rotationPitch,
        double rotationYaw, double rotationRoll, double scaling) {
        World newSubWorld = this.generateContraptionWorld(newSubWorldID, contraption);
        SubWorld subworld = (SubWorld) newSubWorld;

        subworld.setCenter(centerX, centerY, centerZ);
        subworld.setTranslation(translationX, translationY, translationZ);
        subworld.setRotationYaw(rotationYaw);
        subworld.setRotationPitch(rotationPitch);
        subworld.setRotationRoll(rotationRoll);
        subworld.setScaling(scaling);

        return newSubWorld;
    }

}
