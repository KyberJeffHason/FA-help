package lotr.common.block;

import lotr.common.LOTRCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;

public class LOTRBlockStairs extends BlockStairs {
    public LOTRBlockStairs(Block block, int metadata) {
        super(block, metadata);
        this.setCreativeTab(LOTRCreativeTabs.tabBlock);
        this.useNeighborBrightness = true;
    }
}
