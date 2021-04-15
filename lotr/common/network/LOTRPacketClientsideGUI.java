package lotr.common.network;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.LOTRMod;
import net.minecraft.entity.player.EntityPlayer;

public class LOTRPacketClientsideGUI implements IMessage {
	private int guiID;
	private int guiX;
	private int guiY;
	private int guiZ;

	public LOTRPacketClientsideGUI() {
	}

	public LOTRPacketClientsideGUI(int id, int x, int y, int z) {
		guiID = id;
		guiX = x;
		guiY = y;
		guiZ = z;
	}

	@Override
	public void toBytes(ByteBuf data) {
		data.writeInt(guiID);
		data.writeInt(guiX);
		data.writeInt(guiY);
		data.writeInt(guiZ);
	}

	@Override
	public void fromBytes(ByteBuf data) {
		guiID = data.readInt();
		guiX = data.readInt();
		guiY = data.readInt();
		guiZ = data.readInt();
	}

	public static class Handler implements IMessageHandler<LOTRPacketClientsideGUI, IMessage> {
		@Override
		public IMessage onMessage(LOTRPacketClientsideGUI packet, MessageContext context) {
			EntityPlayer entityplayer = LOTRMod.proxy.getClientPlayer();
			entityplayer.openGui(LOTRMod.instance, packet.guiID, entityplayer.worldObj, packet.guiX, packet.guiY, packet.guiZ);
			return null;
		}
	}

}