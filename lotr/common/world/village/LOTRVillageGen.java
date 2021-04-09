package lotr.common.world.village;

import java.util.*;
import lotr.common.util.CentredSquareArray;
import lotr.common.world.LOTRWorldChunkManager;
import lotr.common.world.biome.LOTRBiome;
import lotr.common.world.map.*;
import lotr.common.world.structure2.LOTRWorldGenStructureBase2;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public abstract class LOTRVillageGen {
    protected LOTRBiome villageBiome;
    private List<BiomeGenBase> spawnBiomes;
    private static Random villageRand = new Random();
    protected int gridScale;
    protected int gridRandomDisplace;
    protected float spawnChance;
    protected int villageChunkSize;
    public static final double SQRT2 = Math.sqrt(2.0);

    public LOTRVillageGen(LOTRBiome biome) {
        this.villageBiome = biome;
        this.spawnBiomes = new ArrayList<BiomeGenBase>();
        this.spawnBiomes.add(this.villageBiome);
    }

    public final AbstractInstance createAndSetupVillageInstance(World world, int i, int k, Random random) {
        AbstractInstance instance = this.createVillageInstance(world, i, k, random);
        instance.setupBaseAndVillageProperties();
        return instance;
    }

    protected abstract AbstractInstance createVillageInstance(World var1, int var2, int var3, Random var4);

    private static void seedVillageRand(World world, int i, int k) {
        long seed = i * 6890360793007L + k * 456879569029062L + world.getWorldInfo().getSeed() + 274893855L;
        villageRand.setSeed(seed);
    }

    private boolean isVillageCentre(World world, int chunkX, int chunkZ) {
        LOTRWorldChunkManager worldChunkMgr = (LOTRWorldChunkManager) world.getWorldChunkManager();
        LOTRVillagePositionCache cache = worldChunkMgr.getVillageCache(this);
        if(cache.isVillageAt(chunkX, chunkZ)) {
            return true;
        }
        if(cache.isVillageNotAt(chunkX, chunkZ)) {
            return false;
        }
        int i2 = MathHelper.floor_double((double) chunkX / (double) this.gridScale);
        int k2 = MathHelper.floor_double((double) chunkZ / (double) this.gridScale);
        LOTRVillageGen.seedVillageRand(world, i2, k2);
        i2 *= this.gridScale;
        k2 *= this.gridScale;
        if(chunkX == (i2 += MathHelper.getRandomIntegerInRange(villageRand, -this.gridRandomDisplace, this.gridRandomDisplace)) && chunkZ == (k2 += MathHelper.getRandomIntegerInRange(villageRand, -this.gridRandomDisplace, this.gridRandomDisplace))) {
            int i1 = chunkX * 16 + 8;
            int k1 = chunkZ * 16 + 8;
            int villageRange = this.villageChunkSize * 16;
            if(villageRand.nextFloat() < this.spawnChance) {
                int diagRange = (int) Math.round((villageRange + 8) * SQRT2);
                boolean anythingNear = false;
                boolean bl = anythingNear = LOTRRoads.isRoadNear(i1, k1, diagRange) >= 0.0f;
                if(!anythingNear && !(anythingNear = LOTRMountains.mountainNear(i1, k1, diagRange))) {
                    anythingNear = LOTRFixedStructures.structureNear(world, i1, k1, diagRange);
                }
                if(!anythingNear) {
                    LOTRVillageGen.seedVillageRand(world, i1, k1);
                    AbstractInstance instance = this.createAndSetupVillageInstance(world, i1, k1, villageRand);
                    boolean flat = instance.isFlat();
                    if(worldChunkMgr.areBiomesViable(i1, k1, villageRange, this.spawnBiomes) && worldChunkMgr.areVariantsSuitableVillage(i1, k1, villageRange, flat)) {
                        cache.markResult(chunkX, chunkZ, true);
                        return true;
                    }
                }
            }
        }
        cache.markResult(chunkX, chunkZ, false);
        return false;
    }

    private List<AbstractInstance> getNearbyVillages(World world, int chunkX, int chunkZ) {
        ArrayList<AbstractInstance> villages = new ArrayList<AbstractInstance>();
        for(int i = chunkX - this.villageChunkSize; i <= chunkX + this.villageChunkSize; ++i) {
            for(int k = chunkZ - this.villageChunkSize; k <= chunkZ + this.villageChunkSize; ++k) {
                if(!this.isVillageCentre(world, i, k)) continue;
                int centreX = (i << 4) + 8;
                int centreZ = (k << 4) + 8;
                LOTRVillageGen.seedVillageRand(world, centreX, centreZ);
                AbstractInstance instance = this.createAndSetupVillageInstance(world, centreX, centreZ, villageRand);
                villages.add(instance);
            }
        }
        return villages;
    }

    public List<AbstractInstance> getNearbyVillagesAtPosition(World world, int i, int k) {
        int chunkX = i >> 4;
        int chunkZ = k >> 4;
        return this.getNearbyVillages(world, chunkX, chunkZ);
    }

    public void generateInChunk(World world, int i, int k) {
        List<AbstractInstance> villages = this.getNearbyVillagesAtPosition(world, i, k);
        for(AbstractInstance instance : villages) {
            instance.setupVillageStructures();
            this.generateInstanceInChunk(instance, world, i, k);
        }
    }

    public void generateInstanceInChunk(AbstractInstance instance, World world, int i, int k) {
        for(int i1 = i; i1 <= i + 15; ++i1) {
            for(int k1 = k; k1 <= k + 15; ++k1) {
                BiomeGenBase biome = world.getBiomeGenForCoords(i1, k1);
                Object[] pathData = this.getHeight_getPath_isSlab(instance, world, i1, k1, biome);
                LOTRRoadType pathType = (LOTRRoadType) pathData[1];
                if(pathType != null) {
                    int j1 = (Integer) pathData[0];
                    boolean isSlab = (Boolean) pathData[2];
                    instance.setupWorldPositionSeed(i1, k1);
                    LOTRRoadType.RoadBlock roadblock = pathType.getBlock(instance.instanceRand, biome, true, isSlab);
                    world.setBlock(i1, j1, k1, roadblock.block, roadblock.meta, 2);
                    Block above = world.getBlock(i1, j1 + 1, k1);
                    if(!above.canBlockStay(world, i1, j1 + 1, k1)) {
                        world.setBlock(i1, j1 + 1, k1, Blocks.air, 0, 3);
                    }
                }
                instance.setupWorldPositionSeed(i1, k1);
                for(StructureInfo struct : instance.structures) {
                    int[] coords = instance.getWorldCoords(struct.posX, struct.posZ);
                    if(i1 != coords[0] || k1 != coords[1]) continue;
                    int j1 = world.getTopSolidOrLiquidBlock(i1, k1);
                    int minHeight = 62;
                    int terrainTypeMinHeight = world.provider.terrainType.getMinimumSpawnHeight(world);
                    if(terrainTypeMinHeight < 62) {
                        minHeight = terrainTypeMinHeight - 1;
                    }
                    if(j1 <= minHeight) continue;
                    struct.structure.generateWithSetRotation(world, instance.instanceRand, i1, j1, k1, instance.getStructureRotation(struct.rotation));
                }
            }
        }
    }

    private Object[] getHeight_getPath_isSlab(AbstractInstance instance, World world, int i, int k, BiomeGenBase biome) {
        instance.setupWorldPositionSeed(i, k);
        int[] coords = instance.getRelativeCoords(i, k);
        int i1 = coords[0];
        int k1 = coords[1];
        LOTRRoadType road = instance.getPath(instance.instanceRand, i1, k1);
        boolean isPath = false;
        boolean isSlab = false;
        int j1 = this.getTopTerrainBlock(world, i, k, biome, false);
        if(road != null && j1 > 0 && LOTRWorldGenStructureBase2.isSurfaceStatic(world, i, j1, k)) {
            isPath = true;
            int slabRange = 1;
            CentredSquareArray<Integer> slabArray = new CentredSquareArray<Integer>(slabRange);
            slabArray.fill(j1);
            for(int i2 = -slabRange; i2 <= slabRange; ++i2) {
                for(int k2 = -slabRange; k2 <= slabRange; ++k2) {
                    int j2;
                    int i3 = i + i2;
                    int k3 = k + k2;
                    if(i2 == 0 && k2 == 0 || (j2 = this.getTopTerrainBlock(world, i3, k3, biome, false)) <= 0 || j2 >= j1) continue;
                    slabArray.set(i2, k2, j2);
                }
            }
            if(slabArray.get(-1, 0) < j1 || slabArray.get(1, 0) < j1 || slabArray.get(0, -1) < j1 || slabArray.get(0, 1) < j1) {
                isSlab = true;
            }
            else if(slabArray.get(-1, -1) < j1 || slabArray.get(1, -1) < j1 || slabArray.get(-1, 1) < j1 || slabArray.get(1, 1) < j1) {
                isSlab = true;
            }
            if(isSlab && world.getBlock(i, j1 + 1, k).isOpaqueCube()) {
                isSlab = false;
            }
        }
        Object[] ret = new Object[3];
        if(isPath) {
            ret[0] = j1;
            ret[1] = road;
            ret[2] = isSlab;
        }
        else {
            ret[0] = -1;
            ret[1] = null;
            ret[2] = false;
        }
        return ret;
    }

    private int getTopTerrainBlock(World world, int i, int k, BiomeGenBase biome, boolean acceptSlab) {
        int j = world.getTopSolidOrLiquidBlock(i, k) - 1;
        while(!world.getBlock(i, j + 1, k).getMaterial().isLiquid()) {
            Block block = world.getBlock(i, j, k);
            Block below = world.getBlock(i, j - 1, k);
            if(block.isOpaqueCube() || acceptSlab && block instanceof BlockSlab && below.isOpaqueCube()) {
                return j;
            }
            if(--j > 62) continue;
            break;
        }
        return -1;
    }

    public void generateCompleteVillageInstance(AbstractInstance instance, World world, int i, int k) {
        instance.setupVillageStructures();
        for(int i1 = -this.villageChunkSize; i1 <= this.villageChunkSize; ++i1) {
            for(int k1 = -this.villageChunkSize; k1 <= this.villageChunkSize; ++k1) {
                int i2 = i - 8 + i1 * 16;
                int k2 = k - 8 + k1 * 16;
                this.generateInstanceInChunk(instance, world, i2, k2);
            }
        }
    }

    public abstract class AbstractInstance {
        private World theWorld;
        private Random instanceRand;
        private long instanceRandSeed;
        private int centreX;
        private int centreZ;
        private int rotationMode;
        private List<StructureInfo> structures = new ArrayList<StructureInfo>();

        protected AbstractInstance(World world, int i, int k, Random random) {
            this.theWorld = world;
            this.instanceRand = new Random();
            this.instanceRandSeed = random.nextLong();
            this.centreX = i;
            this.centreZ = k;
        }

        protected final void setupBaseAndVillageProperties() {
            this.setupVillageSeed();
            this.rotationMode = this.instanceRand.nextInt(4);
            this.setupVillageProperties(this.instanceRand);
        }

        protected abstract void setupVillageProperties(Random var1);

        private void setupVillageSeed() {
            long seed = this.centreX * 580682095692076767L + this.centreZ * 12789948968296726L + this.theWorld.getWorldInfo().getSeed() + 49920968939865L;
            this.instanceRand.setSeed(seed += this.instanceRandSeed);
        }

        public void setRotation(int i) {
            this.rotationMode = i;
        }

        private void setupWorldPositionSeed(int i, int k) {
            this.setupVillageSeed();
            int[] coords = this.getRelativeCoords(i, k);
            int i1 = coords[0];
            int k1 = coords[1];
            long seed1 = this.instanceRand.nextLong();
            long seed2 = this.instanceRand.nextLong();
            long seed = i1 * seed1 + k1 * seed2 ^ this.theWorld.getWorldInfo().getSeed();
            this.instanceRand.setSeed(seed);
        }

        public abstract boolean isFlat();

        protected final void setupVillageStructures() {
            this.setupVillageSeed();
            this.structures.clear();
            this.addVillageStructures(this.instanceRand);
        }

        protected abstract void addVillageStructures(Random var1);

        protected void addStructure(LOTRWorldGenStructureBase2 structure, int x, int z, int r) {
            this.addStructure(structure, x, z, r, false);
        }

        protected void addStructure(LOTRWorldGenStructureBase2 structure, int x, int z, int r, boolean force) {
            structure.villageInstance = this;
            boolean bl = structure.restrictions = !force;
            if(force) {
                structure.shouldFindSurface = true;
            }
            this.structures.add(new StructureInfo(structure, x, z, r));
        }

        protected abstract LOTRRoadType getPath(Random var1, int var2, int var3);

        public abstract boolean isVillageSurface(World var1, int var2, int var3, int var4);

        private int[] getWorldCoords(int xRel, int zRel) {
            int xWorld = this.centreX;
            int zWorld = this.centreZ;
            switch(this.rotationMode) {
                case 0: {
                    xWorld = this.centreX - xRel;
                    zWorld = this.centreZ - zRel;
                    break;
                }
                case 1: {
                    xWorld = this.centreX + zRel;
                    zWorld = this.centreZ - xRel;
                    break;
                }
                case 2: {
                    xWorld = this.centreX + xRel;
                    zWorld = this.centreZ + zRel;
                    break;
                }
                case 3: {
                    xWorld = this.centreX - zRel;
                    zWorld = this.centreZ + xRel;
                }
            }
            return new int[] {xWorld, zWorld};
        }

        private int[] getRelativeCoords(int xWorld, int zWorld) {
            int xRel = 0;
            int zRel = 0;
            switch(this.rotationMode) {
                case 0: {
                    xRel = this.centreX - xWorld;
                    zRel = this.centreZ - zWorld;
                    break;
                }
                case 1: {
                    xRel = this.centreZ - zWorld;
                    zRel = xWorld - this.centreX;
                    break;
                }
                case 2: {
                    xRel = xWorld - this.centreX;
                    zRel = zWorld - this.centreZ;
                    break;
                }
                case 3: {
                    xRel = zWorld - this.centreZ;
                    zRel = this.centreX - xWorld;
                }
            }
            return new int[] {xRel, zRel};
        }

        private int getStructureRotation(int r) {
            return (r + (this.rotationMode + 2)) % 4;
        }
    }

    private class StructureInfo {
        public LOTRWorldGenStructureBase2 structure;
        public int posX;
        public int posZ;
        public int rotation;

        public StructureInfo(LOTRWorldGenStructureBase2 s, int x, int z, int r) {
            this.structure = s;
            this.posX = x;
            this.posZ = z;
            this.rotation = r;
        }
    }

}
