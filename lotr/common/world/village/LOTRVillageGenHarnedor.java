package lotr.common.world.village;

import java.util.Random;
import lotr.common.entity.LOTREntityNPCRespawner;
import lotr.common.entity.npc.*;
import lotr.common.world.biome.LOTRBiome;
import lotr.common.world.map.LOTRRoadType;
import lotr.common.world.structure2.*;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class LOTRVillageGenHarnedor extends LOTRVillageGen {
    private boolean isRuinedVillage;

    public LOTRVillageGenHarnedor(LOTRBiome biome, float f) {
        super(biome);
        this.gridScale = 12;
        this.gridRandomDisplace = 1;
        this.spawnChance = f;
        this.villageChunkSize = 4;
    }

    public LOTRVillageGenHarnedor setRuined() {
        this.isRuinedVillage = true;
        return this;
    }

    @Override
    protected LOTRVillageGen.AbstractInstance createVillageInstance(World world, int i, int k, Random random) {
        return new Instance(world, i, k, random);
    }

    public class Instance extends LOTRVillageGen.AbstractInstance {
        public VillageType villageType;
        public String[] villageName;
        private int numOuterHouses;
        private static final int pathInner = 17;
        private static final int roadWidth = 5;
        private static final int pathFuzz = 3;
        private static final int rHouses = 25;
        private static final int rFarms = 45;
        private boolean palisade;
        private static final int rPalisade = 61;
        private static final int rTowers = 24;
        private static final int rFortPalisade = 42;

        public Instance(World world, int i, int k, Random random) {
            super(world, i, k, random);
        }

        @Override
        protected void setupVillageProperties(Random random) {
            this.villageType = random.nextInt(4) == 0 ? VillageType.FORTRESS : VillageType.VILLAGE;
            this.villageName = LOTRNames.getHaradVillageName(random);
            this.numOuterHouses = MathHelper.getRandomIntegerInRange(random, 5, 8);
            this.palisade = random.nextInt(3) != 0;
        }

        @Override
        public boolean isFlat() {
            return false;
        }

        @Override
        protected void addVillageStructures(Random random) {
            if(this.villageType == VillageType.VILLAGE) {
                this.setupVillage(random);
            }
            else {
                this.setupFortress(random);
            }
        }

        private void setupVillage(Random random) {
            if(!LOTRVillageGenHarnedor.this.isRuinedVillage) {
                this.addStructure(new LOTRWorldGenNPCRespawner(false) {

                    @Override
                    public void setupRespawner(LOTREntityNPCRespawner spawner) {
                        spawner.setSpawnClass(LOTREntityHarnedhrim.class);
                        spawner.setCheckRanges(64, -12, 12, 24);
                        spawner.setSpawnRanges(32, -6, 6, 32);
                        spawner.setBlockEnemySpawnRange(64);
                    }
                }, 0, 0, 0);
                this.addStructure(new LOTRWorldGenNPCRespawner(false) {

                    @Override
                    public void setupRespawner(LOTREntityNPCRespawner spawner) {
                        spawner.setSpawnClasses(LOTREntityHarnedorWarrior.class, LOTREntityHarnedorArcher.class);
                        spawner.setCheckRanges(64, -12, 12, 12);
                        spawner.setSpawnRanges(32, -6, 6, 32);
                        spawner.setBlockEnemySpawnRange(64);
                    }
                }, 0, 0, 0);
            }
            if(LOTRVillageGenHarnedor.this.isRuinedVillage) {
                this.addStructure(new LOTRWorldGenHarnedorTavernRuined(false), 3, -7, 0, true);
            }
            else if(random.nextBoolean()) {
                this.addStructure(new LOTRWorldGenHarnedorMarket(false), 0, -8, 0, true);
            }
            else {
                this.addStructure(new LOTRWorldGenHarnedorTavern(false), 3, -7, 0, true);
            }
            float frac = 1.0f / this.numOuterHouses;
            float turn = 0.0f;
            while(turn < 1.0f) {
                float turnR = (float) Math.toRadians((turn += frac) * 360.0f);
                float sin = MathHelper.sin(turnR);
                float cos = MathHelper.cos(turnR);
                int r = 0;
                float turn8 = turn * 8.0f;
                if(turn8 >= 1.0f && turn8 < 3.0f) {
                    r = 0;
                }
                else if(turn8 >= 3.0f && turn8 < 5.0f) {
                    r = 1;
                }
                else if(turn8 >= 5.0f && turn8 < 7.0f) {
                    r = 2;
                }
                else if(turn8 >= 7.0f || turn8 < 1.0f) {
                    r = 3;
                }
                int l = 25;
                int i = Math.round(l * cos);
                int k = Math.round(l * sin);
                if(this.palisade && k < 0 && Math.abs(i) < 10) continue;
                this.addStructure(this.getRandomHouse(random), i, k, r);
            }
            if(!LOTRVillageGenHarnedor.this.isRuinedVillage) {
                int numFarms = this.numOuterHouses * 2;
                frac = 1.0f / numFarms;
                turn = 0.0f;
                while(turn < 1.0f) {
                    float turnR = (float) Math.toRadians((turn += frac) * 360.0f);
                    float sin = MathHelper.sin(turnR);
                    float cos = MathHelper.cos(turnR);
                    int r = 0;
                    float turn8 = turn * 8.0f;
                    if(turn8 >= 1.0f && turn8 < 3.0f) {
                        r = 0;
                    }
                    else if(turn8 >= 3.0f && turn8 < 5.0f) {
                        r = 1;
                    }
                    else if(turn8 >= 5.0f && turn8 < 7.0f) {
                        r = 2;
                    }
                    else if(turn8 >= 7.0f || turn8 < 1.0f) {
                        r = 3;
                    }
                    int l = 45;
                    int i = Math.round(l * cos);
                    int k = Math.round(l * sin);
                    if(this.palisade && k < 0 && Math.abs(i) < 10) continue;
                    if(random.nextInt(3) == 0) {
                        this.addStructure(new LOTRWorldGenHayBales(false), i, k, r);
                        continue;
                    }
                    if(random.nextInt(3) == 0) {
                        this.addStructure(new LOTRWorldGenHarnedorPasture(false), i, k, r);
                        continue;
                    }
                    this.addStructure(new LOTRWorldGenHarnedorFarm(false), i, k, r);
                }
            }
            if(!LOTRVillageGenHarnedor.this.isRuinedVillage) {
                if(this.palisade) {
                    this.addStructure(new LOTRWorldGenHarnedorVillageSign(false).setSignText(this.villageName), 5 * (random.nextBoolean() ? 1 : -1), -56, 0, true);
                }
                else {
                    this.addStructure(new LOTRWorldGenHarnedorVillageSign(false).setSignText(this.villageName), 0, -16, 0, true);
                }
            }
            if(this.palisade) {
                int rSq = 3721;
                int rMax = 62;
                int rSqMax = rMax * rMax;
                for(int i = -61; i <= 61; ++i) {
                    for(int k = -61; k <= 61; ++k) {
                        int dSq;
                        LOTRWorldGenHarnedorPalisade palisade;
                        int i1 = Math.abs(i);
                        if(i1 <= 4 && k < 0 || (dSq = i * i + k * k) < rSq || dSq >= rSqMax) continue;
                        if(LOTRVillageGenHarnedor.this.isRuinedVillage) {
                            if(random.nextBoolean()) continue;
                            palisade = new LOTRWorldGenHarnedorPalisadeRuined(false);
                        }
                        else {
                            palisade = new LOTRWorldGenHarnedorPalisade(false);
                        }
                        if(i1 == 5 && k < 0) {
                            palisade.setTall();
                        }
                        this.addStructure(palisade, i, k, 0);
                    }
                }
            }
        }

        private LOTRWorldGenStructureBase2 getRandomHouse(Random random) {
            if(LOTRVillageGenHarnedor.this.isRuinedVillage) {
                return new LOTRWorldGenHarnedorHouseRuined(false);
            }
            if(random.nextInt(5) == 0) {
                return new LOTRWorldGenHarnedorSmithy(false);
            }
            if(random.nextInt(4) == 0) {
                return new LOTRWorldGenHarnedorStables(false);
            }
            return new LOTRWorldGenHarnedorHouse(false);
        }

        private void setupFortress(Random random) {
            this.addStructure(new LOTRWorldGenNPCRespawner(false) {

                @Override
                public void setupRespawner(LOTREntityNPCRespawner spawner) {
                    spawner.setSpawnClass(LOTREntityHarnedhrim.class);
                    spawner.setCheckRanges(64, -12, 12, 16);
                    spawner.setSpawnRanges(24, -6, 6, 32);
                    spawner.setBlockEnemySpawnRange(50);
                }
            }, 0, 0, 0);
            this.addStructure(new LOTRWorldGenHarnedorFort(false), 0, -12, 0, true);
            this.addStructure(new LOTRWorldGenHarnedorTower(false), -24, -24, 0, true);
            this.addStructure(new LOTRWorldGenHarnedorTower(false), 24, -24, 0, true);
            this.addStructure(new LOTRWorldGenHarnedorTower(false), -24, 24, 2, true);
            this.addStructure(new LOTRWorldGenHarnedorTower(false), 24, 24, 2, true);
            for(int l = -1; l <= 1; ++l) {
                int k = l * 10;
                int i = 24;
                this.addStructure(new LOTRWorldGenNearHaradTent(false), -i, k, 1, true);
                this.addStructure(new LOTRWorldGenNearHaradTent(false), i, k, 3, true);
            }
            int rSq = 1764;
            int rMax = 43;
            int rSqMax = rMax * rMax;
            for(int i = -42; i <= 42; ++i) {
                for(int k = -42; k <= 42; ++k) {
                    int dSq;
                    int i1 = Math.abs(i);
                    if(i1 <= 4 && k < 0 || (dSq = i * i + k * k) < rSq || dSq >= rSqMax) continue;
                    LOTRWorldGenHarnedorPalisade palisade = new LOTRWorldGenHarnedorPalisade(false);
                    if(i1 == 5 && k < 0) {
                        palisade.setTall();
                    }
                    this.addStructure(palisade, i, k, 0);
                }
            }
        }

        @Override
        protected LOTRRoadType getPath(Random random, int i, int k) {
            int i1 = Math.abs(i);
            int k1 = Math.abs(k);
            if(this.villageType == VillageType.VILLAGE) {
                if(LOTRVillageGenHarnedor.this.isRuinedVillage && random.nextInt(4) == 0) {
                    return null;
                }
                int dSq = i * i + k * k;
                int imn = 17 - random.nextInt(3);
                int imx = 22 + random.nextInt(3);
                if(dSq > imn * imn && dSq < imx * imx) {
                    return LOTRRoadType.PATH;
                }
                if(this.palisade && k <= -imx && k >= -66 && i1 < 2 + random.nextInt(3)) {
                    return LOTRRoadType.PATH;
                }
            }
            return null;
        }

        @Override
        public boolean isVillageSurface(World world, int i, int j, int k) {
            return false;
        }

    }

    public static enum VillageType {
        VILLAGE, FORTRESS;

    }

}
