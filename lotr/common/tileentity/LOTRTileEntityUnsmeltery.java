package lotr.common.tileentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.authlib.GameProfile;

import lotr.common.LOTRMod;
import lotr.common.block.LOTRBlockCraftingTable;
import lotr.common.inventory.LOTRContainerCraftingTable;
import lotr.common.item.LOTRItemCrossbow;
import lotr.common.item.LOTRItemMountArmor;
import lotr.common.item.LOTRItemThrowingAxe;
import lotr.common.item.LOTRMaterial;
import lotr.common.recipe.LOTRRecipePoisonWeapon;
import lotr.common.recipe.LOTRRecipes;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class LOTRTileEntityUnsmeltery extends LOTRTileEntityForgeBase {
  public static Random unsmeltingRand = new Random();
  
  public static Map<Pair<Item, Integer>, Integer> unsmeltableCraftingCounts = new HashMap<Pair<Item, Integer>, Integer>();
  
  public float prevRocking;
  
  public float rocking;
  
  public float prevRockingPhase;
  
  public float rockingPhase = unsmeltingRand.nextFloat() * 3.1415927F * 2.0F;
  
  public boolean prevServerActive;
  
  public boolean serverActive;
  
  public boolean clientActive;
  
  public int getForgeInvSize() {
    return 3;
  }
  
  public void setupForgeSlots() {
    this.inputSlots = new int[] { 0 };
    this.fuelSlot = 1;
    this.outputSlots = new int[] { 2 };
  }
  
  protected boolean canMachineInsertInput(ItemStack itemstack) {
    return (itemstack != null && getLargestUnsmeltingResult(itemstack) != null);
  }
  
  public void updateEntity() {
    super.updateEntity();
    if (!this.worldObj.isRemote) {
      this.prevServerActive = this.serverActive;
      this.serverActive = isSmelting();
      if (this.serverActive != this.prevServerActive)
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord); 
    } else {
      this.prevRocking = this.rocking;
      this.prevRockingPhase = this.rockingPhase;
      this.rockingPhase += 0.1F;
      if (this.clientActive) {
        this.rocking += 0.05F;
      } else {
        this.rocking -= 0.01F;
      } 
      this.rocking = MathHelper.clamp_float(this.rocking, 0.0F, 1.0F);
    } 
  }
  
  public float getRockingAmount(float tick) {
    float mag = this.prevRocking + (this.rocking - this.prevRocking) * tick;
    float phase = this.prevRockingPhase + (this.rockingPhase - this.prevRockingPhase) * tick;
    return mag * MathHelper.sin(phase);
  }
  
  public int getSmeltingDuration() {
    return 400;
  }
  
  protected boolean canDoSmelting() {
    ItemStack input = this.inventory[this.inputSlots[0]];
    if (input == null)
      return false; 
    ItemStack result = getLargestUnsmeltingResult(input);
    if (result == null)
      return false; 
    ItemStack output = this.inventory[this.outputSlots[0]];
    if (output == null)
      return true; 
    if (!output.isItemEqual(result))
      return false; 
    int resultSize = output.stackSize + result.stackSize;
    return (resultSize <= getInventoryStackLimit() && resultSize <= result.getMaxStackSize());
  }
  
  protected void doSmelt() {
    if (canDoSmelting()) {
      ItemStack input = this.inventory[this.inputSlots[0]];
      ItemStack result = getRandomUnsmeltingResult(input);
      if (result != null) {
        int slot = this.outputSlots[0];
        if (this.inventory[this.outputSlots[0]] == null) {
          this.inventory[this.outputSlots[0]] = result.copy();
        } else if (this.inventory[this.outputSlots[0]].isItemEqual(result)) {
          (this.inventory[this.outputSlots[0]]).stackSize += result.stackSize;
        } 
      } 
      (this.inventory[this.inputSlots[0]]).stackSize--;
      if ((this.inventory[this.inputSlots[0]]).stackSize <= 0)
        this.inventory[this.inputSlots[0]] = null; 
    } 
  }
  
  public String getForgeName() {
    return StatCollector.translateToLocal("container.lotr.unsmeltery");
  }
  
  public boolean canBeUnsmelted(ItemStack itemstack) {
    if (itemstack == null)
      return false; 
    ItemStack material = getEquipmentMaterial(itemstack);
    if (material != null) {
      if (TileEntityFurnace.getItemBurnTime(material) != 0)
        return false; 
      if (itemstack.getItem() instanceof net.minecraft.item.ItemBlock && Block.getBlockFromItem(itemstack.getItem()).getMaterial().getCanBurn())
        return false; 
      return (determineResourcesUsed(itemstack, material) > 0);
    } 
    return false;
  }
  
  public ItemStack getLargestUnsmeltingResult(ItemStack itemstack) {
      if (itemstack == null || !this.canBeUnsmelted(itemstack)) {
          return null;
      }
      ItemStack material = LOTRTileEntityUnsmeltery.getEquipmentMaterial(itemstack);
      int items = this.determineResourcesUsed(itemstack, material);
      int meta = material.getItemDamage();
      if (meta == 32767) {
          meta = 0;
      }
      return new ItemStack(material.getItem(), items, meta);
  }

  public ItemStack getRandomUnsmeltingResult(ItemStack itemstack) {
      int items_int;
      ItemStack result = this.getLargestUnsmeltingResult(itemstack);
      if (result == null) {
          return null;
      }
      float items = result.stackSize;
      items *= 0.8f;
      if (itemstack.isItemStackDamageable()) {
          items *= (float)(itemstack.getMaxDamage() - itemstack.getItemDamage()) / (float)itemstack.getMaxDamage();
      }
      if ((items_int = Math.round(items *= MathHelper.randomFloatClamp((Random)unsmeltingRand, (float)0.7f, (float)1.0f))) <= 0) {
          return null;
      }
      return new ItemStack(result.getItem(), items_int, result.getItemDamage());
  }
  
  public static ItemStack getEquipmentMaterial(ItemStack itemstack) {
    if (itemstack == null)
      return null; 
    Item item = itemstack.getItem();
    ItemStack material = null;
    if (item instanceof ItemTool) {
      material = ((ItemTool)item).func_150913_i().getRepairItemStack();
    } else if (item instanceof ItemSword) {
      material = LOTRMaterial.getToolMaterialByName(((ItemSword)item).getToolMaterialName()).getRepairItemStack();
    } else if (item instanceof LOTRItemCrossbow) {
      material = ((LOTRItemCrossbow)item).getCrossbowMaterial().getRepairItemStack();
    } else if (item instanceof LOTRItemThrowingAxe) {
      material = ((LOTRItemThrowingAxe)item).getAxeMaterial().getRepairItemStack();
    } else if (item instanceof ItemArmor) {
      material = new ItemStack(((ItemArmor)item).getArmorMaterial().func_151685_b());
    } else if (item instanceof LOTRItemMountArmor) {
      material = new ItemStack(((LOTRItemMountArmor)item).getMountArmorMaterial().func_151685_b());
    } 
    if (material != null) {
      if (item.getIsRepairable(itemstack, material))
        return material; 
    } else {
      if (item instanceof ItemHoe)
        return LOTRMaterial.getToolMaterialByName(((ItemHoe)item).getToolMaterialName()).getRepairItemStack(); 
      if (item == Items.bucket)
        return new ItemStack(Items.iron_ingot); 
      if (item == LOTRMod.silverRing)
        return new ItemStack(LOTRMod.silverNugget); 
      if (item == LOTRMod.goldRing)
        return new ItemStack(Items.gold_nugget); 
      if (item == LOTRMod.mithrilRing)
        return new ItemStack(LOTRMod.mithrilNugget); 
      if (item == LOTRMod.gobletGold)
        return new ItemStack(Items.gold_ingot); 
      if (item == LOTRMod.gobletSilver)
        return new ItemStack(LOTRMod.silver); 
      if (item == LOTRMod.gobletCopper)
        return new ItemStack(LOTRMod.bronze); 
    } 
    return null;
  }
  
  public int determineResourcesUsed(ItemStack itemstack, ItemStack material) {
    return determineResourcesUsed(itemstack, material, (List<IRecipe>)null);
  }
  
  public int determineResourcesUsed(ItemStack itemstack, ItemStack material, List<IRecipe> recursiveCheckedRecipes) {
    if (itemstack == null)
      return 0; 
    Pair<Item, Integer> key = Pair.of(itemstack.getItem(), Integer.valueOf(itemstack.getItemDamage()));
    if (unsmeltableCraftingCounts.containsKey(key))
      return ((Integer)unsmeltableCraftingCounts.get(key)).intValue(); 
    int count = 0;
    List<List> allRecipeLists = new ArrayList<List>();
    allRecipeLists.add(CraftingManager.getInstance().getRecipeList());
    EntityPlayer player = getProxyPlayer();
    for (LOTRBlockCraftingTable table : LOTRBlockCraftingTable.allCraftingTables) {
      Object container = LOTRMod.proxy.getServerGuiElement(table.tableGUIID, player, this.worldObj, 0, 0, 0);
      if (container instanceof LOTRContainerCraftingTable) {
    	  LOTRContainerCraftingTable containerTable = (LOTRContainerCraftingTable)container;
        allRecipeLists.add(containerTable.recipeList);
      } 
    } 
    allRecipeLists.add(LOTRRecipes.uncraftableUnsmeltingRecipes);
    if (recursiveCheckedRecipes == null)
      recursiveCheckedRecipes = new ArrayList<IRecipe>(); 
    label63: for (List recipes : allRecipeLists) {
      for (Object recipesObj : recipes) {
        IRecipe irecipe = (IRecipe)recipesObj;
        if (recursiveCheckedRecipes.contains(irecipe))
          continue; 
        ItemStack result = irecipe.getRecipeOutput();
        if (result != null && result.getItem() == itemstack.getItem() && (itemstack.isItemStackDamageable() || result.getItemDamage() == itemstack.getItemDamage())) {
          recursiveCheckedRecipes.add(irecipe);
          if (irecipe instanceof ShapedRecipes) {
            ShapedRecipes shaped = (ShapedRecipes)irecipe;
            ItemStack[] ingredients = shaped.recipeItems;
            int i = countMatchingIngredients(material, Arrays.asList(ingredients), recursiveCheckedRecipes);
            i /= result.stackSize;
            if (i > 0) {
              count = i;
              break label63;
            } 
          } 
          if (irecipe instanceof ShapelessRecipes) {
            ShapelessRecipes shapeless = (ShapelessRecipes)irecipe;
            List ingredients = shapeless.recipeItems;
            int i = countMatchingIngredients(material, ingredients, recursiveCheckedRecipes);
            i /= result.stackSize;
            if (i > 0) {
              count = i;
              break label63;
            } 
          } 
          if (irecipe instanceof ShapedOreRecipe) {
            ShapedOreRecipe shaped = (ShapedOreRecipe)irecipe;
            Object[] ingredients = shaped.getInput();
            int i = countMatchingIngredients(material, Arrays.asList(ingredients), recursiveCheckedRecipes);
            i /= result.stackSize;
            if (i > 0) {
              count = i;
              break label63;
            } 
          } 
          if (irecipe instanceof ShapelessOreRecipe) {
            ShapelessOreRecipe shapeless = (ShapelessOreRecipe)irecipe;
            List ingredients = shapeless.getInput();
            int i = countMatchingIngredients(material, ingredients, recursiveCheckedRecipes);
            i /= result.stackSize;
            if (i > 0) {
              count = i;
              break label63;
            } 
          } 
          if (irecipe instanceof LOTRRecipePoisonWeapon) {
            LOTRRecipePoisonWeapon poison = (LOTRRecipePoisonWeapon)irecipe;
            Object[] ingredients = { poison.getInputItem() };
            int i = countMatchingIngredients(material, Arrays.asList(ingredients), recursiveCheckedRecipes);
            i /= result.stackSize;
            if (i > 0) {
              count = i;
              break label63;
            } 
          } 
        } 
      } 
    } 
    unsmeltableCraftingCounts.put(key, Integer.valueOf(count));
    return count;
  }
  
  public EntityPlayer getProxyPlayer() {
    if (!this.worldObj.isRemote)
      return (EntityPlayer)FakePlayerFactory.get((WorldServer)this.worldObj, new GameProfile(null, "LOTRUnsmeltery")); 
    return LOTRMod.proxy.getClientPlayer();
  }
  
  public int countMatchingIngredients(ItemStack material, List ingredientList, List<IRecipe> recursiveCheckedRecipes) {
    int i = 0;
    for (Object obj : ingredientList) {
      if (obj instanceof ItemStack) {
        ItemStack ingredient = (ItemStack)obj;
        if (OreDictionary.itemMatches(material, ingredient, false)) {
          i++;
          continue;
        } 
        int sub = determineResourcesUsed(ingredient, material, recursiveCheckedRecipes);
        if (sub > 0)
          i += sub; 
        continue;
      } 
      if (obj instanceof List) {
        List<ItemStack> oreIngredients = (List<ItemStack>)obj;
        boolean matched = false;
        for (ItemStack ingredient : oreIngredients) {
          if (OreDictionary.itemMatches(material, ingredient, false)) {
            matched = true;
            break;
          } 
        } 
        if (matched) {
          i++;
          continue;
        } 
        for (ItemStack ingredient : oreIngredients) {
          int sub = determineResourcesUsed(ingredient, material, recursiveCheckedRecipes);
          if (sub > 0)
            i += sub; 
        } 
      } 
    } 
    return i;
  }
  
  public Packet getDescriptionPacket() {
    NBTTagCompound data = new NBTTagCompound();
    data.setBoolean("Active", this.serverActive);
    return (Packet)new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, data);
  }
  
  public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) {
    NBTTagCompound data = packet.func_148857_g();
    this.clientActive = data.getBoolean("Active");
  }
}