package lotr.common.entity.item;

import java.util.*;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import lotr.common.*;
import lotr.common.fellowship.*;
import lotr.common.item.LOTRItemBanner;
import lotr.common.network.*;
import lotr.common.util.LOTRLog;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.util.*;
import net.minecraft.world.*;

public class LOTREntityBanner extends Entity {
	private NBTTagCompound protectData;
	private boolean wasEverProtecting = false;
	private boolean playerSpecificProtection;
	private boolean structureProtection = false;
	private int customRange;
	private boolean selfProtection = true;
	public static float ALIGNMENT_PROTECTION_MIN = 1.0f;
	public static float ALIGNMENT_PROTECTION_MAX = 10000.0f;
	private float alignmentProtection = ALIGNMENT_PROTECTION_MIN;
	public static int WHITELIST_DEFAULT = 16;
	public static int WHITELIST_MIN = 1;
	public static int WHITELIST_MAX = 4000;
	private LOTRBannerWhitelistEntry[] allowedPlayers = new LOTRBannerWhitelistEntry[WHITELIST_DEFAULT];
	private Set<LOTRBannerProtection.Permission> defaultPermissions = new HashSet<>();

	public LOTREntityBanner(World world) {
		super(world);
		setSize(1.0f, 3.0f);
	}

	@Override
	protected void entityInit() {
		dataWatcher.addObject(18, (byte) 0);
	}

	private int getBannerTypeID() {
		return dataWatcher.getWatchableObjectByte(18);
	}

	private void setBannerTypeID(int i) {
		dataWatcher.updateObject(18, (byte) i);
	}

	public void setBannerType(LOTRItemBanner.BannerType type) {
		setBannerTypeID(type.bannerID);
	}

	public LOTRItemBanner.BannerType getBannerType() {
		return LOTRItemBanner.BannerType.forID(getBannerTypeID());
	}

	public int getProtectionRange() {
		if (!structureProtection && !LOTRConfig.allowBannerProtection) {
			return 0;
		}
		if (customRange > 0) {
			return customRange;
		}
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(boundingBox.minY);
		int k = MathHelper.floor_double(posZ);
		Block block = worldObj.getBlock(i, j - 1, k);
		int meta = worldObj.getBlockMetadata(i, j - 1, k);
		return LOTRBannerProtection.getProtectionRange(block, meta);
	}

	public boolean isProtectingTerritory() {
		return getProtectionRange() > 0;
	}

	public AxisAlignedBB createProtectionCube() {
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(boundingBox.minY);
		int k = MathHelper.floor_double(posZ);
		int range = getProtectionRange();
		return AxisAlignedBB.getBoundingBox(i, j, k, i + 1, j + 1, k + 1).expand(range, range, range);
	}

	public boolean isPlayerSpecificProtection() {
		return playerSpecificProtection;
	}

	public void setPlayerSpecificProtection(boolean flag) {
		playerSpecificProtection = flag;
		if (!worldObj.isRemote) {
			updateForAllWatchers(worldObj);
		}
	}

	public boolean isSelfProtection() {
		if (!LOTRConfig.allowSelfProtectingBanners) {
			return false;
		}
		return selfProtection;
	}

	public void setSelfProtection(boolean flag) {
		selfProtection = flag;
		if (!worldObj.isRemote) {
			updateForAllWatchers(worldObj);
		}
	}

	public boolean isStructureProtection() {
		return structureProtection;
	}

	public void setStructureProtection(boolean flag) {
		structureProtection = flag;
		if (!worldObj.isRemote) {
			updateForAllWatchers(worldObj);
		}
	}

	public void setCustomRange(int i) {
		customRange = MathHelper.clamp_int(i, 0, 64);
		if (!worldObj.isRemote) {
			updateForAllWatchers(worldObj);
		}
	}

	public float getAlignmentProtection() {
		return alignmentProtection;
	}

