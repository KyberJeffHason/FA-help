package lotr.common.entity.npc;

import lotr.common.item.LOTRItemMug;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

public class LOTRTradeEntry {
    private final ItemStack tradeItem;
    private int tradeCost;
    private int recentTradeValue;
    private static final int valueThreshold = 200;
    private static final int valueDecayTime = 60;
    private int lockedTicks;

    public LOTRTradeEntry(ItemStack itemstack, int cost) {
        this.tradeItem = itemstack;
        this.tradeCost = cost;
    }

    public ItemStack createTradeItem() {
        return this.tradeItem.copy();
    }

    public int getCost() {
        return this.tradeCost;
    }

    public void setCost(int cost) {
        this.tradeCost = cost;
    }

    public boolean isAvailable() {
        return this.recentTradeValue < 200 && this.lockedTicks <= 0;
    }

    public boolean updateAvailable(LOTREntityNPC entity) {
        boolean prevAvailable = this.isAvailable();
        if(entity.ticksExisted % 60 == 0 && this.recentTradeValue > 0) {
            --this.recentTradeValue;
        }
        if(this.lockedTicks > 0) {
            --this.lockedTicks;
        }
        return this.isAvailable() != prevAvailable;
    }

    public boolean matches(ItemStack itemstack) {
        ItemStack tradeCreated = this.createTradeItem();
        if(LOTRItemMug.isItemFullDrink(tradeCreated)) {
            ItemStack tradeDrink = LOTRItemMug.getEquivalentDrink(tradeCreated);
            ItemStack offerDrink = LOTRItemMug.getEquivalentDrink(itemstack);
            return tradeDrink.getItem() == offerDrink.getItem();
        }
        return OreDictionary.itemMatches(tradeCreated, itemstack, false);
    }

    public void doTransaction(int value) {
        this.recentTradeValue += value;
    }

    public void setLockedForTicks(int ticks) {
        this.lockedTicks = ticks;
    }

    public void writeToNBT(NBTTagCompound nbt) {
        this.tradeItem.writeToNBT(nbt);
        nbt.setInteger("Cost", this.tradeCost);
        nbt.setInteger("RecentTradeValue", this.recentTradeValue);
        nbt.setInteger("LockedTicks", this.lockedTicks);
    }

    public static LOTRTradeEntry readFromNBT(NBTTagCompound nbt) {
        ItemStack savedItem = ItemStack.loadItemStackFromNBT(nbt);
        if(savedItem != null) {
            int cost = nbt.getInteger("Cost");
            LOTRTradeEntry trade = new LOTRTradeEntry(savedItem, cost);
            if(nbt.hasKey("RecentTradeValue")) {
                trade.recentTradeValue = nbt.getInteger("RecentTradeValue");
            }
            trade.lockedTicks = nbt.getInteger("LockedTicks");
            return trade;
        }
        return null;
    }
}
