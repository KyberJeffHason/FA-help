package lotr.common.network;

import java.util.UUID;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.*;
import lotr.common.fellowship.LOTRFellowshipClient;
import lotr.common.world.map.LOTRCustomWaypoint;
import net.minecraft.entity.player.EntityPlayer;

public class LOTRPacketShareCWPClient implements IMessage {
    private int cwpID;
    private UUID fellowshipID;
    private boolean adding;

    public LOTRPacketShareCWPClient() {
    }

    public LOTRPacketShareCWPClient(int id, UUID fsID, boolean add) {
        this.cwpID = id;
        this.fellowshipID = fsID;
        this.adding = add;
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeInt(this.cwpID);
        data.writeLong(this.fellowshipID.getMostSignificantBits());
        data.writeLong(this.fellowshipID.getLeastSignificantBits());
        data.writeBoolean(this.adding);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        this.cwpID = data.readInt();
        this.fellowshipID = new UUID(data.readLong(), data.readLong());
        this.adding = data.readBoolean();
    }

    public static class Handler implements IMessageHandler<LOTRPacketShareCWPClient, IMessage> {
        @Override
        public IMessage onMessage(LOTRPacketShareCWPClient packet, MessageContext context) {
            LOTRCustomWaypoint cwp;
            LOTRFellowshipClient fsClient;
            LOTRPlayerData pd;
            EntityPlayer entityplayer;
            if(!LOTRMod.proxy.isSingleplayer() && (cwp = (pd = LOTRLevelData.getData(entityplayer = LOTRMod.proxy.getClientPlayer())).getCustomWaypointByID(packet.cwpID)) != null && (fsClient = pd.getClientFellowshipByID(packet.fellowshipID)) != null) {
                if(packet.adding) {
                    pd.customWaypointAddSharedFellowshipClient(cwp, fsClient);
                }
                else {
                    pd.customWaypointRemoveSharedFellowshipClient(cwp, fsClient);
                }
            }
            return null;
        }
    }

}
