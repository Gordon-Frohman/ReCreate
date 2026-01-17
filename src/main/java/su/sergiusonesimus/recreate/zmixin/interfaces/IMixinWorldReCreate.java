package su.sergiusonesimus.recreate.zmixin.interfaces;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.Contraption;

public interface IMixinWorldReCreate {

    /**
     * Creates a new contraption world with the first unoccupied id and given Contraption <br>
     * If this world is a subworld this will function will redirect to parent.createContraptionWorld()
     * 
     * @return new contraption world
     */
    public default World createContraptionWorld(Contraption contraption) {
        return this.createContraptionWorld(((IMixinWorld) this).getUnoccupiedSubworldID(), contraption);
    }

    /**
     * Creates a new SubWorld with the first unoccupied id, given Contraption and parameters <br>
     * If this world is a subworld this will function will redirect to parent.createContraptionWorld()
     * 
     * @return new contraption world
     */
    public default World createContraptionWorld(Contraption contraption, double centerX, double centerY, double centerZ,
        double translationX, double translationY, double translationZ, double rotationPitch, double rotationYaw,
        double rotationRoll, double scaling) {
        return this.createContraptionWorld(
    		((IMixinWorld) this).getUnoccupiedSubworldID(),
            contraption,
            centerX,
            centerY,
            centerZ,
            translationX,
            translationY,
            translationZ,
            rotationPitch,
            rotationYaw,
            rotationRoll,
            scaling);
    }

    /**
     * Creates a new contraption world with the given id and Contraption <br>
     * If this world is a subworld this will function will redirect to parent.createContraptionWorld()
     * 
     * @return new contraption world
     */
    public abstract World createContraptionWorld(int newSubWorldID, Contraption contraption);

    /**
     * Creates a new SubWorld with the given id, contraption and parameters <br>
     * If this world is a subworld this will function will redirect to parent.createContraptionWorld()
     * 
     * @return new contraption world
     */
    public abstract World createContraptionWorld(int newSubWorldID, Contraption contraption, double centerX,
        double centerY, double centerZ, double translationX, double translationY, double translationZ,
        double rotationPitch, double rotationYaw, double rotationRoll, double scaling);

    /**
     * Will get all contraptions intersecting the specified AABB excluding the one passed into it. Args:
     * contraptionToExclude, aabb
     */
    public List<Contraption> getContraptionsWithinAABBExcludingContraption(Contraption contraption, AxisAlignedBB bb);

    public List<Contraption> getContraptionsWithinAABBExcludingContraption(Contraption contraption, AxisAlignedBB bb,
        Predicate<Contraption> selector);

    /**
     * Returns all contraptions of the specified class type which intersect with the AABB. Args: contraptionClass, aabb
     */
    public <T extends Contraption> List<T> getContraptionsWithinAABB(Class<T> contraptionClass, AxisAlignedBB bb);

    public <T extends Contraption> List<T> getContraptionsWithinAABB(Class<T> contraptionClass, AxisAlignedBB bb,
        Predicate<T> selector);

}
