package lotr.common.block;

import java.util.ArrayList;
import java.util.Random;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.LOTRMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LOTRBlockLeaves7 extends LOTRBlockLeavesBase {
    public LOTRBlockLeaves7() {
        this.setLeafNames("aspen", "greenOak", "lairelosse", "almond");
    }

    @Override
    public Item getItemDropped(int i, Random random, int j) {
        return Item.getItemFromBlock(LOTRMod.sapling7);
    }

    @Override
    protected void addSpecialLeafDrops(ArrayList drops, World world, int i, int j, int k, int meta, int fortune) {
        int fruitChance;
        if((meta & 3) == 3 && world.rand.nextInt(fruitChance = this.calcFortuneModifiedDropChance(12, fortune)) == 0) {
            drops.add(new ItemStack(LOTRMod.almond));
        }
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int i, int j, int k, Random random) {
        super.randomDisplayTick(world, i, j, k, random);
        String s = null;
        int metadata = world.getBlockMetadata(i, j, k) & 3;
        if(metadata == 1 && random.nextInt(150) == 0) {
            s = "leafGreen";
        }
        if(s != null) {
            double d = i + random.nextFloat();
            double d1 = j - 0.05;
            double d2 = k + random.nextFloat();
            double d3 = -0.1 + random.nextFloat() * 0.2f;
            double d4 = -0.03 - random.nextFloat() * 0.02f;
            double d5 = -0.1 + random.nextFloat() * 0.2f;
            LOTRMod.proxy.spawnParticle(s, d, d1, d2, d3, d4, d5);
        }
    }
}
