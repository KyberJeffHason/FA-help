package lotr.common.entity.ai;

import java.util.List;
import lotr.common.entity.npc.LOTREntityBandit;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

public class LOTREntityAIBanditFlee extends EntityAIBase {
    private LOTREntityBandit theBandit;
    private double speed;
    private double range;
    private EntityPlayer targetPlayer;

    public LOTREntityAIBanditFlee(LOTREntityBandit bandit, double d) {
        this.theBandit = bandit;
        this.speed = d;
        this.range = bandit.getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue();
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if(this.theBandit.getAttackTarget() != null) {
            return false;
        }
        if(this.theBandit.banditInventory.isEmpty()) {
            return false;
        }
        this.targetPlayer = this.findNearestPlayer();
        return this.targetPlayer != null;
    }

    private EntityPlayer findNearestPlayer() {
        List players = this.theBandit.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.theBandit.boundingBox.expand(this.range, this.range, this.range));
        double distance = this.range;
        EntityPlayer ret = null;
        for(Object player : players) {
            double d;
            EntityPlayer entityplayer = (EntityPlayer) player;
            if(entityplayer.capabilities.isCreativeMode || !((d = this.theBandit.getDistanceToEntity(entityplayer)) < distance)) continue;
            distance = d;
            ret = entityplayer;
        }
        return ret;
    }

    @Override
    public void updateTask() {
        if(this.theBandit.getNavigator().noPath()) {
            Vec3 away = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.theBandit, (int) this.range, 10, Vec3.createVectorHelper(this.targetPlayer.posX, this.targetPlayer.posY, this.targetPlayer.posZ));
            if(away != null) {
                this.theBandit.getNavigator().tryMoveToXYZ(away.xCoord, away.yCoord, away.zCoord, this.speed);
            }
            this.targetPlayer = this.findNearestPlayer();
        }
    }

    @Override
    public boolean continueExecuting() {
        if(this.targetPlayer == null || !this.targetPlayer.isEntityAlive() || this.targetPlayer.capabilities.isCreativeMode) {
            return false;
        }
        return this.theBandit.getAttackTarget() == null && this.theBandit.getDistanceSqToEntity(this.targetPlayer) < this.range * this.range;
    }

    @Override
    public void resetTask() {
        this.targetPlayer = null;
    }
}
