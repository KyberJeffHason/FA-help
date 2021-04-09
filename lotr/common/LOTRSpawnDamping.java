package lotr.common;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.*;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class LOTRSpawnDamping {
    public static Map<String, Float> spawnDamping = new HashMap<String, Float>();
    public static String TYPE_NPC = "lotr_npc";
    public static boolean needsSave = true;

    public static int getCreatureSpawnCap(EnumCreatureType type, World world) {
        return LOTRSpawnDamping.getSpawnCap(type.name(), type.getMaxNumberOfCreature(), world);
    }

    public static int getNPCSpawnCap(World world) {
        return LOTRSpawnDamping.getSpawnCap(TYPE_NPC, LOTRDimension.getCurrentDimension(world).spawnCap, world);
    }

    public static int getSpawnCap(String type, int baseCap, World world) {
        int players = world.playerEntities.size();
        return LOTRSpawnDamping.getSpawnCap(type, baseCap, players);
    }

    public static int getSpawnCap(String type, int baseCap, int players) {
        float stationaryPointValue;
        float damp = LOTRSpawnDamping.getSpawnDamping(type);
        float dampFraction = (players - 1) * damp;
        if((dampFraction = MathHelper.clamp_float(dampFraction, 0.0f, 1.0f)) > (stationaryPointValue = 0.5f + damp / 2.0f)) {
            dampFraction = stationaryPointValue;
        }
        int capPerPlayer = Math.round(baseCap * (1.0f - dampFraction));
        capPerPlayer = Math.max(capPerPlayer, 1);
        return capPerPlayer;
    }

    public static float getSpawnDamping(String type) {
        float f = 0.0f;
        if(spawnDamping.containsKey(type)) {
            f = spawnDamping.get(type).floatValue();
        }
        return f;
    }

    public static void setSpawnDamping(EnumCreatureType type, float damping) {
        LOTRSpawnDamping.setSpawnDamping(type.name(), damping);
    }

    public static void setNPCSpawnDamping(float damping) {
        LOTRSpawnDamping.setSpawnDamping(TYPE_NPC, damping);
    }

    public static void setSpawnDamping(String type, float damping) {
        spawnDamping.put(type, Float.valueOf(damping));
        LOTRSpawnDamping.markDirty();
    }

    public static int getBaseSpawnCapForInfo(String type, World world) {
        if(type.equals(TYPE_NPC)) {
            return LOTRDimension.getCurrentDimension(world).spawnCap;
        }
        EnumCreatureType creatureType = EnumCreatureType.valueOf(type);
        if(creatureType != null) {
            return creatureType.getMaxNumberOfCreature();
        }
        return -1;
    }

    private static void markDirty() {
        needsSave = true;
    }

    private static File getDataFile() {
        return new File(LOTRLevelData.getOrCreateLOTRDir(), "spawn_damping.dat");
    }

    public static void saveAll() {
        try {
            File datFile = LOTRSpawnDamping.getDataFile();
            if(!datFile.exists()) {
                CompressedStreamTools.writeCompressed(new NBTTagCompound(), new FileOutputStream(datFile));
            }
            NBTTagCompound spawnData = new NBTTagCompound();
            NBTTagList typeTags = new NBTTagList();
            for(Map.Entry<String, Float> e : spawnDamping.entrySet()) {
                String type = e.getKey();
                float damping = e.getValue().floatValue();
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("Type", type);
                nbt.setFloat("Damp", damping);
                typeTags.appendTag(nbt);
            }
            spawnData.setTag("Damping", typeTags);
            LOTRLevelData.saveNBTToFile(datFile, spawnData);
            needsSave = false;
        }
        catch(Exception e) {
            FMLLog.severe("Error saving LOTR spawn damping", new Object[0]);
            e.printStackTrace();
        }
    }

    public static void loadAll() {
        try {
            File datFile = LOTRSpawnDamping.getDataFile();
            NBTTagCompound spawnData = LOTRLevelData.loadNBTFromFile(datFile);
            spawnDamping.clear();
            if(spawnData.hasKey("Damping")) {
                NBTTagList typeTags = spawnData.getTagList("Damping", 10);
                for(int i = 0; i < typeTags.tagCount(); ++i) {
                    NBTTagCompound nbt = typeTags.getCompoundTagAt(i);
                    String type = nbt.getString("Type");
                    float damping = nbt.getFloat("Damp");
                    if(StringUtils.isBlank(type)) continue;
                    spawnDamping.put(type, Float.valueOf(damping));
                }
            }
            needsSave = true;
            LOTRSpawnDamping.saveAll();
        }
        catch(Exception e) {
            FMLLog.severe("Error loading LOTR spawn damping", new Object[0]);
            e.printStackTrace();
        }
    }

    public static void resetAll() {
        spawnDamping.clear();
        LOTRSpawnDamping.markDirty();
    }
}
