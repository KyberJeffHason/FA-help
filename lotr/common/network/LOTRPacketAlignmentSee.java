package lotr.common.network;

import java.util.HashMap;
import java.util.Map;
import com.google.common.base.Charsets;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.LOTRMod;
import lotr.common.LOTRPlayerData;
import lotr.common.fac.LOTRFaction;

public class LOTRPacketAlignmentSee implements IMessage {
    private String username;
    private Map<LOTRFaction, Float> alignmentMap = new HashMap<LOTRFaction, Float>();

    public LOTRPacketAlignmentSee() {
    }

    public LOTRPacketAlignmentSee(String name, LOTRPlayerData pd) {
        this.username = name;
        for(LOTRFaction f : LOTRFaction.getPlayableAlignmentFactions()) {
            float al = pd.getAlignment(f);
            this.alignmentMap.put(f, Float.valueOf(al));
        }
    }

    @Override
    public void toBytes(ByteBuf data) {
        byte[] nameBytes = this.username.getBytes(Charsets.UTF_8);
        data.writeByte(nameBytes.length);
        data.writeBytes(nameBytes);
        for(Map.Entry<LOTRFaction, Float> entry : this.alignmentMap.entrySet()) {
            LOTRFaction f = entry.getKey();
            float alignment = entry.getValue().floatValue();
            data.writeByte(f.ordinal());
            data.writeFloat(alignment);
        }
        data.writeByte(-1);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        byte length = data.readByte();
        ByteBuf nameBytes = data.readBytes(length);
        this.username = nameBytes.toString(Charsets.UTF_8);
        byte factionID = 0;
        while((factionID = data.readByte()) >= 0) {
            LOTRFaction f = LOTRFaction.forID(factionID);
            float alignment = data.readFloat();
            this.alignmentMap.put(f, Float.valueOf(alignment));
        }
    }

    public static class Handler implements IMessageHandler<LOTRPacketAlignmentSee, IMessage> {
        @Override
        public IMessage onMessage(LOTRPacketAlignmentSee packet, MessageContext context) {
            LOTRMod.proxy.displayAlignmentSee(packet.username, packet.alignmentMap);
            return null;
        }
    }

}
