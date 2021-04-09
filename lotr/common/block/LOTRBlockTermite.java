package lotr.common.block;

import java.util.List;
import java.util.Random;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.LOTRCreativeTabs;
import lotr.common.entity.animal.LOTREntityTermite;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class LOTRBlockTermite extends Block {
    public LOTRBlockTermite() {
        super(Material.ground);
        this.setCreativeTab(LOTRCreativeTabs.tabBlock);
        this.setHardness(0.5f);
        this.setResistance(3.0f);
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, int i, int j, int k, int meta) {
        if(!world.isRemote && meta == 0 && world.rand.nextBoolean()) {
            int termites = 1 + world.rand.nextInt(3);
            for(int l = 0; l < termites; ++l) {
                this.spawnTermite(world, i, j, k);
            }
        }
    }

    @Override
    public void onBlockExploded(World world, int i, int j, int k, Explosion explosion) {
        int meta = world.getBlockMetadata(i, j, k);
        if(!world.isRemote && meta == 0 && world.rand.nextBoolean()) {
            this.spawnTermite(world, i, j, k);
        }
        super.onBlockExploded(world, i, j, k, explosion);
    }

    private void spawnTermite(World world, int i, int j, int k) {
        LOTREntityTermite termite = new LOTREntityTermite(world);
        termite.setLocationAndAngles(i + 0.5, j, k + 0.5, world.rand.nextFloat() * 360.0f, 0.0f);
        world.spawnEntityInWorld(termite);
    }

    @Override
    public int damageDropped(int i) {
        return i;
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random random) {
        return meta == 1 ? 1 : 0;
    }

    @Override
    protected ItemStack createStackedBlock(int i) {
        return new ItemStack(this, 1, 1);
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for(int i = 0; i <= 1; ++i) {
            list.add(new ItemStack(item, 1, i));
        }
    }
}
