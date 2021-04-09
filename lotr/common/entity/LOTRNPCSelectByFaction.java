package lotr.common.entity;

import lotr.common.LOTRMod;
import lotr.common.fac.LOTRFaction;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;

public class LOTRNPCSelectByFaction implements IEntitySelector {
    protected LOTRFaction faction;

    public LOTRNPCSelectByFaction(LOTRFaction f) {
        this.faction = f;
    }

    @Override
    public boolean isEntityApplicable(Entity entity) {
        return entity.isEntityAlive() && LOTRMod.getNPCFaction(entity) == this.faction;
    }
}
