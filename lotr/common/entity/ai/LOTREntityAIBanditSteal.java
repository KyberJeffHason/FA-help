package lotr.common.entity.ai;

import java.util.*;
import lotr.common.LOTRLevelData;
import lotr.common.entity.npc.LOTREntityBandit;
import lotr.common.item.*;
import lotr.common.recipe.LOTRRecipes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.*;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;

public class LOTREntityAIBanditSteal extends EntityAIBase {
    private LOTREntityBandit theBandit;
    private EntityPlayer targetPlayer;
    private EntityPlayer prevTargetPlayer;
    private double speed;
    private int chaseTimer;
    private int rePathDelay;

    public LOTREntityAIBanditSteal(LOTREntityBandit bandit, double d) {
        this.theBandit = bandit;
        this.speed = d;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if(!this.theBandit.banditInventory.isEmpty()) {
            return false;
        }
        List players = this.theBandit.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.theBandit.boundingBox.expand(32.0, 32.0, 32.0));
        ArrayList<EntityPlayer> validTargets = new ArrayList<EntityPlayer>();
        for(Object player : players) {
            EntityPlayer entityplayer = (EntityPlayer) player;
            if(entityplayer.capabilities.isCreativeMode || !this.theBandit.canStealFromPlayerInv(entityplayer)) continue;
            validTargets.add(entityplayer);
        }
        if(validTargets.isEmpty()) {
            return false;
        }
        this.targetPlayer = validTargets.get(this.theBandit.getRNG().nextInt(validTargets.size()));
        if(this.targetPlayer != this.prevTargetPlayer) {
            this.theBandit.sendSpeechBank(this.targetPlayer, this.theBandit.getSpeechBank(this.targetPlayer));
        }
        return true;
    }

    @Override
    public void startExecuting() {
        this.chaseTimer = 600;
    }

    @Override
    public void updateTask() {
        --this.chaseTimer;
        this.theBandit.getLookHelper().setLookPositionWithEntity(this.targetPlayer, 30.0f, 30.0f);
        --this.rePathDelay;
        if(this.rePathDelay <= 0) {
            this.rePathDelay = 10;
            this.theBandit.getNavigator().tryMoveToEntityLiving(this.targetPlayer, this.speed);
        }
        if(this.theBandit.getDistanceSqToEntity(this.targetPlayer) <= 2.0) {
            this.chaseTimer = 0;
            this.steal();
        }
    }

    @Override
    public boolean continueExecuting() {
        if(this.targetPlayer == null || !this.targetPlayer.isEntityAlive() || this.targetPlayer.capabilities.isCreativeMode || !this.theBandit.canStealFromPlayerInv(this.targetPlayer)) {
            return false;
        }
        return this.chaseTimer > 0 && this.theBandit.getDistanceSqToEntity(this.targetPlayer) < 256.0;
    }

    @Override
    public void resetTask() {
        this.chaseTimer = 0;
        this.rePathDelay = 0;
        if(this.targetPlayer != null) {
            this.prevTargetPlayer = this.targetPlayer;
        }
        this.targetPlayer = null;
    }

    private void steal() {
        InventoryPlayer inv = this.targetPlayer.inventory;
        int thefts = MathHelper.getRandomIntegerInRange(this.theBandit.getRNG(), 1, LOTREntityBandit.MAX_THEFTS);
        boolean stolenSomething = false;
        for(int i = 0; i < thefts; ++i) {
            if(this.tryStealItem(inv, LOTRItemCoin.class)) {
                stolenSomething = true;
            }
            if(this.tryStealItem(inv, LOTRItemGem.class)) {
                stolenSomething = true;
                continue;
            }
            if(this.tryStealItem(inv, LOTRValuableItems.getToolMaterials())) {
                stolenSomething = true;
                continue;
            }
            if(this.tryStealItem(inv, LOTRItemRing.class)) {
                stolenSomething = true;
                continue;
            }
            if(this.tryStealItem(inv, ItemArmor.class)) {
                stolenSomething = true;
                continue;
            }
            if(this.tryStealItem(inv, ItemSword.class)) {
                stolenSomething = true;
                continue;
            }
            if(this.tryStealItem(inv, ItemTool.class)) {
                stolenSomething = true;
                continue;
            }
            if(this.tryStealItem(inv, LOTRItemPouch.class)) {
                stolenSomething = true;
                continue;
            }
            if(!this.tryStealItem(inv)) continue;
            stolenSomething = true;
        }
        if(stolenSomething) {
            this.targetPlayer.addChatMessage(new ChatComponentTranslation("chat.lotr.banditSteal", new Object[0]));
            this.theBandit.playSound("mob.horse.leather", 0.5f, 1.0f);
            LOTRLevelData.getData(this.targetPlayer).cancelFastTravel();
        }
    }

    private boolean tryStealItem(InventoryPlayer inv, final Item item) {
        return this.tryStealItem_do(inv, new BanditItemFilter() {

            @Override
            public boolean isApplicable(ItemStack itemstack) {
                return itemstack.getItem() == item;
            }
        });
    }

    private boolean tryStealItem(InventoryPlayer inv, final Class itemclass) {
        return this.tryStealItem_do(inv, new BanditItemFilter() {

            @Override
            public boolean isApplicable(ItemStack itemstack) {
                return itemclass.isAssignableFrom(itemstack.getItem().getClass());
            }
        });
    }

    private boolean tryStealItem(InventoryPlayer inv, final List<ItemStack> itemList) {
        return this.tryStealItem_do(inv, new BanditItemFilter() {

            @Override
            public boolean isApplicable(ItemStack itemstack) {
                for(ItemStack listItem : itemList) {
                    if(!LOTRRecipes.checkItemEquals(listItem, itemstack)) continue;
                    return true;
                }
                return false;
            }
        });
    }

    private boolean tryStealItem(InventoryPlayer inv) {
        return this.tryStealItem_do(inv, new BanditItemFilter() {

            @Override
            public boolean isApplicable(ItemStack itemstack) {
                return true;
            }
        });
    }

    private boolean tryStealItem_do(InventoryPlayer inv, BanditItemFilter filter) {
        Integer[] inventorySlots = new Integer[inv.mainInventory.length];
        for(int l = 0; l < inventorySlots.length; ++l) {
            inventorySlots[l] = l;
        }
        List<Integer> slotsAsList = Arrays.asList(inventorySlots);
        Collections.shuffle(slotsAsList);
        Integer[] arrinteger = inventorySlots = slotsAsList.toArray(inventorySlots);
        int n = arrinteger.length;
        for(int i = 0; i < n; ++i) {
            ItemStack itemstack;
            int slot = arrinteger[i];
            if(slot == inv.currentItem || (itemstack = inv.getStackInSlot(slot)) == null || !filter.isApplicable(itemstack) || !this.stealItem(inv, slot)) continue;
            return true;
        }
        return false;
    }

    private int getRandomTheftAmount(ItemStack itemstack) {
        return MathHelper.getRandomIntegerInRange(this.theBandit.getRNG(), 1, 8);
    }

    private boolean stealItem(InventoryPlayer inv, int slot) {
        ItemStack playerItem = inv.getStackInSlot(slot);
        int theft = this.getRandomTheftAmount(playerItem);
        if(theft > playerItem.stackSize) {
            theft = playerItem.stackSize;
        }
        int banditSlot = 0;
        while(this.theBandit.banditInventory.getStackInSlot(banditSlot) != null) {
            if(++banditSlot < this.theBandit.banditInventory.getSizeInventory()) continue;
            return false;
        }
        ItemStack stolenItem = playerItem.copy();
        stolenItem.stackSize = theft;
        this.theBandit.banditInventory.setInventorySlotContents(banditSlot, stolenItem);
        playerItem.stackSize -= theft;
        if(playerItem.stackSize <= 0) {
            inv.setInventorySlotContents(slot, null);
        }
        this.theBandit.isNPCPersistent = true;
        return true;
    }

    private abstract class BanditItemFilter {
        private BanditItemFilter() {
        }

        public abstract boolean isApplicable(ItemStack var1);
    }

}
