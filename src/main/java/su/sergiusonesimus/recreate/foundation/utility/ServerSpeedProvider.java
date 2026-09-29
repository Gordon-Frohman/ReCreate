package su.sergiusonesimus.recreate.foundation.utility;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.recreate.foundation.config.AllConfigs;
import su.sergiusonesimus.recreate.foundation.networking.AllPackets;
import su.sergiusonesimus.recreate.foundation.utility.animation.InterpolatedChasingValue;

@SuppressWarnings("deprecation")
public class ServerSpeedProvider {

    static int clientTimer = 0;
    static int serverTimer = 0;
    static boolean initialized = false;
    static InterpolatedChasingValue modifier = new InterpolatedChasingValue().withSpeed(.25f);

    public static void serverTick() {
        serverTimer++;
        if (serverTimer > getSyncInterval()) {
            AllPackets.channel.sendToAll(new Packet());
            serverTimer = 0;
        }
    }

    @SideOnly(Side.CLIENT)
    public static void clientTick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.isSingleplayer() && mc.getIntegratedServer() != null
            && mc.getIntegratedServer()
                .getPublic())
            return;
        modifier.tick();
        clientTimer++;
    }

    public static Integer getSyncInterval() {
        return AllConfigs.SERVER.tickrateSyncTimer;
    }

    public static float get() {
        return modifier.value;
    }

    public static class Packet implements IMessage {

        public Packet() {}

        @Override
        public void fromBytes(ByteBuf buf) {}

        @Override
        public void toBytes(ByteBuf buf) {}

        public static class Handler implements IMessageHandler<Packet, IMessage> {

            @Override
            public IMessage onMessage(Packet message, MessageContext ctx) {
                if (!initialized) {
                    initialized = true;
                    clientTimer = 0;
                    return null;
                }
                float target = ((float) getSyncInterval() * 2) / (float) Math.max(clientTimer, 1);
                modifier.target(Math.min(target, 1));
                // Set this to -1 because packets are processed before ticks.
                // ServerSpeedProvider#clientTick will increment it to 0 at the end of this tick.
                // Setting it to 0 causes consistent desync, as the client ends up counting too many ticks.
                clientTimer = -1;
                return null;
            }
        }

    }

}
