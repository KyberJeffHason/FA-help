package lotr.common.item;

import lotr.common.LOTRCreativeTabs;
import lotr.common.LOTRMod;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class LOTRItemSting extends LOTRItemDagger implements LOTRStoryItem {
    public LOTRItemSting() {
        super(Item.ToolMaterial.IRON);
        this.setMaxDamage(700);
        this.setCreativeTab(LOTRCreativeTabs.tabStory);
        this.lotrWeaponDamage += 2.0f;
    }

    @Override
    public float func_150893_a(ItemStack itemstack, Block block) {
        if(block == LOTRMod.webUngoliant) {
            return 15.0f;
        }
        return super.func_150893_a(itemstack, block);
    }
}
