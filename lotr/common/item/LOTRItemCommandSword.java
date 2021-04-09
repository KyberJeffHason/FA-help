package lotr.common.item;

import java.util.*;
import lotr.common.LOTRMod;
import lotr.common.LOTRSquadrons;
import lotr.common.entity.ai.LOTREntityAINearestAttackableTargetBasic;
import lotr.common.entity.npc.LOTREntityNPC;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class LOTRItemCommandSword extends LOTRItemSword implements LOTRSquadrons.SquadronItem {
    private static final float COMMAND_RANGE = 12.0f;
    private static final float TARGET_RANGE = 64.0f;
    private static final float TARGET_SPREAD = 6.0f;

    public LOTRItemCommandSword() {
        super(Item.ToolMaterial.IRON);
        this.setMaxDamage(0);
        this.lotrWeaponDamage = 1.0f;
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
        entityplayer.swingItem();
        if(!world.isRemote) {
            Entity entity = this.getEntityTarget(entityplayer);
            if(entity != null) {
                MovingObjectPosition entityHit = new MovingObjectPosition(entity, Vec3.createVectorHelper(entity.posX, entity.boundingBox.minY + entity.height / 2.0f, entity.posZ));
                this.command(entityplayer, world, itemstack, entityHit);
            }
            else {
                double range = 64.0;
                Vec3 eyePos = Vec3.createVectorHelper(entityplayer.posX, entityplayer.posY + entityplayer.getEyeHeight(), entityplayer.posZ);
                Vec3 look = entityplayer.getLookVec();
                Vec3 sight = eyePos.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
                MovingObjectPosition rayTrace = world.func_147447_a(eyePos, sight, false, false, true);
                if(rayTrace != null && rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    this.command(entityplayer, world, itemstack, rayTrace);
                }
                else {
                    this.command(entityplayer, world, itemstack, null);
                }
            }
        }
        return itemstack;
    }

    private Entity getEntityTarget(EntityPlayer entityplayer) {
        double range = 64.0;
        Vec3 eyePos = Vec3.createVectorHelper(entityplayer.posX, entityplayer.posY + entityplayer.getEyeHeight(), entityplayer.posZ);
        Vec3 look = entityplayer.getLookVec();
        Vec3 sight = eyePos.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
        float sightWidth = 1.0f;
        List list = entityplayer.worldObj.getEntitiesWithinAABBExcludingEntity(entityplayer, entityplayer.boundingBox.addCoord(look.xCoord * range, look.yCoord * range, look.zCoord * range).expand(sightWidth, sightWidth, sightWidth));
        Entity pointedEntity = null;
        double entityDist = range;
        for(Object element : list) {
            double d;
            Entity entity = (Entity) element;
            if(!(entity instanceof EntityLivingBase) || !entity.canBeCollidedWith()) continue;
            float width = 1.0f;
            AxisAlignedBB axisalignedbb = entity.boundingBox.expand(width, width, width);
            MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(eyePos, sight);
            if(axisalignedbb.isVecInside(eyePos)) {
                if(!(entityDist >= 0.0)) continue;
                pointedEntity = entity;
                entityDist = 0.0;
                continue;
            }
            if(movingobjectposition == null || !((d = eyePos.distanceTo(movingobjectposition.hitVec)) < entityDist) && entityDist != 0.0) continue;
            if(entity == entityplayer.ridingEntity && !entity.canRiderInteract()) {
                if(entityDist != 0.0) continue;
                pointedEntity = entity;
                continue;
            }
            pointedEntity = entity;
            entityDist = d;
        }
        if(pointedEntity != null) {
            return pointedEntity;
        }
        return null;
    }

    private void command(final EntityPlayer entityplayer, World world, ItemStack itemstack, MovingObjectPosition hitTarget) {
        entityplayer.setRevengeTarget(null);
        List spreadTargets = new ArrayList();
        if(hitTarget != null) {
            Vec3 vec = hitTarget.hitVec;
            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(vec.xCoord, vec.yCoord, vec.zCoord, vec.xCoord, vec.yCoord, vec.zCoord);
            aabb = aabb.expand(6.0, 6.0, 6.0);
            spreadTargets = world.selectEntitiesWithinAABB(EntityLivingBase.class, aabb, new IEntitySelector() {

                @Override
                public boolean isEntityApplicable(Entity entity) {
                    return entity.isEntityAlive() && LOTRMod.canPlayerAttackEntity(entityplayer, (EntityLivingBase) entity, false);
                }
            });
        }
        List nearbyHiredUnits = world.getEntitiesWithinAABB(LOTREntityNPC.class, entityplayer.boundingBox.expand(12.0, 12.0, 12.0));
        for(Object nearbyHiredUnit : nearbyHiredUnits) {
            LOTREntityNPC npc = (LOTREntityNPC) nearbyHiredUnit;
            if(!npc.hiredNPCInfo.isActive || npc.hiredNPCInfo.getHiringPlayer() != entityplayer || !npc.hiredNPCInfo.getObeyCommandSword() || !LOTRSquadrons.areSquadronsCompatible(npc, itemstack)) continue;
            ArrayList<EntityLivingBase> validTargets = new ArrayList<EntityLivingBase>();
            if(!spreadTargets.isEmpty()) {
                for(Object obj : spreadTargets) {
                    EntityLivingBase entity = (EntityLivingBase) obj;
                    if(!LOTRMod.canNPCAttackEntity(npc, entity, true)) continue;
                    validTargets.add(entity);
                }
            }
            if(!validTargets.isEmpty()) {
                LOTREntityAINearestAttackableTargetBasic.TargetSorter sorter = new LOTREntityAINearestAttackableTargetBasic.TargetSorter(npc);
                Collections.sort(validTargets, sorter);
                EntityLivingBase target = validTargets.get(0);
                npc.hiredNPCInfo.commandSwordAttack(target);
                npc.hiredNPCInfo.wasAttackCommanded = true;
                continue;
            }
            npc.hiredNPCInfo.commandSwordCancel();
        }
    }

}
