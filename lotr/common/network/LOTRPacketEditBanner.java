package lotr.common.network;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.entity.item.LOTREntityBanner;
import lotr.common.fellowship.LOTRFellowship;
import lotr.common.fellowship.LOTRFellowshipProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class LOTRPacketEditBanner implements IMessage {
    private int bannerID;
    public boolean playerSpecificProtection;
    public boolean selfProtection;
    public float alignmentProtection;
    public int whitelistLength;
    public String[] whitelistSlots;

    public LOTRPacketEditBanner() {
    }

    public LOTRPacketEditBanner(LOTREntityBanner banner) {
        this.bannerID = banner.getEntityId();
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeInt(this.bannerID);
        data.writeBoolean(this.playerSpecificProtection);
        data.writeBoolean(this.selfProtection);
        data.writeFloat(this.alignmentProtection);
        data.writeShort(this.whitelistLength);
        boolean sendUsernames = this.whitelistSlots != null;
        data.writeBoolean(sendUsernames);
        if(sendUsernames) {
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
    }

    @Override
    public void fromBytes(ByteBuf data) {
        this.bannerID = data.readInt();
        this.playerSpecificProtection = data.readBoolean();
        this.selfProtection = data.readBoolean();
        this.alignmentProtection = data.readFloat();
        this.whitelistLength = data.readShort();
        boolean sendUsernames = data.readBoolean();
        if(sendUsernames) {
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
    }

    public static class Handler implements IMessageHandler<LOTRPacketEditBanner, IMessage> {
        @Override
        public IMessage onMessage(LOTRPacketEditBanner packet, MessageContext context) {
            LOTREntityBanner banner;
            EntityPlayerMP entityplayer = context.getServerHandler().playerEntity;
            World world = entityplayer.worldObj;
            Entity bEntity = world.getEntityByID(packet.bannerID);
            if(bEntity instanceof LOTREntityBanner && (banner = (LOTREntityBanner) bEntity).canPlayerEditBanner(entityplayer)) {
                banner.setPlayerSpecificProtection(packet.playerSpecificProtection);
                banner.setSelfProtection(packet.selfProtection);
                banner.setAlignmentProtection(packet.alignmentProtection);
                banner.resizeWhitelist(packet.whitelistLength);
                if(packet.whitelistSlots != null) {
                    for(int index = 0; index < packet.whitelistSlots.length; ++index) {
                        if(index == 0) continue;
                        String username = packet.whitelistSlots[index];
                        if(StringUtils.isNullOrEmpty(username)) {
                            banner.whitelistPlayer(index, null);
                            continue;
                        }
                        if(LOTRFellowshipProfile.hasFellowshipCode(username)) {
                            String fsName = LOTRFellowshipProfile.stripFellowshipCode(username);
                            LOTRFellowship fellowship = banner.getPlacersFellowshipByName(fsName);
                            if(fellowship == null) continue;
                            banner.whitelistFellowship(index, fellowship);
                            continue;
                        }
                        GameProfile profile = MinecraftServer.getServer().func_152358_ax().func_152655_a(username);
                        if(profile == null) continue;
                        banner.whitelistPlayer(index, profile);
                    }
                }
            }
            return null;
        }
    }

}
