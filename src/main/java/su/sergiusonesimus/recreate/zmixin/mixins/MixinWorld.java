package su.sergiusonesimus.recreate.zmixin.mixins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.Contraption;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.ContraptionWorld;
import su.sergiusonesimus.recreate.zmixin.interfaces.IMixinWorldReCreate;

@Mixin(World.class)
public abstract class MixinWorld implements IMixinWorldReCreate {

    @Shadow(remap = false)
    public Map<Integer, World> childSubWorlds;

    @SuppressWarnings("rawtypes")
    @Shadow(remap = true)
    public List playerEntities;

    @Shadow(remap = true)
    public WorldInfo worldInfo;

    @Shadow(remap = false)
    protected void registerSubWorld(World newSubWorld) {}

    protected World generateContraptionWorld(int newSubWorldID, Contraption contraption) {
        World newSubWorld = ReCreate.proxy.createContraptionWorld((World) (Object) this, newSubWorldID, contraption);
        registerSubWorld(newSubWorld);
        return newSubWorld;
    }

    @Override
    public List<Contraption> getContraptionsWithinAABBExcludingContraption(Contraption contraption, AxisAlignedBB bb) {
        return this.getContraptionsWithinAABBExcludingContraption(contraption, bb, null);
    }

    @Override
    public List<Contraption> getContraptionsWithinAABBExcludingContraption(Contraption contraption, AxisAlignedBB bb,
        Predicate<Contraption> selector) {
        ArrayList<Contraption> result = new ArrayList<Contraption>();

        for (ContraptionWorld contraptionWorld : ((IMixinWorld) this)
            .getSubworldsWithinAABB(ContraptionWorld.class, bb)) {
            Contraption currentContraption = contraptionWorld.getContraption();
            if (currentContraption != null && !currentContraption.beingRemoved
                && (selector == null || selector.test(currentContraption))) result.add(currentContraption);
        }

        return result;
    }

    @Override
    public <T extends Contraption> List<T> getContraptionsWithinAABB(Class<T> contraptionClass, AxisAlignedBB bb) {
        return this.getContraptionsWithinAABB(contraptionClass, bb, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Contraption> List<T> getContraptionsWithinAABB(Class<T> contraptionClass, AxisAlignedBB bb,
        Predicate<T> selector) {
        if (contraptionClass == null) return null;
        ArrayList<T> result = new ArrayList<T>();

        for (ContraptionWorld contraptionWorld : ((IMixinWorld) this)
            .getSubworldsWithinAABB(ContraptionWorld.class, bb)) {
            Contraption currentContraption = contraptionWorld.getContraption();
            if (currentContraption != null && !currentContraption.beingRemoved
                && contraptionClass.isInstance(currentContraption)
                && (selector == null || selector.test((T) currentContraption))) result.add((T) currentContraption);
        }

        return result;
    }

}
