package lotr.common.quest;

import java.util.*;
import lotr.common.LOTRPlayerData;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.item.LOTRItemMug;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;

public class LOTRMiniQuestCollect extends LOTRMiniQuest {
    public ItemStack collectItem;
    public int collectTarget;
    public int amountGiven;

    public LOTRMiniQuestCollect(LOTRPlayerData pd) {
        super(pd);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if(this.collectItem != null) {
            NBTTagCompound itemData = new NBTTagCompound();
            this.collectItem.writeToNBT(itemData);
            nbt.setTag("Item", itemData);
        }
        nbt.setInteger("Target", this.collectTarget);
        nbt.setInteger("Given", this.amountGiven);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if(nbt.hasKey("Item")) {
            NBTTagCompound itemData = nbt.getCompoundTag("Item");
            this.collectItem = ItemStack.loadItemStackFromNBT(itemData);
        }
        this.collectTarget = nbt.getInteger("Target");
        this.amountGiven = nbt.getInteger("Given");
    }

    @Override
    public boolean isValidQuest() {
        return super.isValidQuest() && this.collectItem != null && this.collectTarget > 0;
    }

    @Override
    public String getQuestObjective() {
        return StatCollector.translateToLocalFormatted("lotr.miniquest.collect", this.collectTarget, this.collectItem.getDisplayName());
    }

    @Override
    public String getObjectiveInSpeech() {
        return this.collectTarget + " " + this.collectItem.getDisplayName();
    }

    @Override
    public String getProgressedObjectiveInSpeech() {
        return this.collectTarget - this.amountGiven + " " + this.collectItem.getDisplayName();
    }

    @Override
    public String getQuestProgress() {
        return StatCollector.translateToLocalFormatted("lotr.miniquest.collect.progress", this.amountGiven, this.collectTarget);
    }

    @Override
    public String getQuestProgressShorthand() {
        return StatCollector.translateToLocalFormatted("lotr.miniquest.progressShort", this.amountGiven, this.collectTarget);
    }

    @Override
    public float getCompletionFactor() {
        return (float) this.amountGiven / (float) this.collectTarget;
    }

    @Override
    public ItemStack getQuestIcon() {
        return this.collectItem;
    }

    @Override
    public void onInteract(EntityPlayer entityplayer, LOTREntityNPC npc) {
        int prevAmountGiven = this.amountGiven;
        ArrayList<Integer> slotNumbers = new ArrayList<Integer>();
        slotNumbers.add(entityplayer.inventory.currentItem);
        for(int slot = 0; slot < entityplayer.inventory.mainInventory.length; ++slot) {
            if(slotNumbers.contains(slot)) continue;
            slotNumbers.add(slot);
        }
        Iterator slot = slotNumbers.iterator();
        while(slot.hasNext()) {
            int slot2 = (Integer) slot.next();
            ItemStack itemstack = entityplayer.inventory.mainInventory[slot2];
            if(this.isQuestItem(itemstack)) {
                int amountRemaining = this.collectTarget - this.amountGiven;
                if(itemstack.stackSize >= amountRemaining) {
                    itemstack.stackSize -= amountRemaining;
                    if(itemstack.stackSize <= 0) {
                        itemstack = null;
                    }
                    entityplayer.inventory.setInventorySlotContents(slot2, itemstack);
                    this.amountGiven += amountRemaining;
                }
                else {
                    this.amountGiven += itemstack.stackSize;
                    entityplayer.inventory.setInventorySlotContents(slot2, null);
                }
            }
            if(this.amountGiven < this.collectTarget) continue;
            this.complete(entityplayer, npc);
            break;
        }
        if(this.amountGiven > prevAmountGiven && !this.isCompleted()) {
            this.updateQuest();
        }
        if(!this.isCompleted()) {
            this.sendProgressSpeechbank(entityplayer, npc);
        }
    }

    protected boolean isQuestItem(ItemStack itemstack) {
        if(itemstack == null) {
            return false;
        }
        if(LOTRItemMug.isItemFullDrink(this.collectItem)) {
            ItemStack collectDrink = LOTRItemMug.getEquivalentDrink(this.collectItem);
            ItemStack offerDrink = LOTRItemMug.getEquivalentDrink(itemstack);
            return collectDrink.getItem() == offerDrink.getItem();
        }
        return itemstack.getItem() == this.collectItem.getItem() && (this.collectItem.getItemDamage() == 32767 || itemstack.getItemDamage() == this.collectItem.getItemDamage());
    }

    @Override
    public float getAlignmentBonus() {
        float f = this.collectTarget;
        return Math.max(f *= this.rewardFactor, 1.0f);
    }

    @Override
    public int getCoinBonus() {
        return Math.round(this.getAlignmentBonus() * 2.0f);
    }

    public static class QFCollect<Q extends LOTRMiniQuestCollect> extends LOTRMiniQuest.QuestFactoryBase<Q> {
        private ItemStack collectItem;
        private int minTarget;
        private int maxTarget;

        public QFCollect(String name) {
            super(name);
        }

        public QFCollect setCollectItem(ItemStack itemstack, int min, int max) {
            this.collectItem = itemstack;
            if(this.collectItem.isItemStackDamageable()) {
                this.collectItem.setItemDamage(32767);
            }
            this.minTarget = min;
            this.maxTarget = max;
            return this;
        }

        @Override
        public Class getQuestClass() {
            return LOTRMiniQuestCollect.class;
        }

        @Override
        public Q createQuest(LOTREntityNPC npc, Random rand) {
            LOTRMiniQuestCollect quest = super.createQuest(npc, rand);
            quest.collectItem = this.collectItem.copy();
            quest.collectTarget = MathHelper.getRandomIntegerInRange(rand, this.minTarget, this.maxTarget);
            return (Q) quest;
        }
    }

}