	public void setAlignmentProtection(float f) {
		alignmentProtection = MathHelper.clamp_float(f, ALIGNMENT_PROTECTION_MIN, ALIGNMENT_PROTECTION_MAX);
		if (!worldObj.isRemote) {
			updateForAllWatchers(worldObj);
		}
	}

	public void setPlacingPlayer(EntityPlayer player) {
		whitelistPlayer(0, player.getGameProfile());
	}

	public GameProfile getPlacingPlayer() {
		return getWhitelistedPlayer(0);
	}

	public GameProfile getWhitelistedPlayer(int index) {
		if (allowedPlayers[index] == null) {
			return null;
		}
		return allowedPlayers[index].profile;
	}

	public LOTRBannerWhitelistEntry getWhitelistEntry(int index) {
		return allowedPlayers[index];
	}

	public void whitelistPlayer(int index, GameProfile profile) {
		if (index < 0 || index >= allowedPlayers.length) {
			return;
		}
		allowedPlayers[index] = profile == null ? null : new LOTRBannerWhitelistEntry(profile).setFullPerms();
		if (!worldObj.isRemote) {
			updateForAllWatchers(worldObj);
		}
	}

	public void whitelistFellowship(int index, LOTRFellowship fs) {
		if (isValidFellowship(fs)) {
			whitelistPlayer(index, new LOTRFellowshipProfile(this, fs.getFellowshipID(), ""));
		}
	}

	public LOTRFellowship getPlacersFellowshipByName(String fsName) {
		UUID ownerID;
		GameProfile owner = getPlacingPlayer();
		if (owner != null && (ownerID = owner.getId()) != null) {
			return LOTRLevelData.getData(ownerID).getFellowshipByName(fsName);
		}
		return null;
	}

	public boolean hasDefaultPermission(LOTRBannerProtection.Permission p) {
		return defaultPermissions.contains(p);
	}

	public void addDefaultPermission(LOTRBannerProtection.Permission p) {
		if (p == LOTRBannerProtection.Permission.FULL) {
			return;
		}
		defaultPermissions.add(p);
		if (!worldObj.isRemote) {
			updateForAllWatchers(worldObj);
		}
	}

	public void removeDefaultPermission(LOTRBannerProtection.Permission p) {
		defaultPermissions.remove(p);
		if (!worldObj.isRemote) {
			updateForAllWatchers(worldObj);
		}
	}

	public int getDefaultPermBitFlags() {
		return LOTRBannerWhitelistEntry.static_encodePermBitFlags(defaultPermissions);
	}

	public void setDefaultPermissions(Collection<LOTRBannerProtection.Permission> perms) {
		defaultPermissions.clear();
		for (LOTRBannerProtection.Permission p : perms) {
			if (p == LOTRBannerProtection.Permission.FULL) {
				continue;
			}
			defaultPermissions.add(p);
		}
		if (!worldObj.isRemote) {
			updateForAllWatchers(worldObj);
		}
	}

	public boolean isPlayerWhitelisted(EntityPlayer entityplayer, LOTRBannerProtection.Permission perm) {
		if (playerSpecificProtection) {
			if (hasDefaultPermission(perm)) {
				return true;
			}
			GameProfile playerProfile = entityplayer.getGameProfile();
			if (playerProfile != null && playerProfile.getId() != null) {
				String playerName = playerProfile.getName();
				UUID playerID = playerProfile.getId();
				for (LOTRBannerWhitelistEntry entry : allowedPlayers) {
					if (entry == null) {
						continue;
					}
					GameProfile profile = entry.profile;
					boolean playerMatch = false;
					if (profile instanceof LOTRFellowshipProfile) {
						Object fs;
						LOTRFellowshipProfile fsPro = (LOTRFellowshipProfile) profile;
						if (!worldObj.isRemote) {
							fs = fsPro.getFellowship();
							if (fs != null && ((LOTRFellowship) fs).containsPlayer(playerID)) {
								playerMatch = true;
							}
						} else {
							fs = fsPro.getFellowshipClient();
							if (fs != null && ((LOTRFellowshipClient) fs).isPlayerIn(playerName)) {
								playerMatch = true;
							}
						}
					} else if (profile.getId() != null && profile.getId().equals(playerID)) {
						playerMatch = true;
					}
					if (!playerMatch || !entry.allowsPermission(perm)) {
						continue;
					}
					return true;
				}
			}
		}
		return false;
	}

