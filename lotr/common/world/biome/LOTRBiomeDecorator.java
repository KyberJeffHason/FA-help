package lotr.common.world.biome;

import java.util.*;
import lotr.common.LOTRMod;
import lotr.common.world.LOTRChunkProvider;
import lotr.common.world.LOTRWorldChunkManager;
import lotr.common.world.biome.variant.LOTRBiomeVariant;
import lotr.common.world.feature.*;
import lotr.common.world.map.LOTRRoads;
import lotr.common.world.structure.*;
import lotr.common.world.structure2.*;
import lotr.common.world.village.LOTRVillageGen;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.*;

public class LOTRBiomeDecorator {
    private World worldObj;
    private Random rand;
    private int chunkX;
    private int chunkZ;
    private LOTRBiome biome;
    private List<OreGenerant> biomeSoils = new ArrayList<OreGenerant>();
    private List<OreGenerant> biomeOres = new ArrayList<OreGenerant>();
    private List<OreGenerant> biomeGems = new ArrayList<OreGenerant>();
    public float biomeOreFactor = 1.0f;
    public float biomeGemFactor = 0.5f;
    protected WorldGenerator clayGen = new LOTRWorldGenSand(Blocks.clay, 5, 1);
    private WorldGenerator sandGen = new LOTRWorldGenSand(Blocks.sand, 7, 2);
    private WorldGenerator whiteSandGen = new LOTRWorldGenSand(LOTRMod.whiteSand, 7, 2);
    private WorldGenerator quagmireGen = new LOTRWorldGenSand(LOTRMod.quagmire, 7, 2);
    private WorldGenerator surfaceGravelGen = new LOTRWorldGenSurfaceGravel();
    private WorldGenerator flowerGen = new LOTRWorldGenBiomeFlowers();
    private WorldGenerator logGen = new LOTRWorldGenLogs();
    private WorldGenerator mushroomBrownGen = new WorldGenFlowers(Blocks.brown_mushroom);
    private WorldGenerator mushroomRedGen = new WorldGenFlowers(Blocks.red_mushroom);
    private WorldGenerator caneGen = new WorldGenReed();
    private WorldGenerator reedGen = new LOTRWorldGenReeds(LOTRMod.reeds);
    private WorldGenerator dryReedGen = new LOTRWorldGenReeds(LOTRMod.driedReeds);
    private WorldGenerator cornGen = new LOTRWorldGenCorn();
    private WorldGenerator pumpkinGen = new WorldGenPumpkin();
    private WorldGenerator waterlilyGen = new WorldGenWaterlily();
    private WorldGenerator cobwebGen = new LOTRWorldGenCaveCobwebs();
    private WorldGenerator stalactiteGen = new LOTRWorldGenStalactites();
    private WorldGenerator vinesGen = new WorldGenVines();
    private WorldGenerator cactusGen = new WorldGenCactus();
    private WorldGenerator melonGen = new WorldGenMelon();
    public int sandPerChunk = 4;
    public int clayPerChunk = 3;
    public int quagmirePerChunk = 0;
    public int treesPerChunk = 0;
    public int willowPerChunk = 0;
    public int logsPerChunk = 0;
    public int vinesPerChunk = 0;
    public int flowersPerChunk = 2;
    public int doubleFlowersPerChunk = 0;
    public int grassPerChunk = 1;
    public int doubleGrassPerChunk = 0;
    public boolean enableFern = false;
    public boolean enableSpecialGrasses = true;
    public int deadBushPerChunk = 0;
    public int waterlilyPerChunk = 0;
    public int mushroomsPerChunk = 0;
    public boolean enableRandomMushroom = true;
    public int canePerChunk = 0;
    public int reedPerChunk = 1;
    public float dryReedChance = 0.1f;
    public int cornPerChunk = 0;
    public int cactiPerChunk = 0;
    public float melonPerChunk = 0.0f;
    public boolean generateWater = true;
    public boolean generateLava = true;
    public boolean generateCobwebs = true;
    public boolean generateAthelas = false;
    public boolean whiteSand = false;
    private int treeClusterSize;
    private int treeClusterChance = -1;
    private WorldGenerator orcDungeonGen = new LOTRWorldGenOrcDungeon(false);
    private WorldGenerator trollHoardGen = new LOTRWorldGenTrollHoard();
    public boolean generateOrcDungeon = false;
    public boolean generateTrollHoard = false;
    private List<LOTRTreeType.WeightedTreeType> treeTypes = new ArrayList<LOTRTreeType.WeightedTreeType>();
    private Random structureRand = new Random();
    private List<RandomStructure> randomStructures = new ArrayList<RandomStructure>();
    private List<LOTRVillageGen> villages = new ArrayList<LOTRVillageGen>();

