package lotr.common.entity.npc;

import lotr.common.fac.LOTRFaction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface LOTRTradeable {
    public String getNPCName();

    public LOTRFaction getFaction();

    public LOTRTradeEntries getBuyPool();

    public LOTRTradeEntries getSellPool();

    public boolean canTradeWith(EntityPlayer var1);

    public void onPlayerTrade(EntityPlayer var1, LOTRTradeEntries.TradeType var2, ItemStack var3);

    public boolean shouldTraderRespawn();

    public static interface Smith extends LOTRTradeable {
    }

    public static interface Bartender extends LOTRTradeable {
    }

}
