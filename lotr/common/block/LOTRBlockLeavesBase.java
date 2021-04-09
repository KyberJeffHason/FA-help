package lotr.common.block;

import java.util.ArrayList;
import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.*;
import lotr.common.world.LOTRWorldProvider;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class LOTRBlockLeavesBase extends BlockLeaves {
    public static List allLeafBlocks = new ArrayList();
    @SideOnly(value = Side.CLIENT)
    private IIcon[][] leafIcons;
    private String[] leafNames;
    private boolean[] seasonal;
    private String vanillaTextureName;

    public LOTRBlockLeavesBase() {
        this(false, null);
    }

    public LOTRBlockLeavesBase(boolean vanilla, String vname) {
        if(vanilla) {
            this.setCreativeTab(CreativeTabs.tabDecorations);
            this.vanillaTextureName = vname;
        }
        else {
            this.setCreativeTab(LOTRCreativeTabs.tabDeco);
        }
        allLeafBlocks.add(this);
    }

    protected void setLeafNames(String... s) {
        this.leafNames = s;
        this.setSeasonal(new boolean[s.length]);
    }

    @Override
    public String[] func_150125_e() {
        return this.leafNames;
    }

    public String[] getAllLeafNames() {
        return this.leafNames;
    }

    protected void setSeasonal(boolean... b) {
        if(b.length != this.leafNames.length) {
            throw new IllegalArgumentException("Leaf seasons length must match number of types");
        }
        this.seasonal = b;
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public int getRenderColor(int i) {
        return 16777215;
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public int colorMultiplier(IBlockAccess world, int i, int j, int k) {
        return 16777215;
    }

    protected static int getBiomeLeafColor(IBlockAccess world, int i, int j, int k) {
        int r = 0;
        int g = 0;
        int b = 0;
        int count = 0;
        int range = 1;
        for(int i1 = -range; i1 <= range; ++i1) {
            for(int k1 = -range; k1 <= range; ++k1) {
                int biomeColor = world.getBiomeGenForCoords(i + i1, k + k1).getBiomeFoliageColor(i + i1, j, k + k1);
                r += (biomeColor & 0xFF0000) >> 16;
                g += (biomeColor & 0xFF00) >> 8;
                b += biomeColor & 0xFF;
                ++count;
            }
        }
        return (r / count & 0xFF) << 16 | (g / count & 0xFF) << 8 | b / count & 0xFF;
    }

    protected boolean shouldOakUseBiomeColor() {
        LOTRDate.Season season = LOTRDate.ShireReckoning.getSeason();
        return season == LOTRDate.Season.SPRING || season == LOTRDate.Season.SUMMER || !(LOTRMod.proxy.getClientWorld().provider instanceof LOTRWorldProvider);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int i, int j, int k, int meta, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        int saplingChanceBase = this.getSaplingChance(meta & 3);
        int saplingChance = this.calcFortuneModifiedDropChance(saplingChanceBase, fortune);
        if(world.rand.nextInt(saplingChance) == 0) {
            drops.add(new ItemStack(this.getItemDropped(meta, world.rand, fortune), 1, this.damageDropped(meta)));
        }
        this.addSpecialLeafDrops(drops, world, i, j, k, meta, fortune);
        return drops;
    }

    protected int getSaplingChance(int meta) {
        return 20;
    }

    protected void addSpecialLeafDrops(ArrayList drops, World world, int i, int j, int k, int meta, int fortune) {
    }

    protected int calcFortuneModifiedDropChance(int baseChance, int fortune) {
        int chance = baseChance;
        if(fortune > 0) {
            chance -= 2 << fortune;
            chance = Math.max(chance, baseChance / 2);
            chance = Math.max(chance, 1);
        }
        return chance;
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public IIcon getIcon(int i, int j) {
        int meta = j & 3;
        if(meta >= this.leafNames.length) {
            meta = 0;
        }
        return this.leafIcons[meta][this.field_150121_P ? 0 : 1];
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconregister) {
        this.leafIcons = new IIcon[this.leafNames.length][2];
        for(int i = 0; i < this.leafNames.length; ++i) {
            IIcon fancy = iconregister.registerIcon(this.getTextureName() + "_" + this.leafNames[i] + "_fancy");
            IIcon fast = iconregister.registerIcon(this.getTextureName() + "_" + this.leafNames[i] + "_fast");
            this.leafIcons[i][0] = fancy;
            this.leafIcons[i][1] = fast;
        }
    }

    @Override
    public String getTextureName() {
        if(this.vanillaTextureName != null) {
            return this.vanillaTextureName;
        }
        return super.getTextureName();
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for(int j = 0; j < this.leafNames.length; ++j) {
            list.add(new ItemStack(item, 1, j));
        }
    }

    public static void setAllGraphicsLevels(boolean flag) {
        for(Object allLeafBlock : allLeafBlocks) {
            ((LOTRBlockLeavesBase) allLeafBlock).setGraphicsLevel(flag);
        }
    }
}
