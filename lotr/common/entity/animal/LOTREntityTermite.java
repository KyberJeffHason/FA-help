package lotr.common.entity.animal;

import lotr.common.LOTRMod;
import lotr.common.entity.LOTREntities;
import lotr.common.entity.ai.LOTREntityAIAttackOnCollide;
import lotr.common.entity.npc.LOTREntityNPC;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class LOTREntityTermite extends EntityMob {
    private int fuseTime;
    private static int maxFuseTime = 20;
    public static float explosionSize = 2.0f;

    public LOTREntityTermite(World world) {
        super(world);
        this.setSize(0.4f, 0.4f);
        this.renderDistanceWeight = 2.0;
        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new LOTREntityAIAttackOnCollide(this, 1.0, true));
        this.tasks.addTask(2, new EntityAIWander(this, 1.0));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, LOTREntityNPC.class, 0, true));
        this.experienceValue = 2;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3);
    }

    @Override
    public boolean isAIEnabled() {
        return true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(!this.worldObj.isRemote) {
            EntityLivingBase target = this.getAttackTarget();
            if(target == null) {
                --this.fuseTime;
            }
            else {
                float dist = this.getDistanceToEntity(target);
                if(dist < 3.0f) {
                    if(this.fuseTime == 0) {
                        this.worldObj.playSoundAtEntity(this, "creeper.primed", 1.0f, 0.5f);
                    }
                    ++this.fuseTime;
                    if(this.fuseTime >= 20) {
                        this.explode();
                    }
                }
                else {
                    --this.fuseTime;
                }
            }
            this.fuseTime = Math.min(Math.max(this.fuseTime, 0), maxFuseTime);
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        return true;
    }

    private void explode() {
        if(!this.worldObj.isRemote) {
            this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, explosionSize, LOTRMod.canGrief(this.worldObj));
            this.setDead();
        }
    }

    @Override
    protected String getLivingSound() {
        return "mob.silverfish.say";
    }

    @Override
    protected String getHurtSound() {
        return "mob.silverfish.hit";
    }

    @Override
    protected String getDeathSound() {
        return "mob.silverfish.kill";
    }

    @Override
    public void onDeath(DamageSource damagesource) {
        super.onDeath(damagesource);
        if(!this.worldObj.isRemote && damagesource.getEntity() instanceof EntityPlayer) {
            this.dropItem(LOTRMod.termite, 1);
            this.setDead();
        }
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    public ItemStack getPickedResult(MovingObjectPosition target) {
        return new ItemStack(LOTRMod.spawnEgg, 1, LOTREntities.getEntityID(this));
    }
}
