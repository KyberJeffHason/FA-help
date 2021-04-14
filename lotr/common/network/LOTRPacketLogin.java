
package lotr.common.network;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import lotr.common.*;
import net.minecraft.world.EnumDifficulty;

public class LOTRPacketLogin implements IMessage {
	public int ringPortalX;
	public int ringPortalY;
	public int ringPortalZ;
	public int ftCooldownMax;
	public int ftCooldownMin;
	public EnumDifficulty difficulty;
	public boolean difficultyLocked;
	public boolean alignmentZones;
	public boolean feastMode;
	public boolean fellowshipCreation;
	public boolean enchanting;
	public boolean enchantingLOTR;
	public boolean conquestDecay;

	@Override
	public void toBytes(ByteBuf data) {
		data.writeInt(ringPortalX);
		data.writeInt(ringPortalY);
		data.writeInt(ringPortalZ);
		data.writeInt(ftCooldownMax);
		data.writeInt(ftCooldownMin);
		int diff = difficulty == null ? -1 : difficulty.getDifficultyId();
		data.writeByte(diff);
		data.writeBoolean(difficultyLocked);
		data.writeBoolean(alignmentZones);
		data.writeBoolean(feastMode);
		data.writeBoolean(fellowshipCreation);
		data.writeBoolean(enchanting);
		data.writeBoolean(enchantingLOTR);
		data.writeBoolean(conquestDecay);
	}

	@Override
	public void fromBytes(ByteBuf data) {
		ringPortalX = data.readInt();
		ringPortalY = data.readInt();
		ringPortalZ = data.readInt();
		ftCooldownMax = data.readInt();
		ftCooldownMin = data.readInt();
		byte diff = data.readByte();
		difficulty = diff >= 0 ? EnumDifficulty.getDifficultyEnum(diff) : null;
		difficultyLocked = data.readBoolean();
		alignmentZones = data.readBoolean();
		feastMode = data.readBoolean();
		fellowshipCreation = data.readBoolean();
		enchanting = data.readBoolean();
		enchantingLOTR = data.readBoolean();
		conquestDecay = data.readBoolean();
	}

	public static class Handler implements IMessageHandler<LOTRPacketLogin, IMessage> {
		@Override
		public IMessage onMessage(LOTRPacketLogin packet, MessageContext context) {
			if (!LOTRMod.proxy.isSingleplayer()) {
				LOTRLevelData.destroyAllPlayerData();
			}
			LOTRLevelData.middleEarthPortalX = packet.ringPortalX;
			LOTRLevelData.middleEarthPortalY = packet.ringPortalY;
			LOTRLevelData.middleEarthPortalZ = packet.ringPortalZ;
			LOTRLevelData.setFTCooldown(packet.ftCooldownMax, packet.ftCooldownMin);
			EnumDifficulty diff = packet.difficulty;
			if (diff != null) {
				LOTRLevelData.setSavedDifficulty(diff);
				LOTRMod.proxy.setClientDifficulty(diff);
			} else {
				LOTRLevelData.setSavedDifficulty(null);
			}
			LOTRLevelData.setDifficultyLocked(packet.difficultyLocked);
			LOTRLevelData.setEnableAlignmentZones(packet.alignmentZones);
			LOTRLevelData.clientside_thisServer_feastMode = packet.feastMode;
			LOTRLevelData.clientside_thisServer_enchanting = packet.enchanting;
			LOTRLevelData.clientside_thisServer_enchantingLOTR = packet.enchantingLOTR;
			return null;
		}
	}

}