    public void addSoil(WorldGenerator gen, float f, int min, int max) {
        this.biomeSoils.add(new OreGenerant(gen, f, min, max));
    }

    public void addOre(WorldGenerator gen, float f, int min, int max) {
        this.biomeOres.add(new OreGenerant(gen, f, min, max));
    }

    public void addGem(WorldGenerator gen, float f, int min, int max) {
        this.biomeGems.add(new OreGenerant(gen, f, min, max));
    }

    public void clearOres() {
        this.biomeSoils.clear();
        this.biomeOres.clear();
        this.biomeGems.clear();
    }

    private void addDefaultOres() {
        this.addSoil(new WorldGenMinable(Blocks.dirt, 32), 40.0f, 0, 256);
        this.addSoil(new WorldGenMinable(Blocks.gravel, 32), 20.0f, 0, 256);
        this.addOre(new WorldGenMinable(Blocks.coal_ore, 16), 40.0f, 0, 128);
        this.addOre(new WorldGenMinable(LOTRMod.oreCopper, 8), 16.0f, 0, 128);
        this.addOre(new WorldGenMinable(LOTRMod.oreTin, 8), 16.0f, 0, 128);
        this.addOre(new WorldGenMinable(Blocks.iron_ore, 8), 20.0f, 0, 64);
        this.addOre(new WorldGenMinable(LOTRMod.oreSulfur, 8), 2.0f, 0, 64);
        this.addOre(new WorldGenMinable(LOTRMod.oreSaltpeter, 8), 2.0f, 0, 64);
        this.addOre(new WorldGenMinable(LOTRMod.oreSalt, 12), 2.0f, 0, 64);
        this.addOre(new WorldGenMinable(Blocks.gold_ore, 8), 2.0f, 0, 32);
        this.addOre(new WorldGenMinable(LOTRMod.oreSilver, 8), 3.0f, 0, 32);
        this.addGem(new WorldGenMinable(LOTRMod.oreGem, 1, 6, Blocks.stone), 2.0f, 0, 64);
        this.addGem(new WorldGenMinable(LOTRMod.oreGem, 0, 6, Blocks.stone), 2.0f, 0, 64);
        this.addGem(new WorldGenMinable(LOTRMod.oreGem, 4, 5, Blocks.stone), 1.5f, 0, 48);
        this.addGem(new WorldGenMinable(LOTRMod.oreGem, 6, 5, Blocks.stone), 1.5f, 0, 48);
        this.addGem(new WorldGenMinable(LOTRMod.oreGem, 2, 4, Blocks.stone), 1.0f, 0, 32);
        this.addGem(new WorldGenMinable(LOTRMod.oreGem, 3, 4, Blocks.stone), 1.0f, 0, 32);
        this.addGem(new WorldGenMinable(LOTRMod.oreGem, 7, 4, Blocks.stone), 0.75f, 0, 24);
        this.addGem(new WorldGenMinable(LOTRMod.oreGem, 5, 4, Blocks.stone), 0.5f, 0, 16);
    }

    public LOTRBiomeDecorator(LOTRBiome lotrbiome) {
        this.biome = lotrbiome;
        this.addDefaultOres();
    }

    public void addTree(LOTRTreeType type, int weight) {
        this.treeTypes.add(new LOTRTreeType.WeightedTreeType(type, weight));
    }

    public void clearTrees() {
        this.treeTypes.clear();
    }

    public LOTRTreeType getRandomTree(Random random) {
        if(this.treeTypes.isEmpty()) {
            return LOTRTreeType.OAK;
        }
        WeightedRandom.Item item = WeightedRandom.getRandomItem(random, this.treeTypes);
        return ((LOTRTreeType.WeightedTreeType) item).treeType;
    }

