package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.client.multiplayer.SubWorldClient;
import su.sergiusonesimus.metaworlds.network.play.server.S03SubWorldUpdatePacket;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class ContraptionWorldUpdatePacket extends S03SubWorldUpdatePacket {

    public boolean stalled;

    public ContraptionWorldUpdatePacket() {
        super();
    }

    public ContraptionWorldUpdatePacket(ContraptionWorldServer contraptionWorldServer, int updateFlags) {
        super(contraptionWorldServer, updateFlags);
        stalled = contraptionWorldServer.isStalled();
    }

    @Override
    public void fromBytes(ByteBuf par1DataInput) {
        super.fromBytes(par1DataInput);
        this.stalled = par1DataInput.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf par1DataOutput) {
        super.toBytes(par1DataOutput);
        par1DataOutput.writeBoolean(stalled);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnTick() {
        super.executeOnTick();
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        World targetWorld = ((IMixinWorld) player.worldObj).getSubWorld(this.subWorldId);
        if (targetWorld == null) return;

        if (targetWorld instanceof ContraptionWorldClient targetContraptionWorld) {
            targetContraptionWorld.setStalled(stalled);
        }
    }

    public static class Handler implements IMessageHandler<ContraptionWorldUpdatePacket, IMessage> {

        @Override
        public IMessage onMessage(ContraptionWorldUpdatePacket message, MessageContext ctx) {
            if (!ctx.side.isServer()) {
                EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
                World targetWorld = ((IMixinWorld) player.worldObj).getSubWorld(message.subWorldId);
                if (targetWorld == null) {
                    return null;
                }

                if (((IMixinWorld) targetWorld).isSubWorld()) {
                    SubWorldClient targetSubWorld = (SubWorldClient) targetWorld;
                    if (targetSubWorld.lastServerTickReceived < message.serverTick
                        && (targetSubWorld.getUpdatePacketToHandle() == null
                            || targetSubWorld.getUpdatePacketToHandle().serverTick <= message.serverTick)) {
                        targetSubWorld.setUpdatePacketToHandle(message);
                    }
                }
            }
            return null;
        }
    }

}
