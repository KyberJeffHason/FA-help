package lotr.common.block;

import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.LOTRCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class LOTRBlockWallBase extends BlockWall {
    private int subtypes;

    public LOTRBlockWallBase(Block block, int i) {
        super(block);
        this.setCreativeTab(LOTRCreativeTabs.tabBlock);
        this.subtypes = i;
    }

    @Override
    public boolean canPlaceTorchOnTop(World world, int i, int j, int k) {
        return true;
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for(int j = 0; j < this.subtypes; ++j) {
            list.add(new ItemStack(item, 1, j));
        }
    }
}
