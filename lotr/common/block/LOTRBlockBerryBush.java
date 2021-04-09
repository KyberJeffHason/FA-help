package lotr.common.block;

import java.util.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.LOTRCreativeTabs;
import lotr.common.LOTRMod;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class LOTRBlockBerryBush extends Block implements IPlantable, IGrowable {
    public LOTRBlockBerryBush() {
        super(Material.plants);
        this.setTickRandomly(true);
        this.setCreativeTab(LOTRCreativeTabs.tabDeco);
        this.setHardness(0.4f);
        this.setStepSound(Block.soundTypeGrass);
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public IIcon getIcon(int i, int j) {
        int berryType = LOTRBlockBerryBush.getBerryType(j);
        BushType type = BushType.forMeta(berryType);
        if(LOTRBlockBerryBush.hasBerries(j)) {
            return type.iconGrown;
        }
        return type.iconBare;
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconregister) {
        for(BushType type : BushType.values()) {
            type.iconBare = iconregister.registerIcon(this.getTextureName() + "_" + type.bushName + "_bare");
            type.iconGrown = iconregister.registerIcon(this.getTextureName() + "_" + type.bushName);
        }
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for(BushType type : BushType.values()) {
            int meta = type.bushMeta;
            list.add(new ItemStack(item, 1, LOTRBlockBerryBush.setHasBerries(meta, true)));
            list.add(new ItemStack(item, 1, LOTRBlockBerryBush.setHasBerries(meta, false)));
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    public static boolean hasBerries(int meta) {
        return (meta & 8) != 0;
    }

    public static int getBerryType(int meta) {
        return meta & 7;
    }

    public static int setHasBerries(int meta, boolean flag) {
        if(flag) {
            return LOTRBlockBerryBush.getBerryType(meta) | 8;
        }
        return LOTRBlockBerryBush.getBerryType(meta);
    }

    @Override
    public void updateTick(World world, int i, int j, int k, Random random) {
        int meta = world.getBlockMetadata(i, j, k);
        if(!world.isRemote && !LOTRBlockBerryBush.hasBerries(meta)) {
            float growth = this.getGrowthFactor(world, i, j, k);
            if(random.nextFloat() < growth) {
                this.growBerries(world, i, j, k);
            }
        }
    }

    private void growBerries(World world, int i, int j, int k) {
        int meta = world.getBlockMetadata(i, j, k);
        world.setBlockMetadataWithNotify(i, j, k, LOTRBlockBerryBush.setHasBerries(meta, true), 3);
    }

    private float getGrowthFactor(World world, int i, int j, int k) {
        float growth;
        Block below = world.getBlock(i, j - 1, k);
        if(below.canSustainPlant(world, i, j - 1, k, ForgeDirection.UP, this) && world.getBlockLightValue(i, j + 1, k) >= 9) {
            int i1;
            int k1;
            growth = 1.0f;
            boolean bushAdjacent = false;
            boolean bushAdjacentCorner = false;
            block0: for(i1 = i - 1; i1 <= i + 1; ++i1) {
                for(k1 = k - 1; k1 <= k + 1; ++k1) {
                    if(i1 == i && k1 == k || !(world.getBlock(i1, j, k1) instanceof LOTRBlockBerryBush)) continue;
                    bushAdjacent = true;
                    break block0;
                }
            }
            for(i1 = i - 1; i1 <= i + 1; ++i1) {
                for(k1 = k - 1; k1 <= k + 1; ++k1) {
                    float growthBonus = 0.0f;
                    if(world.getBlock(i1, j - 1, k1).canSustainPlant(world, i1, j - 1, k1, ForgeDirection.UP, this)) {
                        growthBonus = 1.0f;
                        if(world.getBlock(i1, j - 1, k1).isFertile(world, i1, j - 1, k1)) {
                            growthBonus = 3.0f;
                        }
                    }
                    if(i1 != i || k1 != k) {
                        growthBonus /= 4.0f;
                    }
                    growth += growthBonus;
                }
            }
            if(growth > 0.0f) {
                if(bushAdjacent) {
                    growth /= 2.0f;
                }
                if(world.isRaining()) {
                    growth *= 3.0f;
                }
                return growth / 150.0f;
            }
        }
        if(below.canSustainPlant(world, i, j - 1, k, ForgeDirection.UP, (IPlantable) (Blocks.sapling))) {
            growth = world.getBlockLightValue(i, j + 1, k) / 2000.0f;
            if(world.isRaining()) {
                growth *= 3.0f;
            }
            return growth;
        }
        return 0.0f;
    }

    @Override
    public int damageDropped(int i) {
        return i;
    }

    @Override
    public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int side, float f, float f1, float f2) {
        int meta = world.getBlockMetadata(i, j, k);
        if(LOTRBlockBerryBush.hasBerries(meta)) {
            world.setBlockMetadataWithNotify(i, j, k, LOTRBlockBerryBush.setHasBerries(meta, false), 3);
            if(!world.isRemote) {
                ArrayList<ItemStack> drops = this.getBerryDrops(world, i, j, k, meta);
                for(ItemStack berry : drops) {
                    this.dropBlockAsItem(world, i, j, k, berry);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int i, int j, int k, int meta, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(this, 1, LOTRBlockBerryBush.setHasBerries(meta, false)));
        drops.addAll(this.getBerryDrops(world, i, j, k, meta));
        return drops;
    }

    private ArrayList<ItemStack> getBerryDrops(World world, int i, int j, int k, int meta) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        if(LOTRBlockBerryBush.hasBerries(meta)) {
            int berryType = LOTRBlockBerryBush.getBerryType(meta);
            Item berry = null;
            int berries = 1 + world.rand.nextInt(4);
            switch(berryType) {
                case 0: {
                    berry = LOTRMod.blueberry;
                    break;
                }
                case 1: {
                    berry = LOTRMod.blackberry;
                    break;
                }
                case 2: {
                    berry = LOTRMod.raspberry;
                    break;
                }
                case 3: {
                    berry = LOTRMod.cranberry;
                    break;
                }
                case 4: {
                    berry = LOTRMod.elderberry;
                    break;
                }
                case 5: {
                    berry = LOTRMod.wildberry;
                }
            }
            if(berry != null) {
                for(int l = 0; l < berries; ++l) {
                    drops.add(new ItemStack(berry));
                }
            }
        }
        return drops;
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, int i, int j, int k) {
        return EnumPlantType.Crop;
    }

    @Override
    public Block getPlant(IBlockAccess world, int i, int j, int k) {
        return this;
    }

    @Override
    public int getPlantMetadata(IBlockAccess world, int i, int j, int k) {
        return world.getBlockMetadata(i, j, k);
    }

    @Override
    public boolean func_149851_a(World world, int i, int j, int k, boolean isRemote) {
        return !LOTRBlockBerryBush.hasBerries(world.getBlockMetadata(i, j, k));
    }

    @Override
    public boolean func_149852_a(World world, Random random, int i, int j, int k) {
        return true;
    }

    @Override
    public void func_149853_b(World world, Random random, int i, int j, int k) {
        if(random.nextInt(3) == 0) {
            this.growBerries(world, i, j, k);
        }
    }

    public static enum BushType {
        BLUEBERRY(0, "blueberry", false),
        BLACKBERRY(1, "blackberry", false),
        RASPBERRY(2, "raspberry", false),
        CRANBERRY(3, "cranberry", false),
        ELDERBERRY(4, "elderberry", false),
        WILDBERRY(5, "wildberry", true);

        public final int bushMeta;
        public final String bushName;
        public final boolean poisonous;
        @SideOnly(value = Side.CLIENT)
        public IIcon iconBare;
        @SideOnly(value = Side.CLIENT)
        public IIcon iconGrown;

        private BushType(int i, String s, boolean flag) {
            this.bushMeta = i;
            this.bushName = s;
            this.poisonous = flag;
        }

        public static BushType randomType(Random rand) {
            return BushType.values()[rand.nextInt(BushType.values().length)];
        }

        public static BushType forMeta(int i) {
            for(BushType type : BushType.values()) {
                if(type.bushMeta != i) continue;
                return type;
            }
            return BushType.values()[0];
        }
    }

}
