package lotr.common.network;

import java.util.*;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.*;
import lotr.common.fac.LOTRFaction;

public class LOTRPacketAlignment implements IMessage {
    private UUID player;
    private Map<LOTRFaction, Float> alignmentMap = new HashMap<LOTRFaction, Float>();
    private boolean hideAlignment;

    public LOTRPacketAlignment() {
    }

    public LOTRPacketAlignment(UUID uuid) {
        this.player = uuid;
        LOTRPlayerData pd = LOTRLevelData.getData(this.player);
        for(LOTRFaction f : LOTRFaction.values()) {
            float al = pd.getAlignment(f);
            this.alignmentMap.put(f, Float.valueOf(al));
        }
        this.hideAlignment = pd.getHideAlignment();
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeLong(this.player.getMostSignificantBits());
        data.writeLong(this.player.getLeastSignificantBits());
        for(Map.Entry<LOTRFaction, Float> entry : this.alignmentMap.entrySet()) {
            LOTRFaction f = entry.getKey();
            float alignment = entry.getValue().floatValue();
            data.writeByte(f.ordinal());
            data.writeFloat(alignment);
        }
        data.writeByte(-1);
        data.writeBoolean(this.hideAlignment);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        this.player = new UUID(data.readLong(), data.readLong());
        byte factionID = 0;
        while((factionID = data.readByte()) >= 0) {
            LOTRFaction f = LOTRFaction.forID(factionID);
            float alignment = data.readFloat();
            this.alignmentMap.put(f, Float.valueOf(alignment));
        }
        this.hideAlignment = data.readBoolean();
    }

    public static class Handler implements IMessageHandler<LOTRPacketAlignment, IMessage> {
        @Override
        public IMessage onMessage(LOTRPacketAlignment packet, MessageContext context) {
            if(!LOTRMod.proxy.isSingleplayer()) {
                LOTRPlayerData pd = LOTRLevelData.getData(packet.player);
                for(Map.Entry entry : packet.alignmentMap.entrySet()) {
                    LOTRFaction f = (LOTRFaction) (entry.getKey());
                    float alignment = ((Float) entry.getValue()).floatValue();
                    pd.setAlignment(f, alignment);
                }
                pd.setHideAlignment(packet.hideAlignment);
            }
            return null;
        }
    }

}
