package lotr.common.entity;

import java.util.ArrayList;
import java.util.List;
import cpw.mods.fml.common.eventhandler.Event;
import lotr.common.*;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.fac.LOTRFaction;
import lotr.common.item.LOTRItemConquestHorn;
import lotr.common.world.spawning.LOTRInvasions;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

public class LOTREntityInvasionSpawner extends Entity {
    public float spawnerSpin;
    public float prevSpawnerSpin;
    private static double INVASION_RANGE = 40.0;
    private int mobsRemaining;
    private int timeSinceLastSpawn = 0;
    public boolean spawnsPersistent = true;
    private List<LOTRFaction> bonusFactions = new ArrayList<LOTRFaction>();

    public LOTREntityInvasionSpawner(World world) {
        super(world);
        this.setSize(1.5f, 1.5f);
        this.renderDistanceWeight = 4.0;
        this.spawnerSpin = this.rand.nextFloat() * 360.0f;
    }

    public ItemStack getInvasionItem() {
        return this.getInvasionType().getInvasionIcon();
    }

    @Override
    public void entityInit() {
        this.dataWatcher.addObject(20, (byte) 0);
    }

    public LOTRInvasions getInvasionType() {
        byte i = this.dataWatcher.getWatchableObjectByte(20);
        LOTRInvasions type = LOTRInvasions.forID(i);
        if(type != null) {
            return type;
        }
        return LOTRInvasions.HOBBIT;
    }

    public void setInvasionType(LOTRInvasions type) {
        this.dataWatcher.updateObject(20, (byte) type.ordinal());
    }

    public boolean canInvasionSpawnHere() {
        if(LOTRBannerProtection.isProtectedByBanner(this.worldObj, this, LOTRBannerProtection.forInvasionSpawner(this), false)) {
            return false;
        }
        if(LOTREntityNPCRespawner.isSpawnBlocked(this, this.getInvasionType().invasionFaction)) {
            return false;
        }
        return this.worldObj.checkNoEntityCollision(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty() && !this.worldObj.isAnyLiquid(this.boundingBox);
    }

    private void playHorn() {
        this.worldObj.playSoundAtEntity(this, "lotr:item.horn", 4.0f, 0.65f + this.rand.nextFloat() * 0.1f);
    }

    public void announceInvasionToEnemies() {
        this.playHorn();
        this.mobsRemaining = MathHelper.getRandomIntegerInRange(this.rand, 30, 70);
        double announceRange = INVASION_RANGE * 2.0;
        List<EntityPlayer> nearbyPlayers = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(announceRange, announceRange, announceRange));
        for(EntityPlayer entityplayer : nearbyPlayers) {
            if(!(LOTRLevelData.getData(entityplayer).getAlignment(this.getInvasionType().invasionFaction) < 0.0f)) continue;
            this.announceInvasionTo(entityplayer);
        }
    }

    public void announceInvasionTo(EntityPlayer entityplayer) {
        entityplayer.addChatMessage(new ChatComponentTranslation("chat.lotr.invasion.start", this.getInvasionType().invasionName()));
    }

