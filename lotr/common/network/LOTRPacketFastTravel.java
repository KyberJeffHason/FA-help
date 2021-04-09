package lotr.common.network;

import java.util.UUID;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.*;
import lotr.common.world.map.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;

public class LOTRPacketFastTravel implements IMessage {
    private boolean isCustom;
    private int wpID;
    private UUID sharingPlayer;

    public LOTRPacketFastTravel() {
    }

    public LOTRPacketFastTravel(LOTRAbstractWaypoint wp) {
        this.isCustom = wp instanceof LOTRCustomWaypoint;
        this.wpID = wp.getID();
        if(wp instanceof LOTRCustomWaypoint) {
            this.sharingPlayer = ((LOTRCustomWaypoint) wp).getSharingPlayerID();
        }
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeBoolean(this.isCustom);
        data.writeInt(this.wpID);
        boolean shared = this.sharingPlayer != null;
        data.writeBoolean(shared);
        if(shared) {
            data.writeLong(this.sharingPlayer.getMostSignificantBits());
            data.writeLong(this.sharingPlayer.getLeastSignificantBits());
        }
    }

    @Override
    public void fromBytes(ByteBuf data) {
        this.isCustom = data.readBoolean();
        this.wpID = data.readInt();
        boolean shared = data.readBoolean();
        if(shared) {
            this.sharingPlayer = new UUID(data.readLong(), data.readLong());
        }
    }

    public static class Handler implements IMessageHandler<LOTRPacketFastTravel, IMessage> {
        @Override
        public IMessage onMessage(LOTRPacketFastTravel packet, MessageContext context) {
            EntityPlayerMP entityplayer = context.getServerHandler().playerEntity;
            if(!LOTRConfig.enableFastTravel) {
                entityplayer.addChatMessage(new ChatComponentTranslation("chat.lotr.ftDisabled", new Object[0]));
            }
            else {
                LOTRPlayerData playerData = LOTRLevelData.getData(entityplayer);
                if(playerData.getFTTimer() <= 0) {
                    boolean isCustom = packet.isCustom;
                    int waypointID = packet.wpID;
                    LOTRAbstractWaypoint waypoint = null;
                    if(!isCustom) {
                        if(waypointID >= 0 && waypointID < LOTRWaypoint.values().length) {
                            waypoint = LOTRWaypoint.values()[waypointID];
                        }
                    }
                    else {
                        UUID sharingPlayer = packet.sharingPlayer;
                        waypoint = sharingPlayer != null ? playerData.getSharedCustomWaypointByID(sharingPlayer, waypointID) : playerData.getCustomWaypointByID(waypointID);
                    }
                    if(waypoint != null && waypoint.hasPlayerUnlocked(entityplayer)) {
                        boolean canTravel = playerData.canFastTravel();
                        if(!canTravel) {
                            entityplayer.closeScreen();
                            entityplayer.addChatMessage(new ChatComponentTranslation("lotr.fastTravel.underAttack", new Object[0]));
                        }
                        else {
                            playerData.setTargetFTWaypoint(waypoint);
                        }
                    }
                }
            }
            return null;
        }
    }

}
