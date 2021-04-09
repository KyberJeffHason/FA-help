package lotr.client.fx;

import lotr.common.fac.LOTRAlignmentBonusMap;
import lotr.common.fac.LOTRFaction;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class LOTREntityAlignmentBonus extends Entity {
    public int particleAge;
    public String name;
    public LOTRFaction mainFaction;
    public float prevMainAlignment;
    public LOTRAlignmentBonusMap factionBonusMap;
    public boolean isKill;
    public float conquestBonus;

    public LOTREntityAlignmentBonus(World world, double d, double d1, double d2, String s, LOTRFaction f, float pre, LOTRAlignmentBonusMap fMap, boolean kill, float conqBonus) {
        super(world);
        this.setSize(0.5f, 0.5f);
        this.yOffset = this.height / 2.0f;
        this.setPosition(d, d1, d2);
        this.particleAge = 0;
        this.name = s;
        this.mainFaction = f;
        this.prevMainAlignment = pre;
        this.factionBonusMap = fMap;
        this.isKill = kill;
        this.conquestBonus = conqBonus;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        ++this.particleAge;
        if(this.particleAge >= 80) {
            this.setDead();
        }
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean isEntityInvulnerable() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }
}
