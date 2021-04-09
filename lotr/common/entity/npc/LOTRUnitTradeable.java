package lotr.common.entity.npc;

import lotr.common.world.spawning.LOTRInvasions;

public interface LOTRUnitTradeable extends LOTRHireableBase {
    public LOTRUnitTradeEntries getUnits();

    public LOTRInvasions getConquestHorn();

    public boolean shouldTraderRespawn();
}
