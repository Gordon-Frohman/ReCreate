package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement;

import java.util.List;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.client.multiplayer.SubWorldClient;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@SideOnly(Side.CLIENT)
public class ContraptionWorldClient extends SubWorldClient implements ContraptionWorld {

    private Contraption contraption;

    private boolean stalled = false;

    public ContraptionWorldClient(WorldClient parentWorld, int newSubWorldID, NetHandlerPlayClient par1NetClientHandler,
        WorldSettings par2WorldSettings, int par3, EnumDifficulty par4, Profiler par5Profiler,
        Contraption contraption) {
        super(parentWorld, newSubWorldID, par1NetClientHandler, par2WorldSettings, par3, par4, par5Profiler);
        this.contraption = contraption;
        if (contraption != null) {
            contraption.contraptionWorld = this;
            if (contraption.parentWorld == null)
                contraption.parentWorld = ((IMixinWorld) parentWorld).getSubWorld(contraption.parentWorldID);
        }
    }

    @Override
    public void removeSubWorld() {
        if (contraption != null) {
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
        if (contraption != null) contraption.tick();
    }

    @Override
    public void doTickPartial(double interpolationFactor) {
        super.doTickPartial(interpolationFactor);
        if (contraption != null) contraption.doTickPartial(interpolationFactor);
    }

    @Override
    public boolean getIsInMotion() {
        return super.getIsInMotion() || (contraption != null && contraption.parentWorld != null
            && contraption.parentWorld instanceof SubWorld subworld
            && subworld.getIsInMotion());
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
