package lotr.common.block;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import lotr.common.LOTRReflection;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.*;
import net.minecraft.util.*;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class LOTRBlockReplacement {
    private static boolean initForgeHooks = false;

    public static void replaceVanillaBlock(Block oldBlock, Block newBlock, Class<? extends ItemBlock> itemClass) {
        try {
            Item oldItem = Item.getItemFromBlock(oldBlock);
            int id = Block.blockRegistry.getIDForObject(oldBlock);
            String blockName = Reflect.getBlockName(oldBlock);
            String registryName = Block.blockRegistry.getNameForObject(oldBlock);
            String itemblockName = blockName;
            if(oldItem != null) {
                itemblockName = Reflect.getItemName(oldItem);
            }
            newBlock.setBlockName(blockName);
            Reflect.overwriteBlockList(oldBlock, newBlock);
            Reflect.getUnderlyingIntMap(Block.blockRegistry).func_148746_a(newBlock, id);
            Reflect.getUnderlyingObjMap(Block.blockRegistry).put(registryName, newBlock);
            if(!initForgeHooks) {
                ForgeHooks.isToolEffective(new ItemStack(Items.iron_shovel), Blocks.dirt, 0);
                initForgeHooks = true;
            }
            for(int meta = 0; meta <= 15; ++meta) {
                newBlock.setHarvestLevel(oldBlock.getHarvestTool(meta), oldBlock.getHarvestLevel(meta), meta);
            }
            if(itemClass != null) {
                Constructor<?>[] itemCtors;
                Constructor<?> itemCtor = null;
                for(Constructor<?> ct : itemCtors = itemClass.getConstructors()) {
                    Class<?>[] params = ct.getParameterTypes();
                    if(params.length != 1 || !Block.class.isAssignableFrom(params[0])) continue;
                    itemCtor = ct;
                    break;
                }
                ItemBlock itemblock = ((ItemBlock) itemCtor.newInstance(newBlock)).setUnlocalizedName(itemblockName);
                Reflect.getUnderlyingIntMap(Item.itemRegistry).func_148746_a(itemblock, id);
                Reflect.getUnderlyingObjMap(Item.itemRegistry).put(registryName, itemblock);
                LOTRBlockReplacement.replaceBlockStats(id, newBlock, itemblock);
                LOTRBlockReplacement.replaceRecipesEtc(itemblock);
            }
        }
        catch(Exception e) {
            FMLLog.severe("Failed to replace vanilla block %s", oldBlock.getUnlocalizedName());
            throw new RuntimeException(e);
        }
    }

    private static void replaceBlockStats(int id, Block newBlock, ItemBlock itemblock) {
        LOTRBlockReplacement.replaceStat(id, StatList.mineBlockStatArray, new StatCrafting("stat.mineBlock." + id, new ChatComponentTranslation("stat.mineBlock", new ItemStack(newBlock).func_151000_E()), itemblock));
        LOTRBlockReplacement.replaceStat(id, StatList.objectUseStats, new StatCrafting("stat.useItem." + id, new ChatComponentTranslation("stat.useItem", new ItemStack(itemblock).func_151000_E()), itemblock));
        LOTRBlockReplacement.replaceStat(id, StatList.objectCraftStats, new StatCrafting("stat.craftItem." + id, new ChatComponentTranslation("stat.craftItem", new ItemStack(itemblock).func_151000_E()), itemblock));
    }

    public static void replaceVanillaItem(Item oldItem, Item newItem) {
        try {
            int id = Item.itemRegistry.getIDForObject(oldItem);
            String itemName = Reflect.getItemName(oldItem);
            String registryName = Item.itemRegistry.getNameForObject(oldItem);
            newItem.setUnlocalizedName(itemName);
            Reflect.overwriteItemList(oldItem, newItem);
            Reflect.getUnderlyingIntMap(Item.itemRegistry).func_148746_a(newItem, id);
            Reflect.getUnderlyingObjMap(Item.itemRegistry).put(registryName, newItem);
            LOTRBlockReplacement.replaceItemStats(id, newItem);
            LOTRBlockReplacement.replaceRecipesEtc(newItem);
        }
        catch(Exception e) {
            FMLLog.severe("Failed to replace vanilla item %s", oldItem.getUnlocalizedName());
            throw new RuntimeException(e);
        }
    }

    private static void replaceItemStats(int id, Item newItem) {
        LOTRBlockReplacement.replaceStat(id, StatList.objectUseStats, new StatCrafting("stat.useItem." + id, new ChatComponentTranslation("stat.useItem", new ItemStack(newItem).func_151000_E()), newItem));
        LOTRBlockReplacement.replaceStat(id, StatList.objectCraftStats, new StatCrafting("stat.craftItem." + id, new ChatComponentTranslation("stat.craftItem", new ItemStack(newItem).func_151000_E()), newItem));
        if(newItem.isDamageable()) {
            LOTRBlockReplacement.replaceStat(id, StatList.objectBreakStats, new StatCrafting("stat.breakItem." + id, new ChatComponentTranslation("stat.breakItem", new ItemStack(newItem).func_151000_E()), newItem));
        }
    }

    private static void replaceStat(int id, StatBase[] stats, StatBase newStat) {
        StatBase oldStat = stats[id];
        if(oldStat != null && oldStat.statId.equals(newStat.statId)) {
            for(int i = 0; i < stats.length; ++i) {
                StatBase otherOldStat = stats[i];
                if(otherOldStat == null || !otherOldStat.statId.equals(oldStat.statId)) continue;
                StatList.allStats.remove(otherOldStat);
                StatList.objectMineStats.remove(otherOldStat);
                StatList.itemStats.remove(otherOldStat);
                StatList.generalStats.remove(otherOldStat);
                Reflect.getOneShotStats().remove(otherOldStat.statId);
                stats[i] = newStat;
            }
            newStat.registerStat();
        }
    }

    private static void replaceRecipesEtc(Item newItem) {
        String newItemName = newItem.getUnlocalizedName();
        List craftingRecipes = CraftingManager.getInstance().getRecipeList();
        for(Object obj : craftingRecipes) {
            IRecipe recipe;
            ItemStack output;
            if(obj instanceof ShapedRecipes && (output = ((ShapedRecipes) (recipe = (ShapedRecipes) obj)).getRecipeOutput()) != null && output.getItem() != null && output.getItem().getUnlocalizedName().equals(newItemName)) {
                LOTRBlockReplacement.injectReplacementItem(output, newItem);
            }
            if(obj instanceof ShapelessRecipes && (output = ((ShapelessRecipes) (recipe = (ShapelessRecipes) obj)).getRecipeOutput()) != null && output.getItem() != null && output.getItem().getUnlocalizedName().equals(newItemName)) {
                LOTRBlockReplacement.injectReplacementItem(output, newItem);
            }
            if(obj instanceof ShapedOreRecipe && (output = ((ShapedOreRecipe) (recipe = (ShapedOreRecipe) obj)).getRecipeOutput()) != null && output.getItem() != null && output.getItem().getUnlocalizedName().equals(newItemName)) {
                LOTRBlockReplacement.injectReplacementItem(output, newItem);
            }
            if(!(obj instanceof ShapelessOreRecipe) || (output = ((ShapelessOreRecipe) (recipe = (ShapelessOreRecipe) obj)).getRecipeOutput()) == null || output.getItem() == null || !output.getItem().getUnlocalizedName().equals(newItemName)) continue;
            LOTRBlockReplacement.injectReplacementItem(output, newItem);
        }
        for(Object obj : AchievementList.achievementList) {
            Achievement a = (Achievement) obj;
            ItemStack icon = a.theItemStack;
            if(!icon.getItem().getUnlocalizedName().equals(newItem.getUnlocalizedName())) continue;
            LOTRBlockReplacement.injectReplacementItem(icon, newItem);
        }
    }

    private static void injectReplacementItem(ItemStack itemstack, Item newItem) {
        NBTTagCompound nbt = new NBTTagCompound();
        itemstack.writeToNBT(nbt);
        itemstack.readFromNBT(nbt);
    }

    private static class Reflect {
        private Reflect() {
        }

        private static void overwriteBlockList(Block oldBlock, Block newBlock) {
            try {
                Field[] declaredFields;
                Field field = null;
                for(Field f : declaredFields = Blocks.class.getDeclaredFields()) {
                    LOTRReflection.unlockFinalField(f);
                    if(f.get(null) != oldBlock) continue;
                    field = f;
                    break;
                }
                LOTRReflection.setFinalField(Blocks.class, null, newBlock, field);
            }
            catch(Exception e) {
                LOTRReflection.logFailure(e);
            }
        }

        private static void overwriteItemList(Item oldItem, Item newItem) {
            try {
                Field[] declaredFields;
                Field field = null;
                for(Field f : declaredFields = Items.class.getDeclaredFields()) {
                    LOTRReflection.unlockFinalField(f);
                    if(f.get(null) != oldItem) continue;
                    field = f;
                    break;
                }
                LOTRReflection.setFinalField(Items.class, null, newItem, field);
            }
            catch(Exception e) {
                LOTRReflection.logFailure(e);
            }
        }

        private static String getBlockName(Block block) {
            try {
                return (String) ObfuscationReflectionHelper.getPrivateValue(Block.class, block, "unlocalizedName", "field_149770_b");
            }
            catch(Exception e) {
                LOTRReflection.logFailure(e);
                return null;
            }
        }

        private static String getItemName(Item item) {
            try {
                return (String) ObfuscationReflectionHelper.getPrivateValue(Item.class, item, "unlocalizedName", "field_77774_bZ");
            }
            catch(Exception e) {
                LOTRReflection.logFailure(e);
                return null;
            }
        }

        private static ObjectIntIdentityMap getUnderlyingIntMap(RegistryNamespaced registry) {
            try {
                return (ObjectIntIdentityMap) ObfuscationReflectionHelper.getPrivateValue(RegistryNamespaced.class, registry, "underlyingIntegerMap", "field_148759_a");
            }
            catch(Exception e) {
                LOTRReflection.logFailure(e);
                return null;
            }
        }

        private static Map getUnderlyingObjMap(RegistryNamespaced registry) {
            try {
                return (Map) ObfuscationReflectionHelper.getPrivateValue(RegistrySimple.class, registry, "registryObjects", "field_82596_a");
            }
            catch(Exception e) {
                LOTRReflection.logFailure(e);
                return null;
            }
        }

        private static Map getOneShotStats() {
            try {
                return (Map) ObfuscationReflectionHelper.getPrivateValue(StatList.class, null, "oneShotStats", "field_75942_a");
            }
            catch(Exception e) {
                LOTRReflection.logFailure(e);
                return null;
            }
        }
    }

}
