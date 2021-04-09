package lotr.common.item;

import lotr.common.LOTRCreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;

public class LOTRItemAxe extends ItemAxe {
    public LOTRItemAxe(LOTRMaterial material) {
        this(material.toToolMaterial());
    }

    public LOTRItemAxe(Item.ToolMaterial material) {
        super(material);
        this.setCreativeTab(LOTRCreativeTabs.tabTools);
        this.setHarvestLevel("axe", material.getHarvestLevel());
    }
}
