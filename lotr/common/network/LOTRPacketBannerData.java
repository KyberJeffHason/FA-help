package lotr.common.network;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.LOTRMod;
import lotr.common.entity.item.LOTREntityBanner;
import lotr.common.fellowship.LOTRFellowshipProfile;
import net.minecraft.entity.Entity;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class LOTRPacketBannerData implements IMessage {
    private int entityID;
    private boolean openGui;
    public boolean playerSpecificProtection;
    public boolean selfProtection;
    public boolean structureProtection;
    public int customRange;
    public float alignmentProtection;
    public int whitelistLength;
    public String[] whitelistSlots;

    public LOTRPacketBannerData() {
    }

    public LOTRPacketBannerData(int id, boolean flag) {
        this.entityID = id;
        this.openGui = flag;
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeInt(this.entityID);
        data.writeBoolean(this.openGui);
        data.writeBoolean(this.playerSpecificProtection);
        data.writeBoolean(this.selfProtection);
        data.writeBoolean(this.structureProtection);
        data.writeShort(this.customRange);
        data.writeFloat(this.alignmentProtection);
        data.writeShort(this.whitelistLength);
        data.writeShort(this.whitelistSlots.length);
        for(int index = 0; index < this.whitelistSlots.length; ++index) {
            data.writeShort(index);
            String username = this.whitelistSlots[index];
            if(StringUtils.isNullOrEmpty(username)) {
                data.writeByte(-1);
                continue;
            }
            byte[] usernameBytes = username.getBytes(Charsets.UTF_8);
            data.writeByte(usernameBytes.length);
            data.writeBytes(usernameBytes);
        }
        data.writeShort(-1);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        this.entityID = data.readInt();
        this.openGui = data.readBoolean();
        this.playerSpecificProtection = data.readBoolean();
        this.selfProtection = data.readBoolean();
        this.structureProtection = data.readBoolean();
        this.customRange = data.readShort();
        this.alignmentProtection = data.readFloat();
        this.whitelistLength = data.readShort();
        this.whitelistSlots = new String[data.readShort()];
        short index = 0;
        while((index = data.readShort()) >= 0) {
            String name;
            byte length = data.readByte();
            if(length == -1) {
                this.whitelistSlots[index] = null;
                continue;
            }
            ByteBuf usernameBytes = data.readBytes(length);
            this.whitelistSlots[index] = name = usernameBytes.toString(Charsets.UTF_8);
        }
    }

    public static class Handler implements IMessageHandler<LOTRPacketBannerData, IMessage> {
        @Override
        public IMessage onMessage(LOTRPacketBannerData packet, MessageContext context) {
            World world = LOTRMod.proxy.getClientWorld();
            Entity entity = world.getEntityByID(packet.entityID);
            if(entity instanceof LOTREntityBanner) {
                LOTREntityBanner banner = (LOTREntityBanner) entity;
                banner.setPlayerSpecificProtection(packet.playerSpecificProtection);
                banner.setSelfProtection(packet.selfProtection);
                banner.setStructureProtection(packet.structureProtection);
                banner.setCustomRange(packet.customRange);
                banner.setAlignmentProtection(packet.alignmentProtection);
                banner.resizeWhitelist(packet.whitelistLength);
                for(int index = 0; index < packet.whitelistSlots.length; ++index) {
                    String username = packet.whitelistSlots[index];
                    if(StringUtils.isNullOrEmpty(username)) {
                        banner.whitelistPlayer(index, null);
                        continue;
                    }
                    if(LOTRFellowshipProfile.hasFellowshipCode(username)) {
                        String fsName = LOTRFellowshipProfile.stripFellowshipCode(username);
                        LOTRFellowshipProfile profile = new LOTRFellowshipProfile(banner, null, fsName);
                        banner.whitelistPlayer(index, profile);
                        continue;
                    }
                    GameProfile profile = new GameProfile(null, username);
                    banner.whitelistPlayer(index, profile);
                }
                if(packet.openGui) {
                    LOTRMod.proxy.displayBannerGui(banner);
                }
            }
            return null;
        }
    }

}