    public void selectAppropriateBonusFactions() {
        if(LOTRFaction.controlZonesEnabled(this.worldObj)) {
            LOTRFaction invasionFaction = this.getInvasionType().invasionFaction;
            for(LOTRFaction faction : invasionFaction.getBonusesForKilling()) {
                if(faction.isolationist || !faction.inDefinedControlZone(this.worldObj, this.posX, this.posY, this.posZ, 50)) continue;
                this.bonusFactions.add(faction);
            }
            if(this.bonusFactions.isEmpty()) {
                int nearestRange = 150;
                LOTRFaction nearest = null;
                double nearestDist = Double.MAX_VALUE;
                for(LOTRFaction faction : invasionFaction.getBonusesForKilling()) {
                    double dist;
                    if(faction.isolationist || !((dist = faction.distanceToNearestControlZoneInRange(this.posX, this.posY, this.posZ, nearestRange)) >= 0.0) || nearest != null && !(dist < nearestDist)) continue;
                    nearest = faction;
                    nearestDist = dist;
                }
                if(nearest != null) {
                    this.bonusFactions.add(nearest);
                }
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setString("InvasionType", this.getInvasionType().codeName());
        nbt.setInteger("MobsRemaining", this.mobsRemaining);
        nbt.setInteger("TimeSinceSpawn", this.timeSinceLastSpawn);
        nbt.setBoolean("NPCPersistent", this.spawnsPersistent);
        if(!this.bonusFactions.isEmpty()) {
            NBTTagList bonusTags = new NBTTagList();
            for(LOTRFaction f : this.bonusFactions) {
                String fName = f.codeName();
                bonusTags.appendTag(new NBTTagString(fName));
            }
            nbt.setTag("BonusFactions", bonusTags);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        LOTRInvasions type = LOTRInvasions.forName(nbt.getString("InvasionType"));
        if(type == null && nbt.hasKey("Faction")) {
            String factionName = nbt.getString("Faction");
            type = LOTRInvasions.forName(factionName);
        }
        if(type == null || type.invasionMobs.isEmpty()) {
            this.setDead();
        }
        else {
            this.setInvasionType(type);
            this.mobsRemaining = nbt.getInteger("MobsRemaining");
            this.timeSinceLastSpawn = nbt.getInteger("TimeSinceSpawn");
            if(nbt.hasKey("NPCPersistent")) {
                this.spawnsPersistent = nbt.getBoolean("NPCPersistent");
            }
            if(nbt.hasKey("BonusFactions")) {
                NBTTagList bonusTags = nbt.getTagList("BonusFactions", 8);
                for(int i = 0; i < bonusTags.tagCount(); ++i) {
                    String fName = bonusTags.getStringTagAt(i);
                    LOTRFaction f = LOTRFaction.forName(fName);
                    if(f == null) continue;
                    this.bonusFactions.add(f);
                }
            }
        }
    }

    private void endInvasion() {
        this.worldObj.createExplosion(this, this.posX, this.posY + this.height / 2.0, this.posZ, 0.0f, false);
        this.setDead();
    }

    @Override
    public void onUpdate() {
        if(!this.worldObj.isRemote && this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL) {
            this.endInvasion();
            return;
        }
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevSpawnerSpin = this.spawnerSpin;
        this.spawnerSpin += 6.0f;
        this.prevSpawnerSpin = MathHelper.wrapAngleTo180_float(this.prevSpawnerSpin);
        this.spawnerSpin = MathHelper.wrapAngleTo180_float(this.spawnerSpin);
        this.motionX = 0.0;
        this.motionY = 0.0;
        this.motionZ = 0.0;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        if(!this.worldObj.isRemote && LOTRMod.canSpawnMobs(this.worldObj)) {
            LOTRInvasions invasionType = this.getInvasionType();
            EntityPlayer entityplayer = this.worldObj.getClosestPlayer(this.posX, this.posY, this.posZ, INVASION_RANGE);
            if(entityplayer != null) {
                List nearbyNPCs;
                ++this.timeSinceLastSpawn;
                if(this.timeSinceLastSpawn >= 2400) {
                    this.endInvasion();
                }
                else if(this.mobsRemaining > 0 && (nearbyNPCs = this.worldObj.selectEntitiesWithinAABB(LOTREntityNPC.class, this.boundingBox.expand(12.0, 12.0, 12.0), new LOTRNPCSelectByFaction(invasionType.invasionFaction))).size() < 16 && this.rand.nextInt(160) == 0) {
                    int mobSpawns = MathHelper.getRandomIntegerInRange(this.rand, 1, 6);
                    mobSpawns = Math.min(mobSpawns, this.mobsRemaining);
                    boolean spawnedAnyMobs = false;
                    block0: for(int l = 0; l < mobSpawns; ++l) {
                        LOTRInvasions.InvasionSpawnEntry entry = (LOTRInvasions.InvasionSpawnEntry) WeightedRandom.getRandomItem(this.rand, invasionType.invasionMobs);
                        Class entityClass = entry.getEntityClass();
                        String entityName = LOTREntities.getStringFromClass(entityClass);
                        LOTREntityNPC npc = (LOTREntityNPC) EntityList.createEntityByName(entityName, this.worldObj);
                        for(int attempts = 0; attempts < 40; ++attempts) {
                            int i = MathHelper.floor_double(this.posX) + MathHelper.getRandomIntegerInRange(this.rand, -6, 6);
                            int k = MathHelper.floor_double(this.posZ) + MathHelper.getRandomIntegerInRange(this.rand, -6, 6);
                            int j = MathHelper.floor_double(this.posY) + MathHelper.getRandomIntegerInRange(this.rand, -8, 4);
                            if(!this.worldObj.getBlock(i, j - 1, k).isSideSolid(this.worldObj, i, j - 1, k, ForgeDirection.UP)) continue;
                            npc.setLocationAndAngles(i + 0.5, j, k + 0.5, this.rand.nextFloat() * 360.0f, 0.0f);
                            npc.liftSpawnRestrictions = true;
                            Event.Result canSpawn = ForgeEventFactory.canEntitySpawn(npc, this.worldObj, (float) npc.posX, (float) npc.posY, (float) npc.posZ);
                            if(canSpawn != Event.Result.ALLOW && (canSpawn != Event.Result.DEFAULT || !npc.getCanSpawnHere())) continue;
                            npc.liftSpawnRestrictions = false;
                            npc.onSpawnWithEgg(null);
                            npc.isNPCPersistent = false;
                            if(this.spawnsPersistent) {
                                npc.isNPCPersistent = true;
                            }
                            npc.isInvasionSpawned = true;
                            npc.killBonusFactions.addAll(this.bonusFactions);
                            this.worldObj.spawnEntityInWorld(npc);
                            npc.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(INVASION_RANGE);
                            spawnedAnyMobs = true;
                            --this.mobsRemaining;
                            if(this.mobsRemaining <= 0) break block0;
                            continue block0;
                        }
                    }
                    if(spawnedAnyMobs) {
                        this.timeSinceLastSpawn = 0;
                        this.playHorn();
                    }
                }
            }
            if(this.mobsRemaining <= 0) {
                this.endInvasion();
            }
        }
        else {
            String particle = this.rand.nextBoolean() ? "smoke" : "flame";
            this.worldObj.spawnParticle(particle, this.posX + (this.rand.nextDouble() - 0.5) * this.width, this.posY + this.rand.nextDouble() * this.height, this.posZ + (this.rand.nextDouble() - 0.5) * this.width, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void applyEntityCollision(Entity entity) {
    }

    @Override
    public boolean hitByEntity(Entity entity) {
        if(entity instanceof EntityPlayer) {
            return this.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) entity), 0.0f);
        }
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damagesource, float f) {
        Entity entity = damagesource.getEntity();
        if(entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode) {
            if(!this.worldObj.isRemote) {
                this.endInvasion();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean interactFirst(EntityPlayer entityplayer) {
        if(!this.worldObj.isRemote && entityplayer.capabilities.isCreativeMode && !this.bonusFactions.isEmpty()) {
            ChatComponentText message = new ChatComponentText("");
            for(LOTRFaction f : this.bonusFactions) {
                if(!message.getSiblings().isEmpty()) {
                    message.appendSibling(new ChatComponentText(", "));
                }
                message.appendSibling(new ChatComponentTranslation(f.factionName(), new Object[0]));
            }
            entityplayer.addChatMessage(message);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getPickedResult(MovingObjectPosition target) {
        LOTRInvasions invasionType = this.getInvasionType();
        if(invasionType != null) {
            return LOTRItemConquestHorn.createHorn(invasionType);
        }
        return null;
    }
}
