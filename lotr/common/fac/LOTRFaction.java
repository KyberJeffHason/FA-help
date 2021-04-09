package lotr.common.fac;

import java.awt.Color;
import java.util.*;
import lotr.common.*;
import lotr.common.entity.LOTRNPCSelectForInfluence;
import lotr.common.item.LOTRItemBanner;
import lotr.common.world.LOTRWorldProvider;
import lotr.common.world.map.LOTRWaypoint;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.world.World;

public enum LOTRFaction {
    HOBBIT(5885518, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(830, 745, 100), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_MAN)),
    RANGER_NORTH(3823170, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1070, 760, 150), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_MAN)),
    BLUE_MOUNTAINS(6132172, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(650, 600, 125), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_DWARF)),
    HIGH_ELF(13035007, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(570, 770, 200), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_ELF)),
    GUNDABAD(9858132, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1160, 670, 150), EnumSet.of(FactionType.TYPE_ORC)),
    ANGMAR(7836023, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1080, 600, 125), EnumSet.of(FactionType.TYPE_ORC, FactionType.TYPE_TROLL)),
    WOOD_ELF(3774030, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1400, 640, 75), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_ELF)),
    DOL_GULDUR(3488580, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1380, 870, 100), EnumSet.of(FactionType.TYPE_ORC)),
    DALE(13535071, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1530, 670, 100), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_MAN)),
    DWARF(4940162, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1650, 650, 125), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_DWARF)),
    GALADHRIM(15716696, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1230, 900, 75), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_ELF)),
    DUNLAND(11048079, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1090, 1030, 125), EnumSet.of(FactionType.TYPE_MAN)),
    URUK_HAI(3356723, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1110, 1070, 50), EnumSet.of(FactionType.TYPE_ORC)),
    FANGORN(4831058, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1200, 1000, 75), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_TREE)),
    ROHAN(3508007, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1230, 1090, 150), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_MAN)),
    GONDOR(16382457, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1170, 1300, 300), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_MAN)),
    MORDOR(3481375, LOTRDimension.DimensionRegion.WEST, new LOTRMapRegion(1620, 1290, 225), EnumSet.of(FactionType.TYPE_ORC)),
    DORWINION(7155816, LOTRDimension.DimensionRegion.EAST, new LOTRMapRegion(1750, 900, 100), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_MAN, FactionType.TYPE_ELF)),
    RHUN(12882471, LOTRDimension.DimensionRegion.EAST, new LOTRMapRegion(1890, 980, 200), EnumSet.of(FactionType.TYPE_MAN)),
    NEAR_HARAD(11868955, LOTRDimension.DimensionRegion.SOUTH, new LOTRMapRegion(1400, 1730, 375), EnumSet.of(FactionType.TYPE_MAN)),
    MOREDAIN(14266458, LOTRDimension.DimensionRegion.SOUTH, new LOTRMapRegion(1400, 2360, 450), EnumSet.of(FactionType.TYPE_MAN)),
    TAUREDAIN(3040066, LOTRDimension.DimensionRegion.SOUTH, new LOTRMapRegion(1250, 2870, 400), EnumSet.of(FactionType.TYPE_FREE, FactionType.TYPE_MAN)),
    HALF_TROLL(10388339, LOTRDimension.DimensionRegion.SOUTH, new LOTRMapRegion(1900, 2500, 200), EnumSet.of(FactionType.TYPE_MAN, FactionType.TYPE_TROLL)),
    DARK_HUORN(0, null, null, true, true, -1, null, null),
    UTUMNO(3343616, LOTRDimension.UTUMNO, -66666, EnumSet.of(FactionType.TYPE_ORC)),
    HOSTILE(true, -1),
    UNALIGNED(false, 0);

    private static Random factionRand;
    public LOTRDimension factionDimension;
    public LOTRDimension.DimensionRegion factionRegion;
    private String factionName;
    private Color factionColor;
    private Set<FactionType> factionTypes = new HashSet<FactionType>();
    public List<LOTRItemBanner.BannerType> factionBanners = new ArrayList<LOTRItemBanner.BannerType>();
    public boolean allowPlayer;
    public boolean allowEntityRegistry;
    public boolean hasFixedAlignment;
    public int fixedAlignment;
    private List<LOTRFactionRank> ranksSortedDescending = new ArrayList<LOTRFactionRank>();
    private LOTRFactionRank pledgeRank;
    private LOTRAchievement.Category achieveCategory;
    public LOTRMapRegion factionMapInfo;
    private List<LOTRControlZone> controlZones = new ArrayList<LOTRControlZone>();
    public boolean isolationist = false;
    public boolean approvesWarCrimes = true;
    public static final int CONTROL_ZONE_EXTRA_RANGE = 50;

    private LOTRFaction(int color, LOTRDimension.DimensionRegion region, LOTRMapRegion mapInfo, EnumSet<FactionType> types) {
        this(color, LOTRDimension.MIDDLE_EARTH, region, mapInfo, types);
    }

    private LOTRFaction(int color, LOTRDimension dim, LOTRDimension.DimensionRegion region, LOTRMapRegion mapInfo, EnumSet<FactionType> types) {
        this(color, dim, region, true, true, Integer.MIN_VALUE, mapInfo, types);
    }

    private LOTRFaction(int color, LOTRDimension dim, int alignment, EnumSet<FactionType> types) {
        this(color, dim, dim.dimensionRegions.get(0), true, true, alignment, null, types);
    }

    private LOTRFaction(boolean registry, int alignment) {
        this(0, null, null, false, registry, alignment, null, null);
    }

    private LOTRFaction(int color, LOTRDimension dim, LOTRDimension.DimensionRegion region, boolean player, boolean registry, int alignment, LOTRMapRegion mapInfo, EnumSet<FactionType> types) {
        this.allowPlayer = player;
        this.allowEntityRegistry = registry;
        this.factionColor = new Color(color);
        this.factionDimension = dim;
        if(this.factionDimension != null) {
            this.factionDimension.factionList.add(this);
        }
        this.factionRegion = region;
        if(this.factionRegion != null) {
            this.factionRegion.factionList.add(this);
            if(this.factionRegion.getDimension() != this.factionDimension) {
                throw new IllegalArgumentException("Faction dimension region must agree with faction dimension!");
            }
        }
        if(alignment != Integer.MIN_VALUE) {
            this.setFixedAlignment(alignment);
        }
        if(mapInfo != null) {
            this.factionMapInfo = mapInfo;
        }
        if(types != null) {
            this.factionTypes.addAll(types);
        }
    }

    private void setFixedAlignment(int alignment) {
        this.hasFixedAlignment = true;
        this.fixedAlignment = alignment;
    }

    private void setAchieveCategory(LOTRAchievement.Category cat) {
        this.achieveCategory = cat;
    }

    public LOTRAchievement.Category getAchieveCategory() {
        return this.achieveCategory;
    }

    private LOTRFactionRank addRank(float alignment, String name) {
        return this.addRank(alignment, name, false);
    }

    private LOTRFactionRank addRank(float alignment, String name, boolean gendered) {
        LOTRFactionRank rank = new LOTRFactionRank(this, alignment, name, gendered);
        this.ranksSortedDescending.add(rank);
        Collections.sort(this.ranksSortedDescending);
        return rank;
    }

    public void setPledgeRank(LOTRFactionRank rank) {
        if(rank.fac == this) {
            if(this.pledgeRank != null) {
                throw new IllegalArgumentException("Faction already has a pledge rank!");
            }
        }
        else {
            throw new IllegalArgumentException("Incompatible faction!");
        }
        this.pledgeRank = rank;
    }

    public LOTRFactionRank getPledgeRank() {
        if(this.pledgeRank != null) {
            return this.pledgeRank;
        }
        return null;
    }

    public float getPledgeAlignment() {
        if(this.pledgeRank != null) {
            return this.pledgeRank.alignment;
        }
        return 0.0f;
    }

    public void checkAlignmentAchievements(EntityPlayer entityplayer, float alignment) {
        LOTRPlayerData playerData = LOTRLevelData.getData(entityplayer);
        for(LOTRFactionRank rank : this.ranksSortedDescending) {
            LOTRAchievementRank rankAch = rank.getRankAchievement();
            if(rankAch == null || !rankAch.isPlayerRequiredRank(entityplayer)) continue;
            playerData.addAchievement(rankAch);
        }
    }

    private void addControlZone(LOTRControlZone zone) {
        this.controlZones.add(zone);
    }

    public List<LOTRControlZone> getControlZones() {
        return this.controlZones;
    }

    public static void initAllProperties() {
        LOTRFactionRelations.setDefaultRelations(HOBBIT, RANGER_NORTH, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, BLUE_MOUNTAINS, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, HIGH_ELF, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, WOOD_ELF, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, DALE, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, DWARF, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, GALADHRIM, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, ROHAN, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, GONDOR, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, HIGH_ELF, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, WOOD_ELF, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, GALADHRIM, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, ROHAN, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, GONDOR, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(BLUE_MOUNTAINS, DWARF, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(HIGH_ELF, WOOD_ELF, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(HIGH_ELF, GALADHRIM, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(HIGH_ELF, FANGORN, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(HIGH_ELF, GONDOR, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(GUNDABAD, ANGMAR, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(GUNDABAD, DOL_GULDUR, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(GUNDABAD, MORDOR, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(ANGMAR, DOL_GULDUR, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(ANGMAR, MORDOR, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(WOOD_ELF, GALADHRIM, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(WOOD_ELF, FANGORN, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(WOOD_ELF, DORWINION, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(DOL_GULDUR, MORDOR, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(DALE, DWARF, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(DALE, ROHAN, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(DALE, GONDOR, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(DWARF, DUNLAND, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(GALADHRIM, FANGORN, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(DUNLAND, URUK_HAI, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(URUK_HAI, HALF_TROLL, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(FANGORN, TAUREDAIN, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(ROHAN, GONDOR, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(MORDOR, RHUN, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(MORDOR, NEAR_HARAD, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(MORDOR, MOREDAIN, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(MORDOR, HALF_TROLL, LOTRFactionRelations.Relation.ALLY);
        LOTRFactionRelations.setDefaultRelations(NEAR_HARAD, MOREDAIN, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(NEAR_HARAD, HALF_TROLL, LOTRFactionRelations.Relation.FRIEND);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, GUNDABAD, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, ANGMAR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, DOL_GULDUR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, URUK_HAI, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, MORDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(HOBBIT, DARK_HUORN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, GUNDABAD, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, ANGMAR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, DOL_GULDUR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, DUNLAND, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, URUK_HAI, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, MORDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, RHUN, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, NEAR_HARAD, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, MOREDAIN, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(RANGER_NORTH, DARK_HUORN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(BLUE_MOUNTAINS, GUNDABAD, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(BLUE_MOUNTAINS, ANGMAR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(BLUE_MOUNTAINS, DOL_GULDUR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(BLUE_MOUNTAINS, URUK_HAI, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(BLUE_MOUNTAINS, MORDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(BLUE_MOUNTAINS, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(HIGH_ELF, GUNDABAD, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(HIGH_ELF, ANGMAR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(HIGH_ELF, DOL_GULDUR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(HIGH_ELF, URUK_HAI, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(HIGH_ELF, MORDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(HIGH_ELF, RHUN, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(HIGH_ELF, NEAR_HARAD, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(HIGH_ELF, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GUNDABAD, WOOD_ELF, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GUNDABAD, DALE, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GUNDABAD, DWARF, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GUNDABAD, GALADHRIM, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GUNDABAD, FANGORN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GUNDABAD, ROHAN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GUNDABAD, GONDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GUNDABAD, DORWINION, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(ANGMAR, WOOD_ELF, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(ANGMAR, DALE, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(ANGMAR, DWARF, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(ANGMAR, GALADHRIM, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(ANGMAR, FANGORN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(ANGMAR, ROHAN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(ANGMAR, GONDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(ANGMAR, DORWINION, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(WOOD_ELF, DOL_GULDUR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(WOOD_ELF, URUK_HAI, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(WOOD_ELF, MORDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(WOOD_ELF, RHUN, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(WOOD_ELF, NEAR_HARAD, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(WOOD_ELF, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DOL_GULDUR, DALE, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DOL_GULDUR, DWARF, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DOL_GULDUR, GALADHRIM, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DOL_GULDUR, FANGORN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DOL_GULDUR, ROHAN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DOL_GULDUR, GONDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DOL_GULDUR, DORWINION, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DALE, URUK_HAI, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DALE, MORDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DALE, RHUN, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(DALE, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DWARF, URUK_HAI, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DWARF, MORDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DWARF, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GALADHRIM, URUK_HAI, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GALADHRIM, MORDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GALADHRIM, RHUN, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(GALADHRIM, NEAR_HARAD, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(GALADHRIM, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DUNLAND, ROHAN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DUNLAND, GONDOR, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(URUK_HAI, FANGORN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(URUK_HAI, ROHAN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(URUK_HAI, GONDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(URUK_HAI, DORWINION, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(FANGORN, MORDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(FANGORN, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(ROHAN, MORDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(ROHAN, RHUN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(ROHAN, NEAR_HARAD, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(ROHAN, MOREDAIN, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(ROHAN, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GONDOR, MORDOR, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GONDOR, RHUN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GONDOR, NEAR_HARAD, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(GONDOR, MOREDAIN, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(GONDOR, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(MORDOR, DORWINION, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(MORDOR, TAUREDAIN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(DORWINION, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(NEAR_HARAD, TAUREDAIN, LOTRFactionRelations.Relation.ENEMY);
        LOTRFactionRelations.setDefaultRelations(MOREDAIN, TAUREDAIN, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        LOTRFactionRelations.setDefaultRelations(TAUREDAIN, HALF_TROLL, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        for(LOTRFaction f : LOTRFaction.values()) {
            if(!f.allowPlayer || f == UTUMNO) continue;
            LOTRFactionRelations.setDefaultRelations(f, UTUMNO, LOTRFactionRelations.Relation.MORTAL_ENEMY);
        }
        LOTRFaction.HOBBIT.approvesWarCrimes = false;
        LOTRFaction.HOBBIT.isolationist = true;
        HOBBIT.addControlZone(new LOTRControlZone(LOTRWaypoint.BYWATER, 40));
        HOBBIT.addControlZone(new LOTRControlZone(LOTRWaypoint.BUCKLEBURY, 15));
        HOBBIT.addControlZone(new LOTRControlZone(LOTRWaypoint.HAYSEND, 10));
        HOBBIT.addControlZone(new LOTRControlZone(LOTRWaypoint.MICHEL_DELVING, 35));
        HOBBIT.addControlZone(new LOTRControlZone(LOTRWaypoint.GREENHOLM, 10));
        HOBBIT.addControlZone(new LOTRControlZone(LOTRWaypoint.LONGBOTTOM, 30));
        HOBBIT.addControlZone(new LOTRControlZone(LOTRWaypoint.BREE, 15));
        LOTRFaction.RANGER_NORTH.approvesWarCrimes = false;
        RANGER_NORTH.addControlZone(new LOTRControlZone(LOTRWaypoint.BYWATER, 110));
        RANGER_NORTH.addControlZone(new LOTRControlZone(LOTRWaypoint.SARN_FORD, 60));
        RANGER_NORTH.addControlZone(new LOTRControlZone(LOTRWaypoint.LAST_BRIDGE, 110));
        RANGER_NORTH.addControlZone(new LOTRControlZone(LOTRWaypoint.BREE, 100));
        RANGER_NORTH.addControlZone(new LOTRControlZone(LOTRWaypoint.ANNUMINAS, 50));
        RANGER_NORTH.addControlZone(new LOTRControlZone(LOTRWaypoint.FORNOST, 50));
        RANGER_NORTH.addControlZone(new LOTRControlZone(LOTRWaypoint.MOUNT_GRAM, 100));
        RANGER_NORTH.addControlZone(new LOTRControlZone(LOTRWaypoint.CARN_DUM, 60));
        RANGER_NORTH.addControlZone(new LOTRControlZone(LOTRWaypoint.GREENWAY_CROSSROADS, 60));
        RANGER_NORTH.addControlZone(new LOTRControlZone(LOTRWaypoint.THARBAD, 50));
        LOTRFaction.BLUE_MOUNTAINS.approvesWarCrimes = false;
        BLUE_MOUNTAINS.addControlZone(new LOTRControlZone(LOTRWaypoint.BELEGOST, 40));
        BLUE_MOUNTAINS.addControlZone(new LOTRControlZone(LOTRWaypoint.NOGROD, 40));
        BLUE_MOUNTAINS.addControlZone(new LOTRControlZone(LOTRWaypoint.THORIN_HALLS, 50));
        BLUE_MOUNTAINS.addControlZone(new LOTRControlZone(695, 820, 80));
        LOTRFaction.HIGH_ELF.approvesWarCrimes = false;
        HIGH_ELF.addControlZone(new LOTRControlZone(LOTRWaypoint.MITHLOND_SOUTH, 60));
        HIGH_ELF.addControlZone(new LOTRControlZone(LOTRWaypoint.FORLOND, 80));
        HIGH_ELF.addControlZone(new LOTRControlZone(LOTRWaypoint.HARLOND, 80));
        HIGH_ELF.addControlZone(new LOTRControlZone(LOTRWaypoint.FORD_BRUINEN, 50));
        LOTRFaction.GUNDABAD.approvesWarCrimes = true;
        GUNDABAD.addControlZone(new LOTRControlZone(LOTRWaypoint.MOUNT_GUNDABAD, 200));
        GUNDABAD.addControlZone(new LOTRControlZone(LOTRWaypoint.MOUNT_GRAM, 200));
        GUNDABAD.addControlZone(new LOTRControlZone(LOTRWaypoint.GOBLIN_TOWN, 150));
        GUNDABAD.addControlZone(new LOTRControlZone(LOTRWaypoint.MOUNT_CARADHRAS, 100));
        LOTRFaction.ANGMAR.approvesWarCrimes = true;
        ANGMAR.addControlZone(new LOTRControlZone(LOTRWaypoint.CARN_DUM, 75));
        ANGMAR.addControlZone(new LOTRControlZone(LOTRWaypoint.MOUNT_GRAM, 125));
        ANGMAR.addControlZone(new LOTRControlZone(LOTRWaypoint.THE_TROLLSHAWS, 50));
        LOTRFaction.WOOD_ELF.approvesWarCrimes = false;
        WOOD_ELF.addControlZone(new LOTRControlZone(LOTRWaypoint.ENCHANTED_RIVER, 75));
        WOOD_ELF.addControlZone(new LOTRControlZone(LOTRWaypoint.FOREST_GATE, 20));
        WOOD_ELF.addControlZone(new LOTRControlZone(LOTRWaypoint.DOL_GULDUR, 30));
        LOTRFaction.DOL_GULDUR.approvesWarCrimes = true;
        DOL_GULDUR.addControlZone(new LOTRControlZone(LOTRWaypoint.DOL_GULDUR, 125));
        DOL_GULDUR.addControlZone(new LOTRControlZone(LOTRWaypoint.ENCHANTED_RIVER, 75));
        LOTRFaction.DALE.approvesWarCrimes = false;
        DALE.addControlZone(new LOTRControlZone(LOTRWaypoint.DALE_CROSSROADS, 175));
        LOTRFaction.DWARF.approvesWarCrimes = false;
        DWARF.addControlZone(new LOTRControlZone(LOTRWaypoint.EREBOR, 75));
        DWARF.addControlZone(new LOTRControlZone(LOTRWaypoint.WEST_PEAK, 100));
        DWARF.addControlZone(new LOTRControlZone(LOTRWaypoint.EAST_PEAK, 75));
        DWARF.addControlZone(new LOTRControlZone(LOTRWaypoint.REDWATER_FORD, 75));
        DWARF.addControlZone(new LOTRControlZone(LOTRWaypoint.MOUNT_CARADHRAS, 100));
        DWARF.addControlZone(new LOTRControlZone(LOTRWaypoint.MOUNT_GUNDABAD, 100));
        DWARF.addControlZone(new LOTRControlZone(LOTRWaypoint.DAINS_HALLS, 50));
        LOTRFaction.GALADHRIM.approvesWarCrimes = false;
        GALADHRIM.addControlZone(new LOTRControlZone(LOTRWaypoint.CARAS_GALADHON, 100));
        LOTRFaction.DUNLAND.approvesWarCrimes = true;
        DUNLAND.addControlZone(new LOTRControlZone(LOTRWaypoint.SOUTH_DUNLAND, 125));
        LOTRFaction.URUK_HAI.approvesWarCrimes = true;
        URUK_HAI.addControlZone(new LOTRControlZone(LOTRWaypoint.ISENGARD, 100));
        URUK_HAI.addControlZone(new LOTRControlZone(LOTRWaypoint.EDORAS, 50));
        LOTRFaction.FANGORN.approvesWarCrimes = false;
        LOTRFaction.FANGORN.isolationist = true;
        FANGORN.addControlZone(new LOTRControlZone(1180, 1005, 70));
        LOTRFaction.ROHAN.approvesWarCrimes = false;
        ROHAN.addControlZone(new LOTRControlZone(LOTRWaypoint.ENTWADE, 150));
        ROHAN.addControlZone(new LOTRControlZone(LOTRWaypoint.ISENGARD, 100));
        LOTRFaction.GONDOR.approvesWarCrimes = false;
        GONDOR.addControlZone(new LOTRControlZone(LOTRWaypoint.MINAS_TIRITH, 200));
        GONDOR.addControlZone(new LOTRControlZone(LOTRWaypoint.EDHELLOND, 125));
        GONDOR.addControlZone(new LOTRControlZone(LOTRWaypoint.GREEN_HILLS, 100));
        GONDOR.addControlZone(new LOTRControlZone(LOTRWaypoint.CROSSINGS_OF_POROS, 150));
        GONDOR.addControlZone(new LOTRControlZone(LOTRWaypoint.CROSSINGS_OF_HARAD, 75));
        GONDOR.addControlZone(new LOTRControlZone(LOTRWaypoint.UMBAR_CITY, 150));
        LOTRFaction.MORDOR.approvesWarCrimes = true;
        MORDOR.addControlZone(new LOTRControlZone(LOTRWaypoint.BARAD_DUR, 500));
        LOTRFaction.DORWINION.approvesWarCrimes = false;
        DORWINION.addControlZone(new LOTRControlZone(LOTRWaypoint.DORWINION_COURT, 175));
        DORWINION.addControlZone(new LOTRControlZone(LOTRWaypoint.DALE_PORT, 30));
        LOTRFaction.RHUN.approvesWarCrimes = false;
        RHUN.addControlZone(new LOTRControlZone(LOTRWaypoint.RHUN_CAPITAL, 175));
        RHUN.addControlZone(new LOTRControlZone(LOTRWaypoint.MINAS_TIRITH, 100));
        RHUN.addControlZone(new LOTRControlZone(LOTRWaypoint.DALE_CITY, 50));
        LOTRFaction.NEAR_HARAD.approvesWarCrimes = false;
        NEAR_HARAD.addControlZone(new LOTRControlZone(LOTRWaypoint.UMBAR_CITY, 200));
        NEAR_HARAD.addControlZone(new LOTRControlZone(LOTRWaypoint.FERTILE_VALLEY, 150));
        NEAR_HARAD.addControlZone(new LOTRControlZone(LOTRWaypoint.HARNEN_SEA_TOWN, 60));
        NEAR_HARAD.addControlZone(new LOTRControlZone(LOTRWaypoint.HARNEN_RIVER_TOWN, 60));
        NEAR_HARAD.addControlZone(new LOTRControlZone(LOTRWaypoint.DESERT_TOWN, 50));
        NEAR_HARAD.addControlZone(new LOTRControlZone(LOTRWaypoint.SOUTH_DESERT_TOWN, 50));
        NEAR_HARAD.addControlZone(new LOTRControlZone(LOTRWaypoint.GULF_CITY, 150));
        NEAR_HARAD.addControlZone(new LOTRControlZone(LOTRWaypoint.CROSSINGS_OF_HARAD, 75));
        NEAR_HARAD.addControlZone(new LOTRControlZone(LOTRWaypoint.CROSSINGS_OF_POROS, 50));
        NEAR_HARAD.addControlZone(new LOTRControlZone(LOTRWaypoint.MINAS_TIRITH, 50));
        NEAR_HARAD.addControlZone(new LOTRControlZone(1210, 1340, 75));
        NEAR_HARAD.addControlZone(new LOTRControlZone(LOTRWaypoint.PELARGIR, 75));
        NEAR_HARAD.addControlZone(new LOTRControlZone(LOTRWaypoint.LINHIR, 75));
        LOTRFaction.MOREDAIN.approvesWarCrimes = true;
        MOREDAIN.addControlZone(new LOTRControlZone(LOTRWaypoint.GREAT_PLAINS_SOUTH, 350));
        MOREDAIN.addControlZone(new LOTRControlZone(LOTRWaypoint.GREAT_PLAINS_WEST, 170));
        MOREDAIN.addControlZone(new LOTRControlZone(LOTRWaypoint.GREAT_PLAINS_EAST, 200));
        MOREDAIN.addControlZone(new LOTRControlZone(LOTRWaypoint.GREAT_PLAINS_NORTH, 75));
        LOTRFaction.TAUREDAIN.approvesWarCrimes = true;
        TAUREDAIN.addControlZone(new LOTRControlZone(LOTRWaypoint.JUNGLE_CITY_CAPITAL, 400));
        TAUREDAIN.addControlZone(new LOTRControlZone(LOTRWaypoint.OLD_JUNGLE_RUIN, 75));
        LOTRFaction.HALF_TROLL.approvesWarCrimes = true;
        HALF_TROLL.addControlZone(new LOTRControlZone(LOTRWaypoint.TROLL_ISLAND, 100));
        HALF_TROLL.addControlZone(new LOTRControlZone(LOTRWaypoint.BLOOD_RIVER, 200));
        HALF_TROLL.addControlZone(new LOTRControlZone(LOTRWaypoint.SHADOW_POINT, 100));
        HALF_TROLL.addControlZone(new LOTRControlZone(LOTRWaypoint.CROSSINGS_OF_POROS, 40));
        HALF_TROLL.addControlZone(new LOTRControlZone(LOTRWaypoint.HARADUIN_BRIDGE, 100));
        LOTRFaction.UTUMNO.approvesWarCrimes = true;
        HOBBIT.setAchieveCategory(LOTRAchievement.Category.SHIRE);
        HOBBIT.addRank(10.0f, "guest").makeAchievement().makeTitle();
        HOBBIT.addRank(100.0f, "friend").makeAchievement().makeTitle().setPledgeRank();
        HOBBIT.addRank(250.0f, "hayward").makeAchievement().makeTitle();
        HOBBIT.addRank(500.0f, "bounder").makeAchievement().makeTitle();
        HOBBIT.addRank(1000.0f, "shirriff").makeAchievement().makeTitle();
        HOBBIT.addRank(2000.0f, "chief").makeAchievement().makeTitle();
        HOBBIT.addRank(3000.0f, "thain").makeAchievement().makeTitle();
        RANGER_NORTH.setAchieveCategory(LOTRAchievement.Category.ERIADOR);
        RANGER_NORTH.addRank(10.0f, "friend").makeAchievement().makeTitle();
        RANGER_NORTH.addRank(50.0f, "warden").makeAchievement().makeTitle();
        RANGER_NORTH.addRank(100.0f, "ranger").makeAchievement().makeTitle().setPledgeRank();
        RANGER_NORTH.addRank(200.0f, "ohtar").makeAchievement().makeTitle();
        RANGER_NORTH.addRank(500.0f, "roquen").makeAchievement().makeTitle();
        RANGER_NORTH.addRank(1000.0f, "champion").makeAchievement().makeTitle();
        RANGER_NORTH.addRank(2000.0f, "captain").makeAchievement().makeTitle();
        BLUE_MOUNTAINS.setAchieveCategory(LOTRAchievement.Category.BLUE_MOUNTAINS);
        BLUE_MOUNTAINS.addRank(10.0f, "guest").makeAchievement().makeTitle();
        BLUE_MOUNTAINS.addRank(50.0f, "friend").makeAchievement().makeTitle();
        BLUE_MOUNTAINS.addRank(100.0f, "warden").makeAchievement().makeTitle().setPledgeRank();
        BLUE_MOUNTAINS.addRank(200.0f, "axebearer").makeAchievement().makeTitle();
        BLUE_MOUNTAINS.addRank(500.0f, "champion").makeAchievement().makeTitle();
        BLUE_MOUNTAINS.addRank(1000.0f, "captain").makeAchievement().makeTitle();
        BLUE_MOUNTAINS.addRank(1500.0f, "noble").makeAchievement().makeTitle();
        BLUE_MOUNTAINS.addRank(3000.0f, "lord", true).makeAchievement().makeTitle();
        HIGH_ELF.setAchieveCategory(LOTRAchievement.Category.LINDON);
        HIGH_ELF.addRank(10.0f, "guest").makeAchievement().makeTitle();
        HIGH_ELF.addRank(50.0f, "friend").makeAchievement().makeTitle();
        HIGH_ELF.addRank(100.0f, "warrior").makeAchievement().makeTitle().setPledgeRank();
        HIGH_ELF.addRank(200.0f, "herald").makeAchievement().makeTitle();
        HIGH_ELF.addRank(500.0f, "captain").makeAchievement().makeTitle();
        HIGH_ELF.addRank(1000.0f, "noble").makeAchievement().makeTitle();
        HIGH_ELF.addRank(2000.0f, "commander").makeAchievement().makeTitle();
        HIGH_ELF.addRank(3000.0f, "lord", true).makeAchievement().makeTitle();
        GUNDABAD.setAchieveCategory(LOTRAchievement.Category.ERIADOR);
        GUNDABAD.addRank(10.0f, "thrall").makeAchievement().makeTitle();
        GUNDABAD.addRank(50.0f, "snaga").makeAchievement().makeTitle();
        GUNDABAD.addRank(100.0f, "raider").makeAchievement().makeTitle().setPledgeRank();
        GUNDABAD.addRank(200.0f, "ravager").makeAchievement().makeTitle();
        GUNDABAD.addRank(500.0f, "scourge").makeAchievement().makeTitle();
        GUNDABAD.addRank(1000.0f, "warlord").makeAchievement().makeTitle();
        GUNDABAD.addRank(2000.0f, "chieftain").makeAchievement().makeTitle();
        ANGMAR.setAchieveCategory(LOTRAchievement.Category.ANGMAR);
        ANGMAR.addRank(10.0f, "thrall").makeAchievement().makeTitle();
        ANGMAR.addRank(50.0f, "servant").makeAchievement().makeTitle();
        ANGMAR.addRank(100.0f, "kinsman").makeAchievement().makeTitle().setPledgeRank();
        ANGMAR.addRank(200.0f, "warrior").makeAchievement().makeTitle();
        ANGMAR.addRank(500.0f, "champion").makeAchievement().makeTitle();
        ANGMAR.addRank(1000.0f, "warlord").makeAchievement().makeTitle();
        ANGMAR.addRank(2000.0f, "chieftain").makeAchievement().makeTitle();
        WOOD_ELF.setAchieveCategory(LOTRAchievement.Category.MIRKWOOD);
        WOOD_ELF.addRank(50.0f, "guest").makeAchievement().makeTitle();
        WOOD_ELF.addRank(100.0f, "friend").makeAchievement().makeTitle().setPledgeRank();
        WOOD_ELF.addRank(200.0f, "guard").makeAchievement().makeTitle();
        WOOD_ELF.addRank(500.0f, "herald").makeAchievement().makeTitle();
        WOOD_ELF.addRank(1000.0f, "captain").makeAchievement().makeTitle();
        WOOD_ELF.addRank(2000.0f, "noble").makeAchievement().makeTitle();
        WOOD_ELF.addRank(3000.0f, "lord", true).makeAchievement().makeTitle();
        DOL_GULDUR.setAchieveCategory(LOTRAchievement.Category.MIRKWOOD);
        DOL_GULDUR.addRank(10.0f, "thrall").makeAchievement().makeTitle();
        DOL_GULDUR.addRank(50.0f, "servant").makeAchievement().makeTitle();
        DOL_GULDUR.addRank(100.0f, "brigand").makeAchievement().makeTitle().setPledgeRank();
        DOL_GULDUR.addRank(200.0f, "torchbearer").makeAchievement().makeTitle();
        DOL_GULDUR.addRank(500.0f, "despoiler").makeAchievement().makeTitle();
        DOL_GULDUR.addRank(1000.0f, "captain").makeAchievement().makeTitle();
        DOL_GULDUR.addRank(2000.0f, "lieutenant").makeAchievement().makeTitle();
        DALE.setAchieveCategory(LOTRAchievement.Category.DALE);
        DALE.addRank(10.0f, "guest").makeAchievement().makeTitle();
        DALE.addRank(50.0f, "friend").makeAchievement().makeTitle();
        DALE.addRank(100.0f, "soldier").makeAchievement().makeTitle().setPledgeRank();
        DALE.addRank(200.0f, "herald").makeAchievement().makeTitle();
        DALE.addRank(500.0f, "captain").makeAchievement().makeTitle();
        DALE.addRank(1000.0f, "marshal").makeAchievement().makeTitle();
        DALE.addRank(2000.0f, "lord", true).makeAchievement().makeTitle();
        DWARF.setAchieveCategory(LOTRAchievement.Category.IRON_HILLS);
        DWARF.addRank(10.0f, "guest").makeAchievement().makeTitle();
        DWARF.addRank(50.0f, "friend").makeAchievement().makeTitle();
        DWARF.addRank(100.0f, "oathfriend").makeAchievement().makeTitle().setPledgeRank();
        DWARF.addRank(200.0f, "axebearer").makeAchievement().makeTitle();
        DWARF.addRank(500.0f, "champion").makeAchievement().makeTitle();
        DWARF.addRank(1000.0f, "commander").makeAchievement().makeTitle();
        DWARF.addRank(1500.0f, "lord", true).makeAchievement().makeTitle();
        DWARF.addRank(3000.0f, "uzbad", true).makeAchievement().makeTitle();
        GALADHRIM.setAchieveCategory(LOTRAchievement.Category.LOTHLORIEN);
        GALADHRIM.addRank(10.0f, "guest").makeAchievement().makeTitle();
        GALADHRIM.addRank(50.0f, "friend").makeAchievement().makeTitle();
        GALADHRIM.addRank(100.0f, "warden").makeAchievement().makeTitle().setPledgeRank();
        GALADHRIM.addRank(200.0f, "warrior").makeAchievement().makeTitle();
        GALADHRIM.addRank(500.0f, "herald").makeAchievement().makeTitle();
        GALADHRIM.addRank(1000.0f, "captain").makeAchievement().makeTitle();
        GALADHRIM.addRank(2000.0f, "noble").makeAchievement().makeTitle();
        GALADHRIM.addRank(3000.0f, "lord", true).makeAchievement().makeTitle();
        DUNLAND.setAchieveCategory(LOTRAchievement.Category.DUNLAND);
        DUNLAND.addRank(10.0f, "guest").makeAchievement().makeTitle();
        DUNLAND.addRank(50.0f, "kinsman").makeAchievement().makeTitle();
        DUNLAND.addRank(100.0f, "warrior").makeAchievement().makeTitle().setPledgeRank();
        DUNLAND.addRank(200.0f, "bearer").makeAchievement().makeTitle();
        DUNLAND.addRank(500.0f, "avenger").makeAchievement().makeTitle();
        DUNLAND.addRank(1000.0f, "warlord").makeAchievement().makeTitle();
        DUNLAND.addRank(2000.0f, "chieftain").makeAchievement().makeTitle();
        URUK_HAI.setAchieveCategory(LOTRAchievement.Category.ROHAN);
        URUK_HAI.addRank(10.0f, "thrall").makeAchievement().makeTitle();
        URUK_HAI.addRank(50.0f, "snaga").makeAchievement().makeTitle();
        URUK_HAI.addRank(100.0f, "soldier").makeAchievement().makeTitle().setPledgeRank();
        URUK_HAI.addRank(200.0f, "treefeller").makeAchievement().makeTitle();
        URUK_HAI.addRank(500.0f, "berserker").makeAchievement().makeTitle();
        URUK_HAI.addRank(1000.0f, "corporal").makeAchievement().makeTitle();
        URUK_HAI.addRank(1500.0f, "hand").makeAchievement().makeTitle();
        URUK_HAI.addRank(3000.0f, "captain").makeAchievement().makeTitle();
        FANGORN.setAchieveCategory(LOTRAchievement.Category.FANGORN);
        FANGORN.addRank(10.0f, "newcomer").makeAchievement().makeTitle();
        FANGORN.addRank(50.0f, "friend").makeAchievement().makeTitle();
        FANGORN.addRank(100.0f, "treeherd").makeAchievement().makeTitle().setPledgeRank();
        FANGORN.addRank(250.0f, "master").makeAchievement().makeTitle();
        FANGORN.addRank(500.0f, "elder").makeAchievement().makeTitle();
        ROHAN.setAchieveCategory(LOTRAchievement.Category.ROHAN);
        ROHAN.addRank(10.0f, "guest").makeAchievement().makeTitle();
        ROHAN.addRank(50.0f, "footman").makeAchievement().makeTitle();
        ROHAN.addRank(100.0f, "atarms").makeAchievement().makeTitle().setPledgeRank();
        ROHAN.addRank(250.0f, "rider").makeAchievement().makeTitle();
        ROHAN.addRank(500.0f, "esquire").makeAchievement().makeTitle();
        ROHAN.addRank(1000.0f, "captain").makeAchievement().makeTitle();
        ROHAN.addRank(2000.0f, "marshal").makeAchievement().makeTitle();
        GONDOR.setAchieveCategory(LOTRAchievement.Category.GONDOR);
        GONDOR.addRank(10.0f, "guest").makeAchievement().makeTitle();
        GONDOR.addRank(50.0f, "friend").makeAchievement().makeTitle();
        GONDOR.addRank(100.0f, "atarms").makeAchievement().makeTitle().setPledgeRank();
        GONDOR.addRank(200.0f, "soldier").makeAchievement().makeTitle();
        GONDOR.addRank(500.0f, "knight").makeAchievement().makeTitle();
        GONDOR.addRank(1000.0f, "champion").makeAchievement().makeTitle();
        GONDOR.addRank(1500.0f, "captain").makeAchievement().makeTitle();
        GONDOR.addRank(3000.0f, "lord", true).makeAchievement().makeTitle();
        MORDOR.setAchieveCategory(LOTRAchievement.Category.MORDOR);
        MORDOR.addRank(10.0f, "thrall").makeAchievement().makeTitle();
        MORDOR.addRank(50.0f, "snaga").makeAchievement().makeTitle();
        MORDOR.addRank(100.0f, "brigand").makeAchievement().makeTitle().setPledgeRank();
        MORDOR.addRank(200.0f, "slavedriver").makeAchievement().makeTitle();
        MORDOR.addRank(500.0f, "despoiler").makeAchievement().makeTitle();
        MORDOR.addRank(1000.0f, "captain").makeAchievement().makeTitle();
        MORDOR.addRank(1500.0f, "lieutenant").makeAchievement().makeTitle();
        MORDOR.addRank(3000.0f, "commander").makeAchievement().makeTitle();
        DORWINION.setAchieveCategory(LOTRAchievement.Category.DORWINION);
        DORWINION.addRank(10.0f, "guest").makeAchievement().makeTitle();
        DORWINION.addRank(50.0f, "vinehand").makeAchievement().makeTitle();
        DORWINION.addRank(100.0f, "merchant").makeAchievement().makeTitle().setPledgeRank();
        DORWINION.addRank(200.0f, "guard").makeAchievement().makeTitle();
        DORWINION.addRank(500.0f, "captain").makeAchievement().makeTitle();
        DORWINION.addRank(1000.0f, "master").makeAchievement().makeTitle();
        DORWINION.addRank(1500.0f, "chief").makeAchievement().makeTitle();
        DORWINION.addRank(3000.0f, "lord", true).makeAchievement().makeTitle();
        RHUN.setAchieveCategory(LOTRAchievement.Category.RHUN);
        RHUN.addRank(10.0f, "bondsman").makeAchievement().makeTitle();
        RHUN.addRank(50.0f, "levyman").makeAchievement().makeTitle();
        RHUN.addRank(100.0f, "clansman").makeAchievement().makeTitle().setPledgeRank();
        RHUN.addRank(200.0f, "warrior").makeAchievement().makeTitle();
        RHUN.addRank(500.0f, "champion").makeAchievement().makeTitle();
        RHUN.addRank(1000.0f, "golden").makeAchievement().makeTitle();
        RHUN.addRank(1500.0f, "warlord").makeAchievement().makeTitle();
        RHUN.addRank(3000.0f, "chieftain").makeAchievement().makeTitle();
        NEAR_HARAD.setAchieveCategory(LOTRAchievement.Category.NEAR_HARAD);
        NEAR_HARAD.addRank(10.0f, "guest").makeAchievement().makeTitle();
        NEAR_HARAD.addRank(50.0f, "friend").makeAchievement().makeTitle();
        NEAR_HARAD.addRank(100.0f, "kinsman").makeAchievement().makeTitle().setPledgeRank();
        NEAR_HARAD.addRank(200.0f, "warrior").makeAchievement().makeTitle();
        NEAR_HARAD.addRank(500.0f, "champion").makeAchievement().makeTitle();
        NEAR_HARAD.addRank(1000.0f, "serpentguard").makeAchievement().makeTitle();
        NEAR_HARAD.addRank(1500.0f, "warlord").makeAchievement().makeTitle();
        NEAR_HARAD.addRank(3000.0f, "prince", true).makeAchievement().makeTitle();
        MOREDAIN.setAchieveCategory(LOTRAchievement.Category.FAR_HARAD_SAVANNAH);
        MOREDAIN.addRank(10.0f, "guest").makeAchievement().makeTitle();
        MOREDAIN.addRank(50.0f, "friend").makeAchievement().makeTitle();
        MOREDAIN.addRank(100.0f, "kinsman").makeAchievement().makeTitle().setPledgeRank();
        MOREDAIN.addRank(250.0f, "hunter").makeAchievement().makeTitle();
        MOREDAIN.addRank(500.0f, "warrior").makeAchievement().makeTitle();
        MOREDAIN.addRank(1000.0f, "chief").makeAchievement().makeTitle();
        MOREDAIN.addRank(3000.0f, "greatchief").makeAchievement().makeTitle();
        TAUREDAIN.setAchieveCategory(LOTRAchievement.Category.FAR_HARAD_JUNGLE);
        TAUREDAIN.addRank(10.0f, "guest").makeAchievement().makeTitle();
        TAUREDAIN.addRank(50.0f, "friend").makeAchievement().makeTitle();
        TAUREDAIN.addRank(100.0f, "forestman").makeAchievement().makeTitle().setPledgeRank();
        TAUREDAIN.addRank(200.0f, "warrior").makeAchievement().makeTitle();
        TAUREDAIN.addRank(500.0f, "champion").makeAchievement().makeTitle();
        TAUREDAIN.addRank(1000.0f, "warlord").makeAchievement().makeTitle();
        TAUREDAIN.addRank(3000.0f, "splendour").makeAchievement().makeTitle();
        HALF_TROLL.setAchieveCategory(LOTRAchievement.Category.PERTOROGWAITH);
        HALF_TROLL.addRank(10.0f, "guest").makeAchievement().makeTitle();
        HALF_TROLL.addRank(50.0f, "scavenger").makeAchievement().makeTitle();
        HALF_TROLL.addRank(100.0f, "kin").makeAchievement().makeTitle().setPledgeRank();
        HALF_TROLL.addRank(200.0f, "warrior").makeAchievement().makeTitle();
        HALF_TROLL.addRank(500.0f, "raider").makeAchievement().makeTitle();
        HALF_TROLL.addRank(1000.0f, "warlord").makeAchievement().makeTitle();
        HALF_TROLL.addRank(2000.0f, "chieftain").makeAchievement().makeTitle();
    }

    public String codeName() {
        return this.name();
    }

    public String untranslatedFactionName() {
        return "lotr.faction." + this.codeName() + ".name";
    }

    public String factionName() {
        if(LOTRMod.isAprilFools()) {
            String[] names = new String[] {"Britain Stronger in Europe", "Vote Leave"};
            int i = this.ordinal();
            i = (int) (i + (i ^ 0xF385L) + 28703L * (i * i ^ 0x30C087L));
            factionRand.setSeed(i);
            List<String> list = Arrays.asList(names);
            Collections.shuffle(list, factionRand);
            return list.get(0);
        }
        return StatCollector.translateToLocal(this.untranslatedFactionName());
    }

    public String factionEntityName() {
        return StatCollector.translateToLocal("lotr.faction." + this.codeName() + ".entity");
    }

    public String factionSubtitle() {
        return StatCollector.translateToLocal("lotr.faction." + this.codeName() + ".subtitle");
    }

    public LOTRFactionRank getRank(EntityPlayer entityplayer) {
        return this.getRank(LOTRLevelData.getData(entityplayer));
    }

    public LOTRFactionRank getRank(LOTRPlayerData pd) {
        float alignment = pd.getAlignment(this);
        return this.getRank(alignment);
    }

    public LOTRFactionRank getRank(float alignment) {
        for(LOTRFactionRank rank : this.ranksSortedDescending) {
            if(rank.isDummyRank() || !(alignment >= rank.alignment)) continue;
            return rank;
        }
        if(alignment >= 0.0f) {
            return LOTRFactionRank.RANK_NEUTRAL;
        }
        return LOTRFactionRank.RANK_ENEMY;
    }

    public LOTRFactionRank getRankAbove(LOTRFactionRank curRank) {
        return this.getRankNAbove(curRank, 1);
    }

    public LOTRFactionRank getRankBelow(LOTRFactionRank curRank) {
        return this.getRankNAbove(curRank, -1);
    }

    public LOTRFactionRank getRankNAbove(LOTRFactionRank curRank, int n) {
        if(this.ranksSortedDescending.isEmpty() || curRank == null) {
            return LOTRFactionRank.RANK_NEUTRAL;
        }
        int index = -1;
        if(curRank.isDummyRank()) {
            index = this.ranksSortedDescending.size();
        }
        else if(this.ranksSortedDescending.contains(curRank)) {
            index = this.ranksSortedDescending.indexOf(curRank);
        }
        if(index >= 0) {
            if((index -= n) < 0) {
                return this.ranksSortedDescending.get(0);
            }
            if(index > this.ranksSortedDescending.size() - 1) {
                return LOTRFactionRank.RANK_NEUTRAL;
            }
            return this.ranksSortedDescending.get(index);
        }
        return LOTRFactionRank.RANK_NEUTRAL;
    }

    public LOTRFactionRank getFirstRank() {
        if(this.ranksSortedDescending.isEmpty()) {
            return LOTRFactionRank.RANK_NEUTRAL;
        }
        return this.ranksSortedDescending.get(this.ranksSortedDescending.size() - 1);
    }

    public int getFactionColor() {
        return this.factionColor.getRGB();
    }

    public float[] getFactionColorComponents() {
        return this.factionColor.getColorComponents(null);
    }

    public boolean isPlayableAlignmentFaction() {
        return this.allowPlayer && !this.hasFixedAlignment;
    }

    public boolean isGoodRelation(LOTRFaction other) {
        LOTRFactionRelations.Relation rel = LOTRFactionRelations.getRelations(this, other);
        return rel == LOTRFactionRelations.Relation.ALLY || rel == LOTRFactionRelations.Relation.FRIEND;
    }

    public boolean isAlly(LOTRFaction other) {
        LOTRFactionRelations.Relation rel = LOTRFactionRelations.getRelations(this, other);
        return rel == LOTRFactionRelations.Relation.ALLY;
    }

    public boolean isNeutral(LOTRFaction other) {
        return LOTRFactionRelations.getRelations(this, other) == LOTRFactionRelations.Relation.NEUTRAL;
    }

    public boolean isBadRelation(LOTRFaction other) {
        LOTRFactionRelations.Relation rel = LOTRFactionRelations.getRelations(this, other);
        return rel == LOTRFactionRelations.Relation.ENEMY || rel == LOTRFactionRelations.Relation.MORTAL_ENEMY;
    }

    public boolean isMortalEnemy(LOTRFaction other) {
        LOTRFactionRelations.Relation rel = LOTRFactionRelations.getRelations(this, other);
        return rel == LOTRFactionRelations.Relation.MORTAL_ENEMY;
    }

    public List<LOTRFaction> getOthersOfRelation(LOTRFactionRelations.Relation rel) {
        ArrayList<LOTRFaction> list = new ArrayList<LOTRFaction>();
        for(LOTRFaction f : LOTRFaction.values()) {
            if(f == this || !f.isPlayableAlignmentFaction() || LOTRFactionRelations.getRelations(this, f) != rel) continue;
            list.add(f);
        }
        return list;
    }

    public List<LOTRFaction> getBonusesForKilling() {
        ArrayList<LOTRFaction> list = new ArrayList<LOTRFaction>();
        for(LOTRFaction f : LOTRFaction.values()) {
            if(f == this || !this.isBadRelation(f)) continue;
            list.add(f);
        }
        return list;
    }

    public List<LOTRFaction> getPenaltiesForKilling() {
        ArrayList<LOTRFaction> list = new ArrayList<LOTRFaction>();
        list.add(this);
        for(LOTRFaction f : LOTRFaction.values()) {
            if(f == this || !this.isGoodRelation(f)) continue;
            list.add(f);
        }
        return list;
    }

    public List<LOTRFaction> getConquestBoostRelations() {
        ArrayList<LOTRFaction> list = new ArrayList<LOTRFaction>();
        for(LOTRFaction f : LOTRFaction.values()) {
            if(f == this || !f.isPlayableAlignmentFaction() || LOTRFactionRelations.getRelations(this, f) != LOTRFactionRelations.Relation.ALLY) continue;
            list.add(f);
        }
        return list;
    }

    public static boolean controlZonesEnabled(World world) {
        return LOTRLevelData.enableAlignmentZones() && world.getWorldInfo().getTerrainType() != LOTRMod.worldTypeMiddleEarthClassic;
    }

    public boolean inControlZone(EntityPlayer entityplayer) {
        return this.inControlZone(entityplayer.worldObj, entityplayer.posX, entityplayer.boundingBox.minY, entityplayer.posZ);
    }

    public boolean inControlZone(World world, double d, double d1, double d2) {
        if(this.inDefinedControlZone(world, d, d1, d2)) {
            return true;
        }
        double nearbyRange = 24.0;
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(d, d1, d2, d, d1, d2).expand(nearbyRange, nearbyRange, nearbyRange);
        List nearbyNPCs = world.selectEntitiesWithinAABB(EntityLivingBase.class, aabb, new LOTRNPCSelectForInfluence(this));
        return !nearbyNPCs.isEmpty();
    }

    public boolean inDefinedControlZone(EntityPlayer entityplayer) {
        return this.inDefinedControlZone(entityplayer, 0);
    }

    public boolean inDefinedControlZone(EntityPlayer entityplayer, int extraMapRange) {
        return this.inDefinedControlZone(entityplayer.worldObj, entityplayer.posX, entityplayer.boundingBox.minY, entityplayer.posZ, extraMapRange);
    }

    public boolean inDefinedControlZone(World world, double d, double d1, double d2) {
        return this.inDefinedControlZone(world, d, d1, d2, 0);
    }

    public boolean inDefinedControlZone(World world, double d, double d1, double d2, int extraMapRange) {
        if(world.provider instanceof LOTRWorldProvider && ((LOTRWorldProvider) world.provider).getLOTRDimension() == this.factionDimension) {
            if(!LOTRFaction.controlZonesEnabled(world)) {
                return true;
            }
            for(LOTRControlZone zone : this.controlZones) {
                if(!zone.inZone(d, d1, d2, extraMapRange)) continue;
                return true;
            }
        }
        return false;
    }

    public int getControlZoneReducedRange() {
        return this.isolationist ? 0 : 50;
    }

    public float getControlZoneAlignmentMultiplier(EntityPlayer entityplayer) {
        if(this.inControlZone(entityplayer)) {
            return 1.0f;
        }
        int reducedRange = this.getControlZoneReducedRange();
        double dist = this.distanceToNearestControlZoneInRange(entityplayer.posX, entityplayer.boundingBox.minY, entityplayer.posZ, reducedRange);
        if(dist >= 0.0) {
            double mapDist = LOTRWaypoint.worldToMapR(dist);
            float frac = (float) mapDist / reducedRange;
            float mplier = 1.0f - frac;
            mplier = MathHelper.clamp_float(mplier, 0.0f, 1.0f);
            return mplier;
        }
        return 0.0f;
    }

    public double distanceToNearestControlZoneInRange(double d, double d1, double d2, int mapRange) {
        double closestDist = -1.0;
        int coordRange = LOTRWaypoint.mapToWorldR(mapRange);
        for(LOTRControlZone zone : this.controlZones) {
            double dx = d - zone.xCoord;
            double dz = d2 - zone.zCoord;
            double dSq = dx * dx + dz * dz;
            double dToEdge = Math.sqrt(dSq) - zone.radiusCoord;
            if(!(dToEdge <= coordRange) || !(closestDist < 0.0) && !(dToEdge < closestDist)) continue;
            closestDist = dToEdge;
        }
        return closestDist;
    }

    public int[] calculateFullControlZoneWorldBorders() {
        int xMin = 0;
        int xMax = 0;
        int zMin = 0;
        int zMax = 0;
        boolean first = true;
        for(LOTRControlZone zone : this.controlZones) {
            int cxMin = zone.xCoord - zone.radiusCoord;
            int cxMax = zone.xCoord + zone.radiusCoord;
            int czMin = zone.zCoord - zone.radiusCoord;
            int czMax = zone.zCoord + zone.radiusCoord;
            if(first) {
                xMin = cxMin;
                xMax = cxMax;
                zMin = czMin;
                zMax = czMax;
                first = false;
                continue;
            }
            xMin = Math.min(xMin, cxMin);
            xMax = Math.max(xMax, cxMax);
            zMin = Math.min(zMin, czMin);
            zMax = Math.max(zMax, czMax);
        }
        return new int[] {xMin, xMax, zMin, zMax};
    }

    public boolean sharesControlZoneWith(LOTRFaction other) {
        return this.sharesControlZoneWith(other, 0);
    }

    public boolean sharesControlZoneWith(LOTRFaction other, int extraMapRadius) {
        for(LOTRControlZone zone : this.controlZones) {
            for(LOTRControlZone otherZone : other.controlZones) {
                if(!zone.intersectsWith(otherZone, extraMapRadius)) continue;
                return true;
            }
        }
        return false;
    }

    public static LOTRFaction forName(String name) {
        for(LOTRFaction f : LOTRFaction.values()) {
            if(!f.codeName().equals(name)) continue;
            return f;
        }
        return null;
    }

    public static LOTRFaction forID(int ID) {
        for(LOTRFaction f : LOTRFaction.values()) {
            if(f.ordinal() != ID) continue;
            return f;
        }
        return null;
    }

    public static List<LOTRFaction> getPlayableAlignmentFactions() {
        ArrayList<LOTRFaction> factions = new ArrayList<LOTRFaction>();
        for(LOTRFaction f : LOTRFaction.values()) {
            if(!f.isPlayableAlignmentFaction()) continue;
            factions.add(f);
        }
        return factions;
    }

    public static List<String> getPlayableAlignmentFactionNames() {
        List<LOTRFaction> factions = LOTRFaction.getPlayableAlignmentFactions();
        ArrayList<String> names = new ArrayList<String>();
        for(LOTRFaction f : factions) {
            names.add(f.codeName());
        }
        return names;
    }

    public static List<LOTRFaction> getAllRegional(LOTRDimension.DimensionRegion region) {
        ArrayList<LOTRFaction> factions = new ArrayList<LOTRFaction>();
        for(LOTRFaction f : LOTRFaction.values()) {
            if(f.factionRegion != region) continue;
            factions.add(f);
        }
        return factions;
    }

    public static List<LOTRFaction> getAllRhun() {
        return LOTRFaction.getAllRegional(LOTRDimension.DimensionRegion.EAST);
    }

    public static List<LOTRFaction> getAllHarad() {
        return LOTRFaction.getAllRegional(LOTRDimension.DimensionRegion.SOUTH);
    }

    public static List<LOTRFaction> getAllOfType(FactionType... types) {
        return LOTRFaction.getAllOfType(false, types);
    }

    public static List<LOTRFaction> getAllOfType(boolean includeUnplayable, FactionType... types) {
        ArrayList<LOTRFaction> factions = new ArrayList<LOTRFaction>();
        for(LOTRFaction f : LOTRFaction.values()) {
            if(!includeUnplayable && (!f.allowPlayer || f.hasFixedAlignment)) continue;
            boolean match = false;
            for(FactionType t : types) {
                if(!f.isOfType(t)) continue;
                match = true;
                break;
            }
            if(!match) continue;
            factions.add(f);
        }
        return factions;
    }

    public boolean isOfType(FactionType type) {
        return this.factionTypes.contains(type);
    }

    static {
        factionRand = new Random();
    }

    public static enum FactionType {
        TYPE_FREE, TYPE_ELF, TYPE_MAN, TYPE_DWARF, TYPE_ORC, TYPE_TROLL, TYPE_TREE;

    }

}
