package lotr.common.block;

import java.util.ArrayList;
import java.util.Random;
import lotr.common.LOTRMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LOTRBlockLeaves6 extends LOTRBlockLeavesBase {
    public LOTRBlockLeaves6() {
        this.setLeafNames("mahogany", "willow", "cypress", "olive");
    }

    @Override
    public Item getItemDropped(int i, Random random, int j) {
        return Item.getItemFromBlock(LOTRMod.sapling6);
    }

    @Override
    protected void addSpecialLeafDrops(ArrayList drops, World world, int i, int j, int k, int meta, int fortune) {
        int fruitChance;
        if((meta & 3) == 3 && world.rand.nextInt(fruitChance = this.calcFortuneModifiedDropChance(10, fortune)) == 0) {
            drops.add(new ItemStack(LOTRMod.olive));
        }
    }
}
