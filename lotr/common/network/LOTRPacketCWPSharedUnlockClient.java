package lotr.common.network;

import java.util.UUID;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.*;
import lotr.common.world.map.LOTRCustomWaypoint;
import net.minecraft.entity.player.EntityPlayer;

public class LOTRPacketCWPSharedUnlockClient implements IMessage {
    private int cwpID;
    private UUID sharingPlayer;

    public LOTRPacketCWPSharedUnlockClient() {
    }

    public LOTRPacketCWPSharedUnlockClient(int id, UUID player) {
        this.cwpID = id;
        this.sharingPlayer = player;
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeInt(this.cwpID);
        data.writeLong(this.sharingPlayer.getMostSignificantBits());
        data.writeLong(this.sharingPlayer.getLeastSignificantBits());
    }

    @Override
    public void fromBytes(ByteBuf data) {
        this.cwpID = data.readInt();
        this.sharingPlayer = new UUID(data.readLong(), data.readLong());
    }

    public static class Handler implements IMessageHandler<LOTRPacketCWPSharedUnlockClient, IMessage> {
        @Override
        public IMessage onMessage(LOTRPacketCWPSharedUnlockClient packet, MessageContext context) {
            LOTRCustomWaypoint cwp;
            LOTRPlayerData pd;
            EntityPlayer entityplayer;
            if(!LOTRMod.proxy.isSingleplayer() && (cwp = (pd = LOTRLevelData.getData(entityplayer = LOTRMod.proxy.getClientPlayer())).getSharedCustomWaypointByID(packet.sharingPlayer, packet.cwpID)) != null) {
                pd.unlockSharedCustomWaypoint(cwp);
            }
            return null;
        }
    }

}
