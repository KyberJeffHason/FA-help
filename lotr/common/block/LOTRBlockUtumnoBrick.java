package lotr.common.block;

import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.LOTRCreativeTabs;
import lotr.common.world.LOTRWorldProviderUtumno;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class LOTRBlockUtumnoBrick extends Block implements LOTRWorldProviderUtumno.UtumnoBlock {
    @SideOnly(value = Side.CLIENT)
    private IIcon[] brickIcons;
    private String[] brickNames = new String[] {"fire", "burning", "ice", "iceGlowing", "obsidian", "obsidianFire", "iceTile", "obsidianTile", "fireTile"};

    public LOTRBlockUtumnoBrick() {
        super(Material.rock);
        this.setCreativeTab(LOTRCreativeTabs.tabBlock);
        this.setHardness(1.5f);
        this.setResistance(Float.MAX_VALUE);
        this.setStepSound(Block.soundTypeStone);
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public IIcon getIcon(int i, int j) {
        if(j >= this.brickNames.length) {
            j = 0;
        }
        return this.brickIcons[j];
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconregister) {
        this.brickIcons = new IIcon[this.brickNames.length];
        for(int i = 0; i < this.brickNames.length; ++i) {
            this.brickIcons[i] = iconregister.registerIcon(this.getTextureName() + "_" + this.brickNames[i]);
        }
    }

    @Override
    public int damageDropped(int i) {
        return i;
    }

    @Override
    public int getLightValue(IBlockAccess world, int i, int j, int k) {
        int meta = world.getBlockMetadata(i, j, k);
        if(meta == 1 || meta == 3 || meta == 5) {
            return 12;
        }
        return super.getLightValue(world, i, j, k);
    }

    @Override
    public boolean isFireSource(World world, int i, int j, int k, ForgeDirection side) {
        return this.isFlammable(world, i, j, k, side);
    }

    @Override
    public boolean isFlammable(IBlockAccess world, int i, int j, int k, ForgeDirection side) {
        int meta = world.getBlockMetadata(i, j, k);
        if(meta == 0 || meta == 1) {
            return true;
        }
        return super.isFlammable(world, i, j, k, side);
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for(int i = 0; i < this.brickNames.length; ++i) {
            list.add(new ItemStack(item, 1, i));
        }
    }
}
