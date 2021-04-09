package lotr.common.entity.npc;

import net.minecraft.entity.player.EntityPlayer;

public interface LOTRTravellingTrader extends LOTRTradeable {
    public void startTraderVisiting(EntityPlayer var1);

    public LOTREntityNPC createTravellingEscort();

    public String getDepartureSpeech();
}
