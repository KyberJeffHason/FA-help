package lotr.common.block;

import java.util.ArrayList;
import java.util.Random;
import lotr.common.LOTRMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LOTRBlockLeaves5 extends LOTRBlockLeavesBase {
    public LOTRBlockLeaves5() {
        this.setLeafNames("pine", "lemon", "orange", "lime");
    }

    @Override
    public Item getItemDropped(int i, Random random, int j) {
        return Item.getItemFromBlock(LOTRMod.sapling5);
    }

    @Override
    protected void addSpecialLeafDrops(ArrayList drops, World world, int i, int j, int k, int meta, int fortune) {
        int fruitChance;
        if((meta & 3) == 1 && world.rand.nextInt(fruitChance = this.calcFortuneModifiedDropChance(16, fortune)) == 0) {
            drops.add(new ItemStack(LOTRMod.lemon));
        }
        if((meta & 3) == 2 && world.rand.nextInt(fruitChance = this.calcFortuneModifiedDropChance(16, fortune)) == 0) {
            drops.add(new ItemStack(LOTRMod.orange));
        }
        if((meta & 3) == 3 && world.rand.nextInt(fruitChance = this.calcFortuneModifiedDropChance(16, fortune)) == 0) {
            drops.add(new ItemStack(LOTRMod.lime));
        }
    }
}