    public LOTRTreeType getRandomTreeForVariant(Random random, LOTRBiomeVariant variant) {
        if(variant.treeTypes.isEmpty()) {
            return this.getRandomTree(random);
        }
        float f = variant.variantTreeChance;
        if(random.nextFloat() < f) {
            return variant.getRandomTree(random);
        }
        return this.getRandomTree(random);
    }

    public void genTree(World world, Random random, int i, int j, int k) {
        WorldGenAbstractTree treeGen = this.biome.getTreeGen(world, random, i, j, k);
        treeGen.generate(world, random, i, j, k);
    }

    public void setTreeCluster(int size, int chance) {
        this.treeClusterSize = size;
        this.treeClusterChance = chance;
    }

    public void resetTreeCluster() {
        this.setTreeCluster(0, -1);
    }

    public void addRandomStructure(WorldGenerator structure, int chunkChance) {
        this.randomStructures.add(new RandomStructure(structure, chunkChance));
    }

    public void clearRandomStructures() {
        this.randomStructures.clear();
    }

    public void addVillage(LOTRVillageGen village) {
        this.villages.add(village);
    }

    public void clearVillages() {
        this.villages.clear();
    }

    public void checkForVillages(World world, int i, int k, LOTRChunkProvider.ChunkFlags chunkFlags) {
        chunkFlags.isVillage = false;
        chunkFlags.isFlatVillage = false;
        for(LOTRVillageGen village : this.villages) {
            List<LOTRVillageGen.AbstractInstance> instances = village.getNearbyVillagesAtPosition(world, i, k);
            if(instances.isEmpty()) continue;
            chunkFlags.isVillage = true;
            for(LOTRVillageGen.AbstractInstance inst : instances) {
                if(!inst.isFlat()) continue;
                chunkFlags.isFlatVillage = true;
            }
        }
    }

    public int getVariantTreesPerChunk(LOTRBiomeVariant variant) {
        int trees = this.treesPerChunk;
        if(variant.treeFactor > 1.0f) {
            trees = Math.max(trees, 1);
        }
        trees = Math.round(trees * variant.treeFactor);
        return trees;
    }

    public void decorate(World world, Random random, int i, int k) {
        this.worldObj = world;
        this.rand = random;
        this.chunkX = i;
        this.chunkZ = k;
        this.decorate();
    }

