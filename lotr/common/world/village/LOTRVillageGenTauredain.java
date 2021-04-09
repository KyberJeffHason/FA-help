package lotr.common.world.village;

import java.util.Random;
import lotr.common.world.biome.LOTRBiome;
import lotr.common.world.map.LOTRRoadType;
import lotr.common.world.structure2.*;
import net.minecraft.world.World;

public class LOTRVillageGenTauredain extends LOTRVillageGen {
    public LOTRVillageGenTauredain(LOTRBiome biome, float f) {
        super(biome);
        this.gridScale = 10;
        this.gridRandomDisplace = 1;
        this.spawnChance = f;
        this.villageChunkSize = 3;
    }

    @Override
    protected LOTRVillageGen.AbstractInstance createVillageInstance(World world, int i, int k, Random random) {
        return new Instance(world, i, k, random);
    }

    public class Instance extends LOTRVillageGen.AbstractInstance {
        public Instance(World world, int i, int k, Random random) {
            super(world, i, k, random);
        }

        @Override
        protected void setupVillageProperties(Random random) {
        }

        @Override
        public boolean isFlat() {
            return false;
        }

        @Override
        protected void addVillageStructures(Random random) {
            int smithyPos = random.nextInt(4);
            this.addStructure(new LOTRWorldGenTauredainChieftainPyramid(false), 0, -11, 0, true);
            this.addStructure(new LOTRWorldGenTauredainVillageTree(false), 0, -16, 2);
            this.addStructure(new LOTRWorldGenTauredainVillageFarm(false), -16, -19, 2);
            this.addStructure(new LOTRWorldGenTauredainVillageFarm(false), 16, -19, 2);
            this.addStructure(new LOTRWorldGenTauredainHouseStilts(false), 0, 15, 0);
            this.addStructure(new LOTRWorldGenTauredainVillageFarm(false), -16, 19, 0);
            this.addStructure(new LOTRWorldGenTauredainVillageFarm(false), 16, 19, 0);
            this.addStructure(new LOTRWorldGenTauredainHouseLarge(false), -20, 0, 1);
            this.addStructure(new LOTRWorldGenTauredainHouseLarge(false), 20, 1, 3);
            this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), -15, -36, 0);
            this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), 15, -36, 0);
            if(smithyPos == 0) {
                this.addStructure(new LOTRWorldGenTauredainSmithy(false), -22, -13, 1);
            }
            else {
                this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), -32, -22, 3);
                this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), -32, -12, 3);
            }
            if(smithyPos == 1) {
                this.addStructure(new LOTRWorldGenTauredainSmithy(false), -22, 14, 1);
            }
            else {
                this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), -32, 13, 3);
                this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), -32, 23, 3);
            }
            if(smithyPos == 2) {
                this.addStructure(new LOTRWorldGenTauredainSmithy(false), 22, -13, 3);
            }
            else {
                this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), 32, -22, 1);
                this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), 32, -12, 1);
            }
            if(smithyPos == 3) {
                this.addStructure(new LOTRWorldGenTauredainSmithy(false), 22, 14, 3);
            }
            else {
                this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), 32, 13, 1);
                this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), 32, 23, 1);
            }
            this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), -15, 36, 2);
            this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), 0, 37, 2);
            this.addStructure(new LOTRWorldGenTauredainHouseSimple(false), 15, 36, 2);
            this.addStructure(new LOTRWorldGenTauredainWatchtower(false), -26, -36, 0);
            this.addStructure(new LOTRWorldGenTauredainWatchtower(false), 26, -36, 0);
            this.addStructure(new LOTRWorldGenTauredainWatchtower(false), -26, 37, 2);
            this.addStructure(new LOTRWorldGenTauredainWatchtower(false), 26, 37, 2);
        }

        @Override
        protected LOTRRoadType getPath(Random random, int i, int k) {
            return null;
        }

        @Override
        public boolean isVillageSurface(World world, int i, int j, int k) {
            return false;
        }
    }

}
