package lotr.common.network;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.LOTRLevelData;
import lotr.common.LOTRMod;
import net.minecraft.entity.player.EntityPlayer;

public class LOTRPacketFTTimer implements IMessage {
    private int timer;

    public LOTRPacketFTTimer() {
    }

    public LOTRPacketFTTimer(int i) {
        this.timer = i;
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeInt(this.timer);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        this.timer = data.readInt();
    }

    public static class Handler implements IMessageHandler<LOTRPacketFTTimer, IMessage> {
        @Override
        public IMessage onMessage(LOTRPacketFTTimer packet, MessageContext context) {
            EntityPlayer entityplayer = LOTRMod.proxy.getClientPlayer();
            LOTRLevelData.getData(entityplayer).setFTTimer(packet.timer);
            return null;
        }
    }

}
