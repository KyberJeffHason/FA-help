package lotr.common.network;

import com.google.common.base.Charsets;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.LOTRMod;
import lotr.common.entity.item.LOTREntityBanner;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class LOTRPacketBannerInvalidName implements IMessage {
    private int entityID;
    private int slot;
    private String prevText;

    public LOTRPacketBannerInvalidName() {
    }

    public LOTRPacketBannerInvalidName(int id, int s, String pre) {
        this.entityID = id;
        this.slot = s;
        this.prevText = pre;
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeInt(this.entityID);
        data.writeInt(this.slot);
        byte[] textBytes = this.prevText.getBytes(Charsets.UTF_8);
        data.writeByte(textBytes.length);
        data.writeBytes(textBytes);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        this.entityID = data.readInt();
        this.slot = data.readInt();
        byte length = data.readByte();
        ByteBuf textBytes = data.readBytes(length);
        this.prevText = textBytes.toString(Charsets.UTF_8);
    }

    public static class Handler implements IMessageHandler<LOTRPacketBannerInvalidName, IMessage> {
        @Override
        public IMessage onMessage(LOTRPacketBannerInvalidName packet, MessageContext context) {
            World world = LOTRMod.proxy.getClientWorld();
            Entity entity = world.getEntityByID(packet.entityID);
            if(entity instanceof LOTREntityBanner) {
                LOTREntityBanner banner = (LOTREntityBanner) entity;
                LOTRMod.proxy.invalidateBannerUsername(banner, packet.slot, packet.prevText);
            }
            return null;
        }
    }

}
