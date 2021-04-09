package lotr.common.entity.animal;

import lotr.common.entity.ai.LOTREntityAIAttackOnCollide;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class LOTREntityWildBoar extends LOTREntityHorse {
    public LOTREntityWildBoar(World world) {
        super(world);
        this.setSize(0.9f, 0.8f);
    }

    @Override
    protected boolean isMountHostile() {
        return true;
    }

    @Override
    protected EntityAIBase createMountAttackAI() {
        return new LOTREntityAIAttackOnCollide(this, 1.2, true);
    }

    @Override
    public int getHorseType() {
        return 0;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(3.0);
    }

    @Override
    protected void onLOTRHorseSpawn() {
        double maxHealth = this.getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue();
        maxHealth = Math.min(maxHealth, 25.0);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(maxHealth);
        double speed = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(speed *= 1.0);
    }

    @Override
    protected double clampChildHealth(double health) {
        return MathHelper.clamp_double(health, 10.0, 30.0);
    }

    @Override
    protected double clampChildJump(double jump) {
        return MathHelper.clamp_double(jump, 0.3, 1.0);
    }

    @Override
    protected double clampChildSpeed(double speed) {
        return MathHelper.clamp_double(speed, 0.08, 0.35);
    }

    @Override
    public boolean isBreedingItem(ItemStack itemstack) {
        return itemstack != null && itemstack.getItem() == Items.carrot;
    }

    @Override
    protected void dropFewItems(boolean flag, int i) {
        int meat = this.rand.nextInt(3) + 1 + this.rand.nextInt(1 + i);
        for(int l = 0; l < meat; ++l) {
            if(this.isBurning()) {
                this.dropItem(Items.cooked_porkchop, 1);
                continue;
            }
            this.dropItem(Items.porkchop, 1);
        }
    }

    @Override
    protected String getLivingSound() {
        return "mob.pig.say";
    }

    @Override
    protected String getHurtSound() {
        return "mob.pig.say";
    }

    @Override
    protected String getDeathSound() {
        return "mob.pig.death";
    }

    @Override
    protected String getAngrySoundName() {
        return "mob.pig.say";
    }

    @Override
    protected void func_145780_a(int i, int j, int k, Block block) {
        this.playSound("mob.pig.step", 0.15f, 1.0f);
    }
}
