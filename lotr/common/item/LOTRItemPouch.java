package lotr.common.item;

import java.util.List;
import java.util.Random;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.LOTRCreativeTabs;
import lotr.common.LOTRMod;
import lotr.common.inventory.LOTRContainerPouch;
import lotr.common.inventory.LOTRInventoryPouch;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class LOTRItemPouch extends Item {
    private static int POUCH_COLOR = 10841676;
    @SideOnly(value = Side.CLIENT)
    private IIcon[] pouchIcons;
    @SideOnly(value = Side.CLIENT)
    private IIcon[] pouchIconsOpen;
    @SideOnly(value = Side.CLIENT)
    private IIcon[] overlayIcons;
    @SideOnly(value = Side.CLIENT)
    private IIcon[] overlayIconsOpen;
    private static String[] pouchTypes = new String[] {"small", "medium", "large"};

    public LOTRItemPouch() {
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
        this.setCreativeTab(LOTRCreativeTabs.tabMisc);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
        entityplayer.openGui(LOTRMod.instance, 15, world, 0, 0, 0);
        return itemstack;
    }

    public static int getCapacity(ItemStack itemstack) {
        return LOTRItemPouch.getCapacityForMeta(itemstack.getItemDamage());
    }

    public static int getCapacityForMeta(int i) {
        return (i + 1) * 9;
    }

    public static int getMaxPouchCapacity() {
        return LOTRItemPouch.getCapacityForMeta(pouchTypes.length - 1);
    }

    public static int getRandomPouchSize(Random random) {
        float f = random.nextFloat();
        if(f < 0.6f) {
            return 0;
        }
        if(f < 0.9f) {
            return 1;
        }
        return 2;
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconregister) {
        this.pouchIcons = new IIcon[pouchTypes.length];
        this.pouchIconsOpen = new IIcon[pouchTypes.length];
        this.overlayIcons = new IIcon[pouchTypes.length];
        this.overlayIconsOpen = new IIcon[pouchTypes.length];
        for(int i = 0; i < pouchTypes.length; ++i) {
            this.pouchIcons[i] = iconregister.registerIcon(this.getIconString() + "_" + pouchTypes[i]);
            this.pouchIconsOpen[i] = iconregister.registerIcon(this.getIconString() + "_" + pouchTypes[i] + "_open");
            this.overlayIcons[i] = iconregister.registerIcon(this.getIconString() + "_" + pouchTypes[i] + "_overlay");
            this.overlayIconsOpen[i] = iconregister.registerIcon(this.getIconString() + "_" + pouchTypes[i] + "_open_overlay");
        }
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    public int getRenderPasses(int meta) {
        return 2;
    }

    @Override
    public IIcon getIcon(ItemStack itemstack, int pass) {
        int meta;
        Container container;
        boolean open = false;
        EntityPlayer entityplayer = LOTRMod.proxy.getClientPlayer();
        if(entityplayer != null && (container = entityplayer.openContainer) instanceof LOTRContainerPouch && itemstack == entityplayer.getHeldItem()) {
            open = true;
        }
        if((meta = itemstack.getItemDamage()) >= this.pouchIcons.length) {
            meta = 0;
        }
        if(open) {
            return pass > 0 ? this.overlayIconsOpen[meta] : this.pouchIconsOpen[meta];
        }
        return pass > 0 ? this.overlayIcons[meta] : this.pouchIcons[meta];
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public int getColorFromItemStack(ItemStack itemstack, int pass) {
        if(pass == 0) {
            return LOTRItemPouch.getPouchColor(itemstack);
        }
        return 16777215;
    }

    public static int getPouchColor(ItemStack itemstack) {
        int dye = LOTRItemPouch.getSavedDyeColor(itemstack);
        if(dye != -1) {
            return dye;
        }
        return POUCH_COLOR;
    }

    private static int getSavedDyeColor(ItemStack itemstack) {
        if(itemstack.getTagCompound() != null && itemstack.getTagCompound().hasKey("PouchColor")) {
            return itemstack.getTagCompound().getInteger("PouchColor");
        }
        return -1;
    }

    public static boolean isPouchDyed(ItemStack itemstack) {
        return LOTRItemPouch.getSavedDyeColor(itemstack) != -1;
    }

    public static void setPouchColor(ItemStack itemstack, int i) {
        if(itemstack.getTagCompound() == null) {
            itemstack.setTagCompound(new NBTTagCompound());
        }
        itemstack.getTagCompound().setInteger("PouchColor", i);
    }

    public static void removePouchDye(ItemStack itemstack) {
        if(itemstack.getTagCompound() != null) {
            itemstack.getTagCompound().removeTag("PouchColor");
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        return super.getUnlocalizedName() + "." + itemstack.getItemDamage();
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag) {
        int slots = LOTRItemPouch.getCapacity(itemstack);
        int slotsFull = 0;
        LOTRInventoryPouch pouchInv = new LOTRInventoryPouch(itemstack);
        for(int i = 0; i < pouchInv.getSizeInventory(); ++i) {
            ItemStack slotItem = pouchInv.getStackInSlot(i);
            if(slotItem == null) continue;
            ++slotsFull;
        }
        list.add(StatCollector.translateToLocalFormatted("item.lotr.pouch.slots", slotsFull, slots));
        if(LOTRItemPouch.isPouchDyed(itemstack)) {
            list.add(StatCollector.translateToLocal("item.lotr.pouch.dyed"));
        }
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for(int i = 0; i < pouchTypes.length; ++i) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    public static boolean tryAddItemToPouch(ItemStack pouch, ItemStack itemstack, boolean requireMatchInPouch) {
        if(itemstack != null && itemstack.stackSize > 0) {
            LOTRInventoryPouch pouchInv = new LOTRInventoryPouch(pouch);
            for(int i = 0; i < pouchInv.getSizeInventory() && itemstack.stackSize > 0; ++i) {
                int difference;
                ItemStack itemInSlot = pouchInv.getStackInSlot(i);
                if(itemInSlot != null ? itemInSlot.stackSize >= itemInSlot.getMaxStackSize() || itemInSlot.getItem() != itemstack.getItem() || !itemInSlot.isStackable() || itemInSlot.getHasSubtypes() && itemInSlot.getItemDamage() != itemstack.getItemDamage() || !ItemStack.areItemStackTagsEqual(itemInSlot, itemstack) : requireMatchInPouch) continue;
                if(itemInSlot == null) {
                    pouchInv.setInventorySlotContents(i, itemstack);
                    return true;
                }
                int maxStackSize = itemInSlot.getMaxStackSize();
                if(pouchInv.getInventoryStackLimit() < maxStackSize) {
                    maxStackSize = pouchInv.getInventoryStackLimit();
                }
                if((difference = maxStackSize - itemInSlot.stackSize) > itemstack.stackSize) {
                    difference = itemstack.stackSize;
                }
                itemstack.stackSize -= difference;
                itemInSlot.stackSize += difference;
                pouchInv.setInventorySlotContents(i, itemInSlot);
                if(itemstack.stackSize > 0) continue;
                return true;
            }
        }
        return false;
    }
}
