package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.network.play.server.S03SubWorldUpdatePacket;
import su.sergiusonesimus.metaworlds.world.SubWorldServer;

public class ContraptionWorldServer extends SubWorldServer implements ContraptionWorld {

    private Contraption contraption;

    private boolean stalled = false;

    public ContraptionWorldServer(WorldServer parentWorld, int newSubWorldID, MinecraftServer par1MinecraftServer,
        ISaveHandler par2ISaveHandler, String par3Str, int par4, WorldSettings par5WorldSettings, Profiler par6Profiler,
        Contraption contraption) {
        super(
            parentWorld,
            newSubWorldID,
            par1MinecraftServer,
            par2ISaveHandler,
            par3Str,
            par4,
            par5WorldSettings,
            par6Profiler);
        this.contraption = contraption;
    }

    @Override
    public S03SubWorldUpdatePacket getUpdatePacket(SubWorldServer par1SubWorldServer, int updateFlags) {
        return new ContraptionWorldUpdatePacket((ContraptionWorldServer) par1SubWorldServer, updateFlags);
    }

    @Override
    public void removeSubWorld() {
        if (contraption != null) {
            if (!contraption.ticking) contraption.stop(this.getParentWorld());
            contraption.onRemoved();
        }
        super.removeSubWorld();
    }

    @Override
    public Contraption getContraption() {
        return contraption;
    }

    @Override
    public void setContraption(Contraption contraption) {
        this.contraption = contraption;
    }

    @Override
    public void tick() {
        super.tick();
        if (!contraption.beingRemoved) contraption.tick();
    }

    @Override
    public boolean getIsInMotion() {
        return super.getIsInMotion()
            || (contraption.parentWorld instanceof SubWorld subworld && subworld.getIsInMotion());
    }

    @Override
    public void addSittingPassenger(Entity passenger, int seatIndex) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePassenger(Entity passenger) {
        // TODO Auto-generated method stub

    }

    @Override
    public Vec3 getPassengerPosition(Entity passenger, float partialTicks) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void ejectPassengers() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isStalled() {
        return stalled;
    }

    @Override
    public void setStalled(boolean isStalled) {
        stalled = isStalled;
    }

    @Override
    public List<Entity> getPassengers() {
        // TODO Auto-generated method stub
        return null;
    }

}
