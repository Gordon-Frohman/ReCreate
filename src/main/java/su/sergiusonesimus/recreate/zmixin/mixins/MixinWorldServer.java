package su.sergiusonesimus.recreate.zmixin.mixins;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import org.spongepowered.asm.mixin.Mixin;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.Contraption;

@Mixin(WorldServer.class)
public class MixinWorldServer extends MixinWorld {

    public World createContraptionWorld(int newSubWorldID, Contraption contraption) {
        World newSubWorld = this.generateContraptionWorld(newSubWorldID, contraption);
        
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(newSubWorld));
        
        return newSubWorld;
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
        
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(newSubWorld));
        
        return newSubWorld;
	}

}