	public int getWhitelistLength() {
		return allowedPlayers.length;
	}

	public void resizeWhitelist(int length) {
		if ((length = MathHelper.clamp_int(length, WHITELIST_MIN, WHITELIST_MAX)) == allowedPlayers.length) {
			return;
		}
		LOTRBannerWhitelistEntry[] resized = new LOTRBannerWhitelistEntry[length];
		for (int i = 0; i < length; ++i) {
			if (i >= allowedPlayers.length) {
				continue;
			}
			resized[i] = allowedPlayers[i];
		}
		allowedPlayers = resized;
		if (!worldObj.isRemote) {
			updateForAllWatchers(worldObj);
		}
	}

	public boolean isPlayerAllowedByFaction(EntityPlayer entityplayer, LOTRBannerProtection.Permission perm) {
		if (!playerSpecificProtection) {
			if (hasDefaultPermission(perm)) {
				return true;
			}
			float alignment = LOTRLevelData.getData(entityplayer).getAlignment(getBannerType().faction);
			if (alignment >= getAlignmentProtection()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return null;
	}

	@Override
	public void onUpdate() {
		boolean onSolidBlock;
		super.onUpdate();
		boolean protecting = isProtectingTerritory();
		if (!worldObj.isRemote && protecting) {
			wasEverProtecting = true;
		}
		if (!worldObj.isRemote && getPlacingPlayer() == null && playerSpecificProtection) {
			playerSpecificProtection = false;
		}
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		func_145771_j(posX, (boundingBox.minY + boundingBox.maxY) / 2.0, posZ);
		motionZ = 0.0;
		motionX = 0.0;
		motionY = 0.0;
		moveEntity(motionX, motionY, motionZ);
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(boundingBox.minY);
		int k = MathHelper.floor_double(posZ);
		onSolidBlock = World.doesBlockHaveSolidTopSurface(worldObj, i, j - 1, k) && boundingBox.minY == MathHelper.ceiling_double_int(boundingBox.minY);
		if (!worldObj.isRemote && !onSolidBlock) {
			dropAsItem(true);
		}
		ignoreFrustumCheck = protecting;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setByte("BannerType", (byte) getBannerTypeID());
		if (protectData == null && wasEverProtecting) {
			protectData = new NBTTagCompound();
		}
		if (protectData != null) {
			writeProtectionToNBT(protectData);
			nbt.setTag("ProtectData", protectData);
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		setBannerTypeID(nbt.getByte("BannerType"));
		if (nbt.hasKey("PlayerProtection")) {
			readProtectionFromNBT(nbt);
			protectData = new NBTTagCompound();
			writeProtectionToNBT(protectData);
		} else if (nbt.hasKey("ProtectData")) {
			readProtectionFromNBT(nbt.getCompoundTag("ProtectData"));
		}
	}

	public final void writeProtectionToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("PlayerProtection", playerSpecificProtection);
		nbt.setBoolean("StructureProtection", structureProtection);
		nbt.setShort("CustomRange", (short) customRange);
		nbt.setBoolean("SelfProtection", selfProtection);
		nbt.setFloat("AlignProtectF", alignmentProtection);
		nbt.setInteger("WhitelistLength", allowedPlayers.length);
		NBTTagList allowedPlayersTags = new NBTTagList();
		for (int i = 0; i < allowedPlayers.length; ++i) {
			GameProfile profile;
			LOTRBannerWhitelistEntry entry = allowedPlayers[i];
			if (entry == null || (profile = entry.profile) == null) {
				continue;
			}
			NBTTagCompound playerData = new NBTTagCompound();
			playerData.setInteger("Index", i);
			boolean isFellowship = profile instanceof LOTRFellowshipProfile;
			playerData.setBoolean("Fellowship", isFellowship);
			if (isFellowship) {
				LOTRFellowship fs = ((LOTRFellowshipProfile) profile).getFellowship();
				if (fs != null) {
					playerData.setString("FellowshipID", fs.getFellowshipID().toString());
				}
			} else {
				NBTTagCompound profileData = new NBTTagCompound();
				NBTUtil.func_152460_a(profileData, profile);
				playerData.setTag("Profile", profileData);
			}
			NBTTagList permTags = new NBTTagList();
			for (LOTRBannerProtection.Permission p : entry.listPermissions()) {
				String pName = p.codeName;
				permTags.appendTag(new NBTTagString(pName));
			}
			playerData.setTag("Perms", permTags);
			playerData.setBoolean("PermsSaved", true);
			allowedPlayersTags.appendTag(playerData);
		}
		nbt.setTag("AllowedPlayers", allowedPlayersTags);
		if (!defaultPermissions.isEmpty()) {
			NBTTagList permTags = new NBTTagList();
			for (LOTRBannerProtection.Permission p : defaultPermissions) {
				String pName = p.codeName;
				permTags.appendTag(new NBTTagString(pName));
			}
			nbt.setTag("DefaultPerms", permTags);
		}
	}

	public final void readProtectionFromNBT(NBTTagCompound nbt) {
		protectData = (NBTTagCompound) nbt.copy();
		playerSpecificProtection = nbt.getBoolean("PlayerProtection");
		structureProtection = nbt.getBoolean("StructureProtection");
		customRange = nbt.getShort("CustomRange");
		customRange = MathHelper.clamp_int(customRange, 0, 64);
		selfProtection = nbt.hasKey("SelfProtection") ? nbt.getBoolean("SelfProtection") : true;
		if (nbt.hasKey("AlignmentProtection")) {
			setAlignmentProtection(nbt.getInteger("AlignmentProtection"));
		} else {
			setAlignmentProtection(nbt.getFloat("AlignProtectF"));
		}
		int wlength = WHITELIST_DEFAULT;
		if (nbt.hasKey("WhitelistLength")) {
			wlength = nbt.getInteger("WhitelistLength");
		}
		allowedPlayers = new LOTRBannerWhitelistEntry[wlength];
		NBTTagList allowedPlayersTags = nbt.getTagList("AllowedPlayers", 10);
		for (int i = 0; i < allowedPlayersTags.tagCount(); ++i) {
			LOTRBannerWhitelistEntry entry;
			NBTTagCompound playerData = allowedPlayersTags.getCompoundTagAt(i);
			int index = playerData.getInteger("Index");
			if (index < 0 || index >= wlength) {
				continue;
			}
			GameProfile profile = null;
			boolean isFellowship = playerData.getBoolean("Fellowship");
			if (isFellowship) {
				LOTRFellowshipProfile pr;
				UUID fsID;
				if (playerData.hasKey("FellowshipID") && (fsID = UUID.fromString(playerData.getString("FellowshipID"))) != null && (pr = new LOTRFellowshipProfile(this, fsID, "")).getFellowship() != null) {
					profile = pr;
				}
			} else if (playerData.hasKey("Profile")) {
				NBTTagCompound profileData = playerData.getCompoundTag("Profile");
				profile = NBTUtil.func_152459_a(profileData);
			}
			if (profile == null) {
				continue;
			}
			allowedPlayers[i] = entry = new LOTRBannerWhitelistEntry(profile);
			boolean savedWithPerms = playerData.getBoolean("PermsSaved");
			if (savedWithPerms) {
				if (!playerData.hasKey("Perms", 9)) {
					continue;
				}
				NBTTagList permTags = playerData.getTagList("Perms", 8);
				for (int p = 0; p < permTags.tagCount(); ++p) {
					String pName = permTags.getStringTagAt(p);
					LOTRBannerProtection.Permission perm = LOTRBannerProtection.Permission.forName(pName);
					if (perm == null) {
						continue;
					}
					entry.addPermission(perm);
				}
				continue;
			}
			entry.setFullPerms();
		}
		validateWhitelistedFellowships();
		defaultPermissions.clear();
		if (nbt.hasKey("DefaultPerms")) {
			NBTTagList permTags = nbt.getTagList("DefaultPerms", 8);
			for (int p = 0; p < permTags.tagCount(); ++p) {
				String pName = permTags.getStringTagAt(p);
				LOTRBannerProtection.Permission perm = LOTRBannerProtection.Permission.forName(pName);
				if (perm == null) {
					continue;
				}
				defaultPermissions.add(perm);
			}
		}
	}

	private boolean isValidFellowship(LOTRFellowship fs) {
		GameProfile owner = getPlacingPlayer();
		return fs != null && !fs.isDisbanded() && owner != null && owner.getId() != null && fs.containsPlayer(owner.getId());
	}

	private void validateWhitelistedFellowships() {
		getPlacingPlayer();
		for (int i = 0; i < allowedPlayers.length; ++i) {
			GameProfile profile = getWhitelistedPlayer(i);
			if (!(profile instanceof LOTRFellowshipProfile) || isValidFellowship(((LOTRFellowshipProfile) profile).getFellowship())) {
				continue;
			}
			allowedPlayers[i] = null;
		}
	}

	public boolean canPlayerEditBanner(EntityPlayer entityplayer) {
		GameProfile owner = getPlacingPlayer();
		if (owner != null && owner.getId() != null && entityplayer.getUniqueID().equals(owner.getId())) {
			return true;
		}
		return !isStructureProtection() && MinecraftServer.getServer().getConfigurationManager().func_152596_g(entityplayer.getGameProfile()) && entityplayer.capabilities.isCreativeMode;
	}

	@Override
	public boolean interactFirst(EntityPlayer entityplayer) {
		if (!worldObj.isRemote && isProtectingTerritory() && canPlayerEditBanner(entityplayer)) {
			sendBannerToPlayer(entityplayer, true, true);
		}
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource damagesource, float f) {
		boolean isProtectionBanner = isProtectingTerritory();
		boolean isPlayerDamage = damagesource.getEntity() instanceof EntityPlayer;
		if (isProtectionBanner && !isPlayerDamage) {
			return false;
		}
		if (!isDead && !worldObj.isRemote) {
			if (isPlayerDamage) {
				EntityPlayer entityplayer = (EntityPlayer) damagesource.getEntity();
				if (LOTRBannerProtection.isProtected(worldObj, this, LOTRBannerProtection.forPlayer(entityplayer, LOTRBannerProtection.Permission.FULL), true)) {
					if (isProtectionBanner) {
						if (selfProtection) {
							return false;
						}
						if (structureProtection && damagesource.getEntity() != damagesource.getSourceOfDamage()) {
							return false;
						}
					} else {
						return false;
					}
				}
				if (isProtectionBanner && selfProtection && !canPlayerEditBanner(entityplayer)) {
					return false;
				}
			}
			setBeenAttacked();
			worldObj.playSoundAtEntity(this, Blocks.planks.stepSound.getBreakSound(), (Blocks.planks.stepSound.getVolume() + 1.0f) / 2.0f, Blocks.planks.stepSound.getPitch() * 0.8f);
			boolean drop = true;
			if (damagesource.getEntity() instanceof EntityPlayer && ((EntityPlayer) damagesource.getEntity()).capabilities.isCreativeMode) {
				drop = false;
			}
			dropAsItem(drop);
		}
		return true;
	}

	private void dropAsItem(boolean drop) {
		setDead();
		if (drop) {
			entityDropItem(getBannerItem(), 0.0f);
		}
	}

	@Override
	public ItemStack getPickedResult(MovingObjectPosition target) {
		return getBannerItem();
	}

	private ItemStack getBannerItem() {
		ItemStack item = new ItemStack(LOTRMod.banner, 1, getBannerType().bannerID);
		if (wasEverProtecting && protectData == null) {
			protectData = new NBTTagCompound();
		}
		if (protectData != null) {
			writeProtectionToNBT(protectData);
			if (!structureProtection) {
				LOTRItemBanner.setProtectionData(item, protectData);
			}
		}
		return item;
	}

	public void sendBannerToPlayer(EntityPlayer entityplayer, boolean sendWhitelist, boolean openGui) {
		sendBannerData(entityplayer, sendWhitelist, openGui);
	}

	private void updateForAllWatchers(World world) {
		int x = MathHelper.floor_double(posX) >> 4;
		int z = MathHelper.floor_double(posZ) >> 4;
		PlayerManager playermanager = ((WorldServer) worldObj).getPlayerManager();
		List players = worldObj.playerEntities;
		for (Object obj : players) {
			EntityPlayerMP entityplayer = (EntityPlayerMP) obj;
			if (!playermanager.isPlayerWatchingChunk(entityplayer, x, z)) {
				continue;
			}
			sendBannerData(entityplayer, false, false);
		}
	}

	private void sendBannerData(EntityPlayer entityplayer, boolean sendWhitelist, boolean openGui) {
		LOTRPacketBannerData packet = new LOTRPacketBannerData(getEntityId(), openGui);
		packet.playerSpecificProtection = playerSpecificProtection;
		packet.selfProtection = selfProtection;
		packet.structureProtection = structureProtection;
		packet.customRange = customRange;
		packet.alignmentProtection = getAlignmentProtection();
		packet.whitelistLength = getWhitelistLength();
		int maxSendIndex = sendWhitelist ? allowedPlayers.length : 1;
		String[] whitelistSlots = new String[maxSendIndex];
		int[] whitelistPerms = new int[maxSendIndex];
		for (int index = 0; index < maxSendIndex; ++index) {
			LOTRBannerWhitelistEntry entry = allowedPlayers[index];
			if (entry == null) {
				whitelistSlots[index] = null;
				continue;
			}
			GameProfile profile = entry.profile;
			if (profile == null) {
				whitelistSlots[index] = null;
				continue;
			}
			if (profile instanceof LOTRFellowshipProfile) {
				LOTRFellowshipProfile fsProfile = (LOTRFellowshipProfile) profile;
				LOTRFellowship fs = fsProfile.getFellowship();
				if (isValidFellowship(fs)) {
					whitelistSlots[index] = LOTRFellowshipProfile.addFellowshipCode(fs.getName());
				}
			} else {
				String username;
				if (StringUtils.isNullOrEmpty(profile.getName())) {
					MinecraftServer.getServer().func_147130_as().fillProfileProperties(profile, true);
				}
				if (StringUtils.isNullOrEmpty(username = profile.getName())) {
					whitelistSlots[index] = null;
					if (index == 0) {
						LOTRLog.logger.info("LOTR: Banner needs to be replaced at " + MathHelper.floor_double(posX) + " " + MathHelper.floor_double(posY) + " " + MathHelper.floor_double(posZ) + " dim_" + dimension);
					}
				} else {
					whitelistSlots[index] = username;
				}
			}
			if (whitelistSlots[index] == null) {
				continue;
			}
			whitelistPerms[index] = entry.encodePermBitFlags();
		}
		packet.whitelistSlots = whitelistSlots;
		packet.whitelistPerms = whitelistPerms;
		packet.defaultPerms = getDefaultPermBitFlags();
		LOTRPacketHandler.networkWrapper.sendTo((IMessage) packet, (EntityPlayerMP) entityplayer);
	}
}