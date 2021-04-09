package lotr.common;

import java.io.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLLog;
import lotr.common.fellowship.LOTRFellowship;
import lotr.common.fellowship.LOTRFellowshipData;
import lotr.common.network.*;
import lotr.common.world.spawning.LOTREventSpawner;
import lotr.common.world.spawning.LOTRTravellingTraderSpawner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.world.*;
import net.minecraftforge.common.DimensionManager;

public class LOTRLevelData {
    private static final int FAST_TRAVEL_COOLDOWN_DEFAULT = 36000;
    private static final int FAST_TRAVEL_COOLDOWN_MIN_DEFAULT = 3600;
    public static int madePortal;
    public static int madeMiddleEarthPortal;
    public static int overworldPortalX;
    public static int overworldPortalY;
    public static int overworldPortalZ;
    public static int middleEarthPortalX;
    public static int middleEarthPortalY;
    public static int middleEarthPortalZ;
    private static int structuresBanned;
    private static int ftCooldownMax;
    private static int ftCooldownMin;
    private static boolean gollumSpawned;
    private static boolean enableAlignmentZones;
    private static float conquestRate;
    public static boolean clientside_thisServer_feastMode;
    public static boolean clientside_thisServer_enchanting;
    public static boolean clientside_thisServer_enchantingLOTR;
    private static EnumDifficulty difficulty;
    private static boolean difficultyLock;
    private static Map<UUID, LOTRPlayerData> playerDataMap;
    public static boolean needsLoad;
    private static boolean needsSave;
    private static Random rand;

    public static void markDirty() {
        needsSave = true;
    }

    public static boolean anyDataNeedsSave() {
        if(needsSave) {
            return true;
        }
        if(LOTRSpawnDamping.needsSave) {
            return true;
        }
        for(LOTRPlayerData pd : playerDataMap.values()) {
            if(!pd.needsSave()) continue;
            return true;
        }
        return false;
    }

