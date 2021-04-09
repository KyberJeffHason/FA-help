package lotr.common.item;

import java.util.ArrayList;
import java.util.List;
import lotr.common.LOTRBannerProtection;
import lotr.common.LOTRMod;
import lotr.common.enchant.LOTREnchantment;
import lotr.common.enchant.LOTREnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class LOTRItemBalrogWhip extends LOTRItemSword {
    public LOTRItemBalrogWhip() {
        super(LOTRMaterial.UTUMNO);
        this.lotrWeaponDamage = 7.0f;
        this.setMaxDamage(1000);
    }

    @Override
    public boolean hitEntity(ItemStack itemstack, EntityLivingBase hitEntity, EntityLivingBase user) {
        if(super.hitEntity(itemstack, hitEntity, user)) {
            this.checkIncompatibleModifiers(itemstack);
            if(!user.worldObj.isRemote && hitEntity.hurtTime == hitEntity.maxHurtTime) {
                this.launchWhip(user, hitEntity);
            }
            return true;
        }
        return false;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack itemstack) {
        return EnumAction.bow;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack itemstack) {
        return 20;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
        entityplayer.setItemInUse(itemstack, this.getMaxItemUseDuration(itemstack));
        return itemstack;
    }

    @Override
    public ItemStack onEaten(ItemStack itemstack, World world, EntityPlayer entityplayer) {
        this.checkIncompatibleModifiers(itemstack);
        entityplayer.swingItem();
        if(!world.isRemote) {
            this.launchWhip(entityplayer, null);
        }
        itemstack.damageItem(1, entityplayer);
        return itemstack;
    }

    private void launchWhip(EntityLivingBase user, EntityLivingBase hitEntity) {
        user.worldObj.playSoundAtEntity(user, "lotr:item.balrogWhip", 2.0f, 0.7f + itemRand.nextFloat() * 0.6f);
        double range = 16.0;
        Vec3 position = Vec3.createVectorHelper(user.posX, user.posY, user.posZ);
        Vec3 look = user.getLookVec();
        Vec3 sight = position.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
        float sightWidth = 1.0f;
        List list = user.worldObj.getEntitiesWithinAABBExcludingEntity(user, user.boundingBox.addCoord(look.xCoord * range, look.yCoord * range, look.zCoord * range).expand(sightWidth, sightWidth, sightWidth));
        ArrayList<EntityLivingBase> whipTargets = new ArrayList<EntityLivingBase>();
        for(Object element : list) {
            EntityLivingBase entity;
            Entity obj = (Entity) element;
            if(!(obj instanceof EntityLivingBase) || (entity = (EntityLivingBase) obj) == user.ridingEntity && !entity.canRiderInteract() || !entity.canBeCollidedWith()) continue;
            float width = 1.0f;
            AxisAlignedBB axisalignedbb = entity.boundingBox.expand(width, width, width);
            MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(position, sight);
            if(axisalignedbb.isVecInside(position)) {
                whipTargets.add(entity);
                continue;
            }
            if(movingobjectposition == null) continue;
            whipTargets.add(entity);
        }
        for(EntityLivingBase entity : whipTargets) {
            if(entity != hitEntity && !entity.attackEntityFrom(DamageSource.causeMobDamage(user), 1.0f)) continue;
            entity.setFire(5);
        }
        Vec3 eyeHeight = position.addVector(0.0, user.getEyeHeight(), 0.0);
        block2: for(int l = 4; l < (int) range; ++l) {
            double d = l / range;
            double dx = sight.xCoord - eyeHeight.xCoord;
            double dy = sight.yCoord - eyeHeight.yCoord;
            double dz = sight.zCoord - eyeHeight.zCoord;
            double x = eyeHeight.xCoord + dx * d;
            double y = eyeHeight.yCoord + dy * d;
            double z = eyeHeight.zCoord + dz * d;
            int i = MathHelper.floor_double(x);
            int j = MathHelper.floor_double(y);
            int k = MathHelper.floor_double(z);
            for(int j1 = j - 3; j1 <= j + 3; ++j1) {
                if(!World.doesBlockHaveSolidTopSurface(user.worldObj, i, j1 - 1, k) || !user.worldObj.getBlock(i, j1, k).isReplaceable(user.worldObj, i, j1, k)) continue;
                boolean protection = false;
                if(user instanceof EntityPlayer) {
                    protection = LOTRBannerProtection.isProtectedByBanner(user.worldObj, i, j1, k, LOTRBannerProtection.forPlayer((EntityPlayer) user), false);
                }
                else if(user instanceof EntityLiving) {
                    protection = LOTRBannerProtection.isProtectedByBanner(user.worldObj, i, j1, k, LOTRBannerProtection.forNPC((EntityLiving) user), false);
                }
                if(protection) continue;
                user.worldObj.setBlock(i, j1, k, Blocks.fire, 0, 3);
                continue block2;
            }
        }
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @Override
    public boolean getIsRepairable(ItemStack itemstack, ItemStack repairItem) {
        return repairItem.getItem() == LOTRMod.balrogFire;
    }

    private void checkIncompatibleModifiers(ItemStack itemstack) {
        for(LOTREnchantment ench : new LOTREnchantment[] {LOTREnchantment.fire, LOTREnchantment.chill}) {
            if(!LOTREnchantmentHelper.hasEnchant(itemstack, ench)) continue;
            LOTREnchantmentHelper.removeEnchant(itemstack, ench);
        }
    }
}
