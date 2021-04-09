package lotr.common.world.mapgen.tpyr;

import java.util.*;
import lotr.common.LOTRDimension;
import lotr.common.world.LOTRWorldChunkManager;
import lotr.common.world.biome.*;
import lotr.common.world.village.LOTRVillagePositionCache;
import net.minecraft.util.MathHelper;
import net.minecraft.world.gen.structure.*;

public class LOTRMapGenTauredainPyramid extends MapGenStructure {
    private static List spawnBiomes;
    private int spawnChance = 10;
    private static int minDist;
    private static int separation;

    private static void setupSpawnBiomes() {
        if(spawnBiomes == null) {
            spawnBiomes = new ArrayList();
            for(LOTRBiome biome : LOTRDimension.MIDDLE_EARTH.biomeList) {
                boolean flag = false;
                if(biome instanceof LOTRBiomeGenFarHaradJungle && !(biome instanceof LOTRBiomeGenTauredainClearing)) {
                    flag = true;
                }
                if(!flag) continue;
                spawnBiomes.add(biome);
            }
        }
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int i, int k) {
        LOTRWorldChunkManager worldChunkMgr = (LOTRWorldChunkManager) this.worldObj.getWorldChunkManager();
        LOTRVillagePositionCache cache = worldChunkMgr.getStructureCache(this);
        if(cache.isVillageAt(i, k)) {
            return true;
        }
        if(cache.isVillageNotAt(i, k)) {
            return false;
        }
        LOTRMapGenTauredainPyramid.setupSpawnBiomes();
        int i2 = MathHelper.floor_double((double) i / (double) separation);
        int k2 = MathHelper.floor_double((double) k / (double) separation);
        Random dRand = this.worldObj.setRandomSeed(i2, k2, 190169976);
        i2 *= separation;
        k2 *= separation;
        if(i == (i2 += dRand.nextInt(separation - minDist + 1)) && k == (k2 += dRand.nextInt(separation - minDist + 1))) {
            int i1 = i * 16 + 8;
            int k1 = k * 16 + 8;
            if(this.worldObj.getWorldChunkManager().areBiomesViable(i1, k1, 0, spawnBiomes) && this.rand.nextInt(this.spawnChance) == 0) {
                cache.markResult(i, k, true);
                return true;
            }
        }
        cache.markResult(i, k, false);
        return false;
    }

    @Override
    protected StructureStart getStructureStart(int i, int j) {
        return new LOTRStructureTPyrStart(this.worldObj, this.rand, i, j);
    }

    @Override
    public String func_143025_a() {
        return "LOTR.TPyr";
    }

    public static void register() {
        MapGenStructureIO.registerStructure(LOTRStructureTPyrStart.class, "LOTR.TPyr");
        MapGenStructureIO.func_143031_a(LOTRComponentTauredainPyramid.class, "LOTR.TPyr.Pyramid");
    }

    static {
        minDist = 12;
        separation = 24;
    }
}
