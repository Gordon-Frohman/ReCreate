package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.world.SubWorldInfoHolder;

public class ContraptionWorldInfoHolder extends SubWorldInfoHolder {

    Contraption contraption = null;
    boolean isStalled = false;

    public ContraptionWorldInfoHolder(SubWorld sourceWorld) {
        super(sourceWorld);
        if (sourceWorld instanceof ContraptionWorld) {
            ContraptionWorld contraptionWorld = (ContraptionWorld) sourceWorld;
            contraption = contraptionWorld.getContraption();
            isStalled = contraptionWorld.isStalled();
        }
    }

    public ContraptionWorldInfoHolder(SubWorldInfoHolder sourceWorld) {
        super(sourceWorld);
        if (sourceWorld instanceof ContraptionWorldInfoHolder) {
            ContraptionWorldInfoHolder sourceContraptionWorld = (ContraptionWorldInfoHolder) sourceWorld;
            contraption = sourceContraptionWorld.contraption;
            isStalled = sourceContraptionWorld.isStalled;
        }
    }

    public ContraptionWorldInfoHolder(NBTTagCompound sourceNBT) {
        super(sourceNBT);
        if (sourceNBT.hasKey("Contraption"))
            contraption = Contraption.fromNBT(sourceNBT.getCompoundTag("Contraption"), false);
        isStalled = sourceNBT.getBoolean("Stalled");
    }

    public SubWorldInfoHolder copy() {
        return new ContraptionWorldInfoHolder(this);
    }

    public void writeToNBT(NBTTagCompound targetNBT) {
        super.writeToNBT(targetNBT);
        if (contraption != null) targetNBT.setTag("Contraption", contraption.writeNBT(false));
        targetNBT.setBoolean("Stalled", isStalled);
    }

    public void applyToSubWorld(SubWorld targetWorld) {
        super.applyToSubWorld(targetWorld);
        if (targetWorld instanceof ContraptionWorld) {
            ContraptionWorld targetContraptionWorld = (ContraptionWorld) targetWorld;
            targetContraptionWorld.setContraption(contraption);
            contraption.contraptionWorld = (World) targetContraptionWorld;
            targetContraptionWorld.setStalled(isStalled);
        }
    }
}
