package lotr.common.network;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.LOTRLevelData;
import lotr.common.LOTRPlayerData;
import lotr.common.fellowship.LOTRFellowship;
import lotr.common.fellowship.LOTRFellowshipClient;
import net.minecraft.entity.player.EntityPlayerMP;

public class LOTRPacketFellowshipLeave extends LOTRPacketFellowshipDo {
    public LOTRPacketFellowshipLeave() {
    }

    public LOTRPacketFellowshipLeave(LOTRFellowshipClient fs) {
        super(fs);
    }

    @Override
    public void toBytes(ByteBuf data) {
        super.toBytes(data);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        super.fromBytes(data);
    }

    public static class Handler implements IMessageHandler<LOTRPacketFellowshipLeave, IMessage> {
        @Override
        public IMessage onMessage(LOTRPacketFellowshipLeave packet, MessageContext context) {
            EntityPlayerMP entityplayer = context.getServerHandler().playerEntity;
            LOTRFellowship fellowship = packet.getFellowship();
            if(fellowship != null) {
                LOTRPlayerData playerData = LOTRLevelData.getData(entityplayer);
                playerData.leaveFellowship(fellowship);
            }
            return null;
        }
    }

}