    public static File getOrCreateLOTRDir() {
        File file = new File(DimensionManager.getCurrentSaveRootDirectory(), "LOTR");
        if(!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private static File getLOTRDat() {
        return new File(LOTRLevelData.getOrCreateLOTRDir(), "LOTR.dat");
    }

    private static File getLOTRPlayerDat(UUID player) {
        File playerDir = new File(LOTRLevelData.getOrCreateLOTRDir(), "players");
        if(!playerDir.exists()) {
            playerDir.mkdirs();
        }
        return new File(playerDir, player.toString() + ".dat");
    }

    @SuppressWarnings("resource")
	public static NBTTagCompound loadNBTFromFile(File file) throws FileNotFoundException, IOException {
        if(file.exists()) {
            return CompressedStreamTools.readCompressed(new FileInputStream(file));
        }
        return new NBTTagCompound();
    }

    public static void saveNBTToFile(File file, NBTTagCompound nbt) throws FileNotFoundException, IOException {
        CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(file));
    }

    public static void save() {
        try {
            if(needsSave) {
                File LOTR_dat = LOTRLevelData.getLOTRDat();
                if(!LOTR_dat.exists()) {
                    LOTRLevelData.saveNBTToFile(LOTR_dat, new NBTTagCompound());
                }
                NBTTagCompound levelData = new NBTTagCompound();
                levelData.setInteger("MadePortal", madePortal);
                levelData.setInteger("MadeMiddlePortal", madeMiddleEarthPortal);
                levelData.setInteger("OverworldX", overworldPortalX);
                levelData.setInteger("OverworldY", overworldPortalY);
                levelData.setInteger("OverworldZ", overworldPortalZ);
                levelData.setInteger("MiddleEarthX", middleEarthPortalX);
                levelData.setInteger("MiddleEarthY", middleEarthPortalY);
                levelData.setInteger("MiddleEarthZ", middleEarthPortalZ);
                levelData.setInteger("StructuresBanned", structuresBanned);
                levelData.setInteger("FastTravel", ftCooldownMax);
                levelData.setInteger("FastTravelMin", ftCooldownMin);
                levelData.setBoolean("GollumSpawned", gollumSpawned);
                levelData.setBoolean("AlignmentZones", enableAlignmentZones);
                levelData.setFloat("ConqRate", conquestRate);
                if(difficulty != null) {
                    levelData.setInteger("SavedDifficulty", difficulty.getDifficultyId());
                }
                levelData.setBoolean("DifficultyLock", difficultyLock);
                NBTTagCompound travellingTraderData = new NBTTagCompound();
                for(LOTRTravellingTraderSpawner trader : LOTREventSpawner.travellingTraders) {
                    NBTTagCompound nbt = new NBTTagCompound();
                    trader.writeToNBT(nbt);
                    travellingTraderData.setTag(trader.entityClassName, nbt);
                }
                levelData.setTag("TravellingTraders", travellingTraderData);
                LOTRGreyWandererTracker.save(levelData);
                LOTRDate.saveDates(levelData);
                LOTRLevelData.saveNBTToFile(LOTR_dat, levelData);
                needsSave = false;
            }
            int i = 0;
            int j = 0;
            for(Map.Entry<UUID, LOTRPlayerData> e : playerDataMap.entrySet()) {
                UUID player = e.getKey();
                LOTRPlayerData pd = e.getValue();
                if(pd.needsSave()) {
                    LOTRLevelData.saveData(player);
                    ++i;
                }
                ++j;
            }
            if(LOTRSpawnDamping.needsSave) {
                LOTRSpawnDamping.saveAll();
            }
        }
        catch(Exception e) {
            FMLLog.severe("Error saving LOTR data", new Object[0]);
            e.printStackTrace();
        }
    }

    public static void load() {
        try {
            NBTTagCompound levelData = LOTRLevelData.loadNBTFromFile(LOTRLevelData.getLOTRDat());
            File oldLOTRDat = new File(DimensionManager.getCurrentSaveRootDirectory(), "LOTR.dat");
            if(oldLOTRDat.exists()) {
                levelData = LOTRLevelData.loadNBTFromFile(oldLOTRDat);
                oldLOTRDat.delete();
                if(levelData.hasKey("PlayerData")) {
                    NBTTagList playerDataTags = levelData.getTagList("PlayerData", 10);
                    for(int i = 0; i < playerDataTags.tagCount(); ++i) {
                        NBTTagCompound nbt = playerDataTags.getCompoundTagAt(i);
                        UUID player = UUID.fromString(nbt.getString("PlayerUUID"));
                        LOTRLevelData.saveNBTToFile(LOTRLevelData.getLOTRPlayerDat(player), nbt);
                    }
                }
            }
            madePortal = levelData.getInteger("MadePortal");
            madeMiddleEarthPortal = levelData.getInteger("MadeMiddlePortal");
            overworldPortalX = levelData.getInteger("OverworldX");
            overworldPortalY = levelData.getInteger("OverworldY");
            overworldPortalZ = levelData.getInteger("OverworldZ");
            middleEarthPortalX = levelData.getInteger("MiddleEarthX");
            middleEarthPortalY = levelData.getInteger("MiddleEarthY");
            middleEarthPortalZ = levelData.getInteger("MiddleEarthZ");
            structuresBanned = levelData.getInteger("StructuresBanned");
            ftCooldownMax = levelData.hasKey("FastTravel") ? levelData.getInteger("FastTravel") : 36000;
            ftCooldownMin = levelData.hasKey("FastTravelMin") ? levelData.getInteger("FastTravelMin") : 3600;
            gollumSpawned = levelData.getBoolean("GollumSpawned");
            enableAlignmentZones = levelData.hasKey("AlignmentZones") ? levelData.getBoolean("AlignmentZones") : true;
            conquestRate = levelData.hasKey("ConqRate") ? levelData.getFloat("ConqRate") : 1.0f;
            if(levelData.hasKey("SavedDifficulty")) {
                EnumDifficulty d;
                int id = levelData.getInteger("SavedDifficulty");
                difficulty = d = EnumDifficulty.getDifficultyEnum(id);
                LOTRMod.proxy.setClientDifficulty(difficulty);
            }
            else {
                difficulty = null;
            }
            difficultyLock = levelData.getBoolean("DifficultyLock");
            NBTTagCompound travellingTraderData = levelData.getCompoundTag("TravellingTraders");
            for(LOTRTravellingTraderSpawner trader : LOTREventSpawner.travellingTraders) {
                NBTTagCompound nbt = travellingTraderData.getCompoundTag(trader.entityClassName);
                trader.readFromNBT(nbt);
            }
            LOTRGreyWandererTracker.load(levelData);
            LOTRLevelData.destroyAllPlayerData();
            LOTRDate.loadDates(levelData);
            LOTRSpawnDamping.loadAll();
            needsLoad = false;
            needsSave = true;
            LOTRLevelData.save();
        }
        catch(Exception e) {
            FMLLog.severe("Error loading LOTR data", new Object[0]);
            e.printStackTrace();
        }
    }

    public static void setMadePortal(int i) {
        madePortal = i;
        LOTRLevelData.markDirty();
    }

    public static void setMadeMiddleEarthPortal(int i) {
        madeMiddleEarthPortal = i;
        LOTRLevelData.markDirty();
    }

    public static void markOverworldPortalLocation(int i, int j, int k) {
        overworldPortalX = i;
        overworldPortalY = j;
        overworldPortalZ = k;
        LOTRLevelData.markDirty();
    }

    public static void markMiddleEarthPortalLocation(int i, int j, int k) {
        LOTRPacketPortalPos packet = new LOTRPacketPortalPos(i, j, k);
        LOTRPacketHandler.networkWrapper.sendToAll(packet);
        LOTRLevelData.markDirty();
    }

    public static void sendLoginPacket(EntityPlayerMP entityplayer) {
        LOTRPacketLogin packet = new LOTRPacketLogin();
        packet.ringPortalX = middleEarthPortalX;
        packet.ringPortalY = middleEarthPortalY;
        packet.ringPortalZ = middleEarthPortalZ;
        packet.ftCooldownMax = ftCooldownMax;
        packet.ftCooldownMin = ftCooldownMin;
        packet.difficulty = difficulty;
        packet.difficultyLocked = difficultyLock;
        packet.alignmentZones = enableAlignmentZones;
        packet.feastMode = LOTRConfig.canAlwaysEat;
        packet.enchanting = LOTRConfig.enchantingVanilla;
        packet.enchantingLOTR = LOTRConfig.enchantingLOTR;
        LOTRPacketHandler.networkWrapper.sendTo(packet, entityplayer);
    }

    public static int getFTCooldownMax() {
        return ftCooldownMax;
    }

    public static int getFTCooldownMin() {
        return ftCooldownMin;
    }

    public static void setFTCooldown(int max, int min) {
        max = Math.max(0, max);
        if((min = Math.max(0, min)) > max) {
            min = max;
        }
        ftCooldownMax = max;
        ftCooldownMin = min;
        LOTRLevelData.markDirty();
        if(!LOTRMod.proxy.isClient()) {
            List players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
            for(Object player : players) {
                EntityPlayerMP entityplayer = (EntityPlayerMP) player;
                LOTRPacketFTCooldown packet = new LOTRPacketFTCooldown(ftCooldownMax, ftCooldownMin);
                LOTRPacketHandler.networkWrapper.sendTo(packet, entityplayer);
            }
        }
    }

    public static boolean enableAlignmentZones() {
        return enableAlignmentZones;
    }

    public static void setEnableAlignmentZones(boolean flag) {
        enableAlignmentZones = flag;
        LOTRLevelData.markDirty();
        if(!LOTRMod.proxy.isClient()) {
            List players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
            for(Object player : players) {
                EntityPlayerMP entityplayer = (EntityPlayerMP) player;
                LOTRPacketEnableAlignmentZones packet = new LOTRPacketEnableAlignmentZones(enableAlignmentZones);
                LOTRPacketHandler.networkWrapper.sendTo(packet, entityplayer);
            }
        }
    }

    public static float getConquestRate() {
        return conquestRate;
    }

    public static void setConquestRate(float f) {
        conquestRate = f;
        LOTRLevelData.markDirty();
    }

    public static void sendPlayerData(EntityPlayerMP entityplayer) {
        try {
            LOTRPlayerData pd = LOTRLevelData.getData(entityplayer);
            pd.sendPlayerData(entityplayer);
        }
        catch(Exception e) {
            FMLLog.severe("Failed to send player data to player " + entityplayer.getCommandSenderName(), new Object[0]);
            e.printStackTrace();
        }
    }

    public static LOTRPlayerData getData(EntityPlayer entityplayer) {
        return LOTRLevelData.getData(entityplayer.getUniqueID());
    }

    public static LOTRPlayerData getData(UUID player) {
        LOTRPlayerData pd = playerDataMap.get(player);
        if(pd == null) {
            pd = LOTRLevelData.loadData(player);
            if(pd == null) {
                pd = new LOTRPlayerData(player);
            }
            playerDataMap.put(player, pd);
        }
        return pd;
    }

    private static LOTRPlayerData loadData(UUID player) {
        try {
            NBTTagCompound nbt = LOTRLevelData.loadNBTFromFile(LOTRLevelData.getLOTRPlayerDat(player));
            LOTRPlayerData pd = new LOTRPlayerData(player);
            pd.load(nbt);
            return pd;
        }
        catch(Exception e) {
            FMLLog.severe("Error loading LOTR player data for %s", player);
            e.printStackTrace();
            return null;
        }
    }

    public static void saveData(UUID player) {
        try {
            NBTTagCompound nbt = new NBTTagCompound();
            LOTRPlayerData pd = playerDataMap.get(player);
            pd.save(nbt);
            LOTRLevelData.saveNBTToFile(LOTRLevelData.getLOTRPlayerDat(player), nbt);
        }
        catch(Exception e) {
            FMLLog.severe("Error saving LOTR player data for %s", player);
            e.printStackTrace();
        }
    }

    private static boolean saveAndClearData(UUID player) {
        LOTRPlayerData pd = playerDataMap.get(player);
        if(pd != null) {
            boolean saved = false;
            if(pd.needsSave()) {
                LOTRLevelData.saveData(player);
                saved = true;
            }
            playerDataMap.remove(player);
            return saved;
        }
        FMLLog.severe("Attempted to clear LOTR player data for %s; no data found", player);
        return false;
    }

    public static void saveAndClearUnusedPlayerData() {
        ArrayList<UUID> clearing = new ArrayList<UUID>();
        for(UUID player : playerDataMap.keySet()) {
            boolean foundPlayer = false;
            for(WorldServer world : MinecraftServer.getServer().worldServers) {
                if(world.func_152378_a(player) == null) continue;
                foundPlayer = true;
                break;
            }
            if(foundPlayer) continue;
            clearing.add(player);
        }
        int numCleared = clearing.size();
        int sizeBefore = playerDataMap.size();
        int numSaved = 0;
        for(UUID player : clearing) {
            boolean saved = LOTRLevelData.saveAndClearData(player);
            if(!saved) continue;
            ++numSaved;
        }
        int sizeNow = playerDataMap.size();
    }

    public static void destroyAllPlayerData() {
        playerDataMap.clear();
    }

    public static boolean structuresBanned() {
        return structuresBanned == 1;
    }

    public static void setStructuresBanned(boolean banned) {
        structuresBanned = banned ? 1 : 0;
        LOTRLevelData.markDirty();
    }

    public static void setPlayerBannedForStructures(String username, boolean flag) {
        UUID uuid = UUID.fromString(PreYggdrasilConverter.func_152719_a(username));
        if(uuid != null) {
            LOTRLevelData.getData(uuid).setStructuresBanned(flag);
        }
    }

    public static boolean isPlayerBannedForStructures(EntityPlayer entityplayer) {
        return LOTRLevelData.getData(entityplayer).getStructuresBanned();
    }

    public static Set<String> getBannedStructurePlayersUsernames() {
        HashSet<String> players = new HashSet<String>();
        for(UUID uuid : playerDataMap.keySet()) {
            String username;
            if(!LOTRLevelData.getData(uuid).getStructuresBanned()) continue;
            GameProfile profile = MinecraftServer.getServer().func_152358_ax().func_152652_a(uuid);
            if(StringUtils.isBlank(profile.getName())) {
                MinecraftServer.getServer().func_147130_as().fillProfileProperties(profile, true);
            }
            if(StringUtils.isBlank(username = profile.getName())) continue;
            players.add(username);
        }
        return players;
    }

    public static void sendAlignmentToAllPlayersInWorld(EntityPlayer entityplayer, World world) {
        for(Object element : world.playerEntities) {
            EntityPlayer worldPlayer = (EntityPlayer) element;
            LOTRPacketAlignment packet = new LOTRPacketAlignment(entityplayer.getUniqueID());
            LOTRPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) worldPlayer);
        }
    }

    public static void sendAllAlignmentsInWorldToPlayer(EntityPlayer entityplayer, World world) {
        for(Object element : world.playerEntities) {
            EntityPlayer worldPlayer = (EntityPlayer) element;
            LOTRPacketAlignment packet = new LOTRPacketAlignment(worldPlayer.getUniqueID());
            LOTRPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public static void selectDefaultShieldForPlayer(EntityPlayer entityplayer) {
        if(LOTRLevelData.getData(entityplayer).getShield() == null) {
            for(LOTRShields shield : LOTRShields.values()) {
                if(!shield.canPlayerWear(entityplayer)) continue;
                LOTRLevelData.getData(entityplayer).setShield(shield);
                return;
            }
        }
    }

    public static void sendShieldToAllPlayersInWorld(EntityPlayer entityplayer, World world) {
        for(Object element : world.playerEntities) {
            EntityPlayer worldPlayer = (EntityPlayer) element;
            LOTRPacketShield packet = new LOTRPacketShield(entityplayer.getUniqueID());
            LOTRPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) worldPlayer);
        }
    }

    public static void sendAllShieldsInWorldToPlayer(EntityPlayer entityplayer, World world) {
        for(Object element : world.playerEntities) {
            EntityPlayer worldPlayer = (EntityPlayer) element;
            LOTRPacketShield packet = new LOTRPacketShield(worldPlayer.getUniqueID());
            LOTRPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public static void sendPlayerLocationsToPlayer(EntityPlayer entityplayer, World world) {
        LOTRPacketUpdatePlayerLocations packetLocations = new LOTRPacketUpdatePlayerLocations();
        boolean isOp = MinecraftServer.getServer().getConfigurationManager().func_152596_g(entityplayer.getGameProfile());
        boolean creative = entityplayer.capabilities.isCreativeMode;
        LOTRPlayerData playerData = LOTRLevelData.getData(entityplayer);
        ArrayList<LOTRFellowship> fellowshipsMapShow = new ArrayList<LOTRFellowship>();
        for(UUID fsID : playerData.getFellowshipIDs()) {
            LOTRFellowship fs = LOTRFellowshipData.getFellowship(fsID);
            if(fs == null || fs.isDisbanded() || !fs.getShowMapLocations()) continue;
            fellowshipsMapShow.add(fs);
        }
        for(Object element : world.playerEntities) {
            boolean show;
            EntityPlayer worldPlayer = (EntityPlayer) element;
            if(worldPlayer == entityplayer) continue;
            boolean bl = show = !LOTRLevelData.getData(worldPlayer).getHideMapLocation();
            if(LOTRConfig.forceMapLocations == 1) {
                show = false;
            }
            else if(LOTRConfig.forceMapLocations == 2) {
                show = true;
            }
            else if(!show) {
                if(isOp && creative) {
                    show = true;
                }
                else if(!isOp && LOTRLevelData.getData(worldPlayer).getAdminHideMap()) {
                    show = false;
                }
                else if(!playerData.isSiegeActive()) {
                    for(LOTRFellowship fs : fellowshipsMapShow) {
                        if(!fs.containsPlayer(worldPlayer.getUniqueID())) continue;
                        show = true;
                        break;
                    }
                }
            }
            if(!show) continue;
            packetLocations.addPlayerLocation(worldPlayer.getGameProfile(), worldPlayer.posX, worldPlayer.posZ);
        }
        LOTRPacketHandler.networkWrapper.sendTo(packetLocations, (EntityPlayerMP) entityplayer);
    }

    public static boolean gollumSpawned() {
        return gollumSpawned;
    }

    public static void setGollumSpawned(boolean flag) {
        gollumSpawned = flag;
        LOTRLevelData.markDirty();
    }

    public static EnumDifficulty getSavedDifficulty() {
        return difficulty;
    }

    public static void setSavedDifficulty(EnumDifficulty d) {
        difficulty = d;
        LOTRLevelData.markDirty();
    }

    public static boolean isDifficultyLocked() {
        return difficultyLock;
    }

    public static void setDifficultyLocked(boolean flag) {
        difficultyLock = flag;
        LOTRLevelData.markDirty();
    }

    public static String getHMSTime(int i) {
        int hours = i / 72000;
        int minutes = i % 72000 / 1200;
        int seconds = i % 72000 % 1200 / 20;
        return hours + "h " + minutes + "m " + seconds + "s";
    }

    static {
        conquestRate = 1.0f;
        difficultyLock = false;
        playerDataMap = new HashMap<UUID, LOTRPlayerData>();
        needsLoad = true;
        needsSave = false;
        rand = new Random();
    }
}
