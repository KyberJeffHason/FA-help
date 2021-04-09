package lotr.common.network;

import java.util.UUID;
import com.google.common.base.Charsets;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.LOTRMod;
import lotr.common.entity.npc.LOTREntityNPC;
import net.minecraft.entity.Entity;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class LOTRPacketHiredInfo implements IMessage {
    private int entityID;
    public boolean isHired;
    public UUID hiringPlayer;
    public String squadron;

    public LOTRPacketHiredInfo() {
    }

    public LOTRPacketHiredInfo(int i, UUID player, String sq) {
        this.entityID = i;
        this.hiringPlayer = player;
        this.isHired = this.hiringPlayer != null;
        this.squadron = sq;
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeInt(this.entityID);
        data.writeBoolean(this.isHired);
        if(this.isHired) {
            data.writeLong(this.hiringPlayer.getMostSignificantBits());
            data.writeLong(this.hiringPlayer.getLeastSignificantBits());
        }
        if(StringUtils.isNullOrEmpty(this.squadron)) {
            data.writeShort(-1);
        }
        else {
            byte[] sqBytes = this.squadron.getBytes(Charsets.UTF_8);
            data.writeShort(sqBytes.length);
            data.writeBytes(sqBytes);
        }
    }

    @Override
    public void fromBytes(ByteBuf data) {
        this.entityID = data.readInt();
        this.isHired = data.readBoolean();
        this.hiringPlayer = this.isHired ? new UUID(data.readLong(), data.readLong()) : null;
        short sqLength = data.readShort();
        if(sqLength > -1) {
            this.squadron = data.readBytes(sqLength).toString(Charsets.UTF_8);
        }
    }

    public static class Handler implements IMessageHandler<LOTRPacketHiredInfo, IMessage> {
        @Override
        public IMessage onMessage(LOTRPacketHiredInfo packet, MessageContext context) {
            World world = LOTRMod.proxy.getClientWorld();
            Entity entity = world.getEntityByID(packet.entityID);
            if(entity instanceof LOTREntityNPC) {
                ((LOTREntityNPC) entity).hiredNPCInfo.receiveData(packet);
            }
            return null;
        }
    }

}