    private void decorate() {
        int l;
        int j;
        int l2;
        int i;
        int l3;
        int i2;
        int j2;
        int k;
        int l4;
        int j3;
        int l5;
        WorldGenerator house;
        int k2;
        int k3;
        int j4;
        int k4;
        int k5;
        int cluster;
        int k6;
        int i3;
        int l6;
        int i4;
        int l7;
        int j5;
        float reciprocalTreeFactor;
        int i5;
        int k7;
        LOTRBiomeVariant biomeVariant = ((LOTRWorldChunkManager) this.worldObj.getWorldChunkManager()).getBiomeVariantAt(this.chunkX + 8, this.chunkZ + 8);
        this.generateOres();
        biomeVariant.decorateVariant(this.worldObj, this.rand, this.chunkX, this.chunkZ, this.biome);
        if(this.rand.nextBoolean() && this.generateCobwebs) {
            i2 = this.chunkX + this.rand.nextInt(16) + 8;
            int j6 = this.rand.nextInt(60);
            k2 = this.chunkZ + this.rand.nextInt(16) + 8;
            this.cobwebGen.generate(this.worldObj, this.rand, i2, j6, k2);
        }
        for(l2 = 0; l2 < 3; ++l2) {
            i4 = this.chunkX + this.rand.nextInt(16) + 8;
            j4 = this.rand.nextInt(60);
            int k8 = this.chunkZ + this.rand.nextInt(16) + 8;
            this.stalactiteGen.generate(this.worldObj, this.rand, i4, j4, k8);
        }
        for(l2 = 0; l2 < this.quagmirePerChunk; ++l2) {
            i4 = this.chunkX + this.rand.nextInt(16) + 8;
            k2 = this.chunkZ + this.rand.nextInt(16) + 8;
            this.quagmireGen.generate(this.worldObj, this.rand, i4, this.worldObj.getTopSolidOrLiquidBlock(i4, k2), k2);
        }
        for(l2 = 0; l2 < this.sandPerChunk; ++l2) {
            i4 = this.chunkX + this.rand.nextInt(16) + 8;
            k2 = this.chunkZ + this.rand.nextInt(16) + 8;
            WorldGenerator biomeSandGenerator = this.sandGen;
            if(this.whiteSand) {
                biomeSandGenerator = this.whiteSandGen;
            }
            biomeSandGenerator.generate(this.worldObj, this.rand, i4, this.worldObj.getTopSolidOrLiquidBlock(i4, k2), k2);
        }
        for(l2 = 0; l2 < this.clayPerChunk; ++l2) {
            i4 = this.chunkX + this.rand.nextInt(16) + 8;
            k2 = this.chunkZ + this.rand.nextInt(16) + 8;
            this.clayGen.generate(this.worldObj, this.rand, i4, this.worldObj.getTopSolidOrLiquidBlock(i4, k2), k2);
        }
        if(this.rand.nextInt(60) == 0) {
            i2 = this.chunkX + this.rand.nextInt(16) + 8;
            k = this.chunkZ + this.rand.nextInt(16) + 8;
            this.surfaceGravelGen.generate(this.worldObj, this.rand, i2, 0, k);
        }
        if(!biomeVariant.disableStructures && Math.abs(this.chunkX) > 32 && Math.abs(this.chunkZ) > 32) {
            boolean roadNear;
            long seed = this.chunkX * 1879267 ^ this.chunkZ * 67209689L;
            seed = seed * seed * 5829687L + seed * 2876L;
            this.structureRand.setSeed(seed);
            boolean bl = roadNear = LOTRRoads.isRoadNear(this.chunkX + 8, this.chunkZ + 8, 16) >= 0.0f;
            if(!roadNear) {
                for(RandomStructure randomstructure : this.randomStructures) {
                    if(this.structureRand.nextInt(randomstructure.chunkChance) != 0) continue;
                    int i6 = this.chunkX + this.rand.nextInt(16) + 8;
                    k7 = this.chunkZ + this.rand.nextInt(16) + 8;
                    j5 = this.worldObj.getTopSolidOrLiquidBlock(i6, k7);
                    randomstructure.structureGen.generate(this.worldObj, this.rand, i6, j5, k7);
                }
            }
            for(LOTRVillageGen village : this.villages) {
                village.generateInChunk(this.worldObj, this.chunkX, this.chunkZ);
            }
        }
        if(LOTRWorldGenMarshHut.generatesAt(this.worldObj, this.chunkX, this.chunkZ)) {
            i2 = this.chunkX + 8;
            k = this.chunkZ + 8;
            j4 = this.worldObj.getTopSolidOrLiquidBlock(i2, k);
            house = new LOTRWorldGenMarshHut();
            ((LOTRWorldGenStructureBase) house).restrictions = false;
            house.generate(this.worldObj, this.rand, i2, j4, k);
        }
        if(LOTRWorldGenGrukHouse.generatesAt(this.worldObj, this.chunkX, this.chunkZ)) {
            i2 = this.chunkX + 8;
            k = this.chunkZ + 8;
            j4 = this.worldObj.getTopSolidOrLiquidBlock(i2, k);
            house = new LOTRWorldGenGrukHouse(false);
            ((LOTRWorldGenStructureBase2) house).restrictions = false;
            ((LOTRWorldGenStructureBase2) house).generateWithSetRotation(this.worldObj, this.rand, i2, j4, k, 2);
        }
        if(LOTRWorldGenTicketBooth.generatesAt(this.worldObj, this.chunkX, this.chunkZ)) {
            i2 = this.chunkX + 8;
            k = this.chunkZ + 8;
            j4 = this.worldObj.getTopSolidOrLiquidBlock(i2, k);
            LOTRWorldGenTicketBooth booth = new LOTRWorldGenTicketBooth(false);
            booth.restrictions = false;
            ((LOTRWorldGenStructureBase2) booth).generateWithSetRotation(this.worldObj, this.rand, i2, j4, k, 3);
        }
        int trees = this.getVariantTreesPerChunk(biomeVariant);
        if(this.rand.nextFloat() < this.biome.getTreeIncreaseChance() * biomeVariant.treeFactor) {
            ++trees;
        }
        if((cluster = Math.round(this.treeClusterChance * (reciprocalTreeFactor = 1.0f / Math.max(biomeVariant.treeFactor, 0.001f)))) > 0) {
            Random chunkRand = new Random();
            long seed = this.chunkX / this.treeClusterSize * 3129871 ^ this.chunkZ / this.treeClusterSize * 116129781L;
            seed = seed * seed * 42317861L + seed * 11L;
            chunkRand.setSeed(seed);
            if(chunkRand.nextInt(cluster) == 0) {
                trees += 6 + this.rand.nextInt(5);
            }
        }
        for(l4 = 0; l4 < trees; ++l4) {
            int i7 = this.chunkX + this.rand.nextInt(16) + 8;
            k5 = this.chunkZ + this.rand.nextInt(16) + 8;
            WorldGenAbstractTree treeGen = this.getRandomTreeForVariant(this.rand, biomeVariant).create(false, this.rand);
            treeGen.generate(this.worldObj, this.rand, i7, this.worldObj.getHeightValue(i7, k5), k5);
        }
        for(l4 = 0; l4 < this.willowPerChunk; ++l4) {
            int i8 = this.chunkX + this.rand.nextInt(16) + 8;
            k5 = this.chunkZ + this.rand.nextInt(16) + 8;
            WorldGenAbstractTree treeGen = LOTRTreeType.WILLOW_WATER.create(false, this.rand);
            treeGen.generate(this.worldObj, this.rand, i8, this.worldObj.getHeightValue(i8, k5), k5);
        }
        if(trees > 0) {
            float fallenLeaves = trees / 2.0f;
            int fallenLeavesI = (int) fallenLeaves;
            float fallenLeavesR = fallenLeaves - fallenLeavesI;
            if(this.rand.nextFloat() < fallenLeavesR) {
                ++fallenLeavesI;
            }
            l7 = 0;
            while(l7 < fallenLeaves) {
                i = this.chunkX + this.rand.nextInt(16) + 8;
                k4 = this.chunkZ + this.rand.nextInt(16) + 8;
                new LOTRWorldGenFallenLeaves().generate(this.worldObj, this.rand, i, this.worldObj.getTopSolidOrLiquidBlock(i, k4), k4);
                ++l7;
            }
        }
        if(trees > 0) {
            float bushes = trees / 3.0f;
            int bushesI = (int) bushes;
            float bushesR = bushes - bushesI;
            if(this.rand.nextFloat() < bushesR) {
                ++bushesI;
            }
            l7 = 0;
            while(l7 < bushes) {
                i = this.chunkX + this.rand.nextInt(16) + 8;
                k4 = this.chunkZ + this.rand.nextInt(16) + 8;
                new LOTRWorldGenBushes().generate(this.worldObj, this.rand, i, this.worldObj.getTopSolidOrLiquidBlock(i, k4), k4);
                ++l7;
            }
        }
        for(l3 = 0; l3 < this.logsPerChunk; ++l3) {
            int i9 = this.chunkX + this.rand.nextInt(16) + 8;
            int k9 = this.chunkZ + this.rand.nextInt(16) + 8;
            this.logGen.generate(this.worldObj, this.rand, i9, this.worldObj.getHeightValue(i9, k9), k9);
        }
        for(l3 = 0; l3 < this.vinesPerChunk; ++l3) {
            int i10 = this.chunkX + this.rand.nextInt(16) + 8;
            int j7 = 64;
            k7 = this.chunkZ + this.rand.nextInt(16) + 8;
            this.vinesGen.generate(this.worldObj, this.rand, i10, j7, k7);
        }
        int flowers = this.flowersPerChunk;
        flowers = Math.round(flowers * biomeVariant.flowerFactor);
        for(int l8 = 0; l8 < flowers; ++l8) {
            int i11 = this.chunkX + this.rand.nextInt(16) + 8;
            int j8 = this.rand.nextInt(128);
            int k10 = this.chunkZ + this.rand.nextInt(16) + 8;
            this.flowerGen.generate(this.worldObj, this.rand, i11, j8, k10);
        }
        int doubleFlowers = this.doubleFlowersPerChunk;
        doubleFlowers = Math.round(doubleFlowers * biomeVariant.flowerFactor);
        for(int l9 = 0; l9 < doubleFlowers; ++l9) {
            int i12 = this.chunkX + this.rand.nextInt(16) + 8;
            j5 = this.rand.nextInt(128);
            k4 = this.chunkZ + this.rand.nextInt(16) + 8;
            WorldGenerator doubleFlowerGen = this.biome.getRandomWorldGenForDoubleFlower(this.rand);
            doubleFlowerGen.generate(this.worldObj, this.rand, i12, j5, k4);
        }
        int grasses = this.grassPerChunk;
        grasses = Math.round(grasses * biomeVariant.grassFactor);
        for(l7 = 0; l7 < grasses; ++l7) {
            i = this.chunkX + this.rand.nextInt(16) + 8;
            j3 = this.rand.nextInt(128);
            int k11 = this.chunkZ + this.rand.nextInt(16) + 8;
            WorldGenerator grassGen = this.biome.getRandomWorldGenForGrass(this.rand);
            grassGen.generate(this.worldObj, this.rand, i, j3, k11);
        }
        int doubleGrasses = this.doubleGrassPerChunk;
        doubleGrasses = Math.round(doubleGrasses * biomeVariant.grassFactor);
        for(l = 0; l < doubleGrasses; ++l) {
            i3 = this.chunkX + this.rand.nextInt(16) + 8;
            int j9 = this.rand.nextInt(128);
            int k12 = this.chunkZ + this.rand.nextInt(16) + 8;
            WorldGenerator grassGen = this.biome.getRandomWorldGenForDoubleGrass(this.rand);
            grassGen.generate(this.worldObj, this.rand, i3, j9, k12);
        }
        for(l = 0; l < this.deadBushPerChunk; ++l) {
            i3 = this.chunkX + this.rand.nextInt(16) + 8;
            int j10 = this.rand.nextInt(128);
            int k13 = this.chunkZ + this.rand.nextInt(16) + 8;
            new WorldGenDeadBush(Blocks.deadbush).generate(this.worldObj, this.rand, i3, j10, k13);
        }
        for(l = 0; l < this.waterlilyPerChunk; ++l) {
            int j11;
            i3 = this.chunkX + this.rand.nextInt(16) + 8;
            int k14 = this.chunkZ + this.rand.nextInt(16) + 8;
            for(j11 = this.rand.nextInt(128); j11 > 0 && this.worldObj.getBlock(i3, j11 - 1, k14) == Blocks.air; --j11) {
            }
            this.waterlilyGen.generate(this.worldObj, this.rand, i3, j11, k14);
        }
        for(l = 0; l < this.mushroomsPerChunk; ++l) {
            if(this.rand.nextInt(4) == 0) {
                i3 = this.chunkX + this.rand.nextInt(16) + 8;
                int k15 = this.chunkZ + this.rand.nextInt(16) + 8;
                int j12 = this.worldObj.getHeightValue(i3, k15);
                this.mushroomBrownGen.generate(this.worldObj, this.rand, i3, j12, k15);
            }
            if(this.rand.nextInt(8) != 0) continue;
            i3 = this.chunkX + this.rand.nextInt(16) + 8;
            k6 = this.chunkZ + this.rand.nextInt(16) + 8;
            j = this.worldObj.getHeightValue(i3, k6);
            this.mushroomRedGen.generate(this.worldObj, this.rand, i3, j, k6);
        }
        if(this.enableRandomMushroom) {
            if(this.rand.nextInt(4) == 0) {
                i = this.chunkX + this.rand.nextInt(16) + 8;
                j3 = this.rand.nextInt(128);
                int k16 = this.chunkZ + this.rand.nextInt(16) + 8;
                this.mushroomBrownGen.generate(this.worldObj, this.rand, i, j3, k16);
            }
            if(this.rand.nextInt(8) == 0) {
                i = this.chunkX + this.rand.nextInt(16) + 8;
                j3 = this.rand.nextInt(128);
                k6 = this.chunkZ + this.rand.nextInt(16) + 8;
                this.mushroomRedGen.generate(this.worldObj, this.rand, i, j3, k6);
            }
        }
        for(l = 0; l < this.canePerChunk; ++l) {
            i3 = this.chunkX + this.rand.nextInt(16) + 8;
            j2 = this.rand.nextInt(128);
            int k17 = this.chunkZ + this.rand.nextInt(16) + 8;
            this.caneGen.generate(this.worldObj, this.rand, i3, j2, k17);
        }
        for(l = 0; l < 10; ++l) {
            i3 = this.chunkX + this.rand.nextInt(16) + 8;
            j2 = this.rand.nextInt(128);
            int k18 = this.chunkZ + this.rand.nextInt(16) + 8;
            this.caneGen.generate(this.worldObj, this.rand, i3, j2, k18);
        }
        for(l = 0; l < this.reedPerChunk; ++l) {
            int j13;
            i3 = this.chunkX + this.rand.nextInt(16) + 8;
            k6 = this.chunkZ + this.rand.nextInt(16) + 8;
            for(j13 = this.rand.nextInt(128); j13 > 0 && this.worldObj.getBlock(i3, j13 - 1, k6) == Blocks.air; --j13) {
            }
            if(this.rand.nextFloat() < this.dryReedChance) {
                this.dryReedGen.generate(this.worldObj, this.rand, i3, j13, k6);
                continue;
            }
            this.reedGen.generate(this.worldObj, this.rand, i3, j13, k6);
        }
        for(l = 0; l < this.cornPerChunk; ++l) {
            i3 = this.chunkX + this.rand.nextInt(16) + 8;
            j2 = this.rand.nextInt(128);
            int k19 = this.chunkZ + this.rand.nextInt(16) + 8;
            this.cornGen.generate(this.worldObj, this.rand, i3, j2, k19);
        }
        for(l = 0; l < this.cactiPerChunk; ++l) {
            i3 = this.chunkX + this.rand.nextInt(16) + 8;
            j2 = this.rand.nextInt(128);
            int k20 = this.chunkZ + this.rand.nextInt(16) + 8;
            this.cactusGen.generate(this.worldObj, this.rand, i3, j2, k20);
        }
        if(this.melonPerChunk > 0.0f) {
            int melonInt = MathHelper.floor_double(this.melonPerChunk);
            float melonF = this.melonPerChunk - melonInt;
            for(l5 = 0; l5 < melonInt; ++l5) {
                int i13 = this.chunkX + this.rand.nextInt(16) + 8;
                int k21 = this.chunkZ + this.rand.nextInt(16) + 8;
                int j14 = this.worldObj.getHeightValue(i13, k21);
                this.melonGen.generate(this.worldObj, this.rand, i13, j14, k21);
            }
            if(this.rand.nextFloat() < melonF) {
                i5 = this.chunkX + this.rand.nextInt(16) + 8;
                int k22 = this.chunkZ + this.rand.nextInt(16) + 8;
                int j15 = this.worldObj.getHeightValue(i5, k22);
                this.melonGen.generate(this.worldObj, this.rand, i5, j15, k22);
            }
        }
        if(this.flowersPerChunk > 0 && this.rand.nextInt(32) == 0) {
            i = this.chunkX + this.rand.nextInt(16) + 8;
            j3 = this.rand.nextInt(128);
            k6 = this.chunkZ + this.rand.nextInt(16) + 8;
            this.pumpkinGen.generate(this.worldObj, this.rand, i, j3, k6);
        }
        if(this.flowersPerChunk > 0 && this.rand.nextInt(4) == 0) {
            i = this.chunkX + this.rand.nextInt(16) + 8;
            j3 = this.rand.nextInt(128);
            k6 = this.chunkZ + this.rand.nextInt(16) + 8;
            new LOTRWorldGenBerryBush().generate(this.worldObj, this.rand, i, j3, k6);
        }
        if(this.generateAthelas && this.rand.nextInt(30) == 0) {
            i = this.chunkX + this.rand.nextInt(16) + 8;
            j3 = this.rand.nextInt(128);
            k6 = this.chunkZ + this.rand.nextInt(16) + 8;
            new WorldGenFlowers(LOTRMod.athelas).generate(this.worldObj, this.rand, i, j3, k6);
        }
        if(this.generateWater) {
            int k23;
            LOTRWorldGenStreams waterGen = new LOTRWorldGenStreams(Blocks.flowing_water);
            for(l6 = 0; l6 < 50; ++l6) {
                i5 = this.chunkX + this.rand.nextInt(16) + 8;
                j = this.rand.nextInt(this.rand.nextInt(120) + 8);
                k23 = this.chunkZ + this.rand.nextInt(16) + 8;
                ((WorldGenerator) waterGen).generate(this.worldObj, this.rand, i5, j, k23);
            }
            if(this.biome.rootHeight > 1.0f) {
                for(l6 = 0; l6 < 50; ++l6) {
                    i5 = this.chunkX + this.rand.nextInt(16) + 8;
                    j = 100 + this.rand.nextInt(150);
                    k23 = this.chunkZ + this.rand.nextInt(16) + 8;
                    ((WorldGenerator) waterGen).generate(this.worldObj, this.rand, i5, j, k23);
                }
            }
        }
        if(this.generateLava) {
            LOTRWorldGenStreams lavaGen = new LOTRWorldGenStreams(Blocks.flowing_lava);
            int lava = 20;
            if(this.biome instanceof LOTRBiomeGenMordor) {
                lava = 50;
            }
            for(l5 = 0; l5 < lava; ++l5) {
                int i14 = this.chunkX + this.rand.nextInt(16) + 8;
                int j16 = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(112) + 8) + 8);
                int k24 = this.chunkZ + this.rand.nextInt(16) + 8;
                ((WorldGenerator) lavaGen).generate(this.worldObj, this.rand, i14, j16, k24);
            }
        }
        if(this.generateOrcDungeon) {
            for(l = 0; l < 6; ++l) {
                i3 = this.chunkX + this.rand.nextInt(16) + 8;
                j2 = this.rand.nextInt(128);
                k3 = this.chunkZ + this.rand.nextInt(16) + 8;
                this.orcDungeonGen.generate(this.worldObj, this.rand, i3, j2, k3);
            }
        }
        if(this.generateTrollHoard) {
            for(l = 0; l < 2; ++l) {
                i3 = this.chunkX + this.rand.nextInt(16) + 8;
                j2 = MathHelper.getRandomIntegerInRange(this.rand, 36, 90);
                k3 = this.chunkZ + this.rand.nextInt(16) + 8;
                this.trollHoardGen.generate(this.worldObj, this.rand, i3, j2, k3);
            }
        }
        if(biomeVariant.boulderGen != null && this.rand.nextInt(biomeVariant.boulderChance) == 0) {
            int boulders = MathHelper.getRandomIntegerInRange(this.rand, 1, biomeVariant.boulderMax);
            for(l6 = 0; l6 < boulders; ++l6) {
                i5 = this.chunkX + this.rand.nextInt(16) + 8;
                k3 = this.chunkZ + this.rand.nextInt(16) + 8;
                biomeVariant.boulderGen.generate(this.worldObj, this.rand, i5, this.worldObj.getHeightValue(i5, k3), k3);
            }
        }
    }

    private void generateOres() {
        float f;
        for(OreGenerant soil : this.biomeSoils) {
            this.genStandardOre(soil.oreChance, soil.oreGen, soil.minHeight, soil.maxHeight);
        }
        for(OreGenerant ore : this.biomeOres) {
            f = ore.oreChance * this.biomeOreFactor;
            this.genStandardOre(f, ore.oreGen, ore.minHeight, ore.maxHeight);
        }
        for(OreGenerant gem : this.biomeGems) {
            f = gem.oreChance * this.biomeGemFactor;
            this.genStandardOre(f, gem.oreGen, gem.minHeight, gem.maxHeight);
        }
    }

    private void genStandardOre(float ores, WorldGenerator oreGen, int minHeight, int maxHeight) {
        while(ores > 0.0f) {
            boolean generate = ores >= 1.0f ? true : this.rand.nextFloat() < ores;
            ores -= 1.0f;
            if(!generate) continue;
            int i = this.chunkX + this.rand.nextInt(16);
            int j = MathHelper.getRandomIntegerInRange(this.rand, minHeight, maxHeight);
            int k = this.chunkZ + this.rand.nextInt(16);
            oreGen.generate(this.worldObj, this.rand, i, j, k);
        }
    }

    private class RandomStructure {
        public WorldGenerator structureGen;
        public int chunkChance;

        public RandomStructure(WorldGenerator w, int i) {
            this.structureGen = w;
            this.chunkChance = i;
        }
    }

    private class OreGenerant {
        private WorldGenerator oreGen;
        private float oreChance;
        private int minHeight;
        private int maxHeight;

        public OreGenerant(WorldGenerator gen, float f, int min, int max) {
            this.oreGen = gen;
            this.oreChance = f;
            this.minHeight = min;
            this.maxHeight = max;
        }
    }

}
