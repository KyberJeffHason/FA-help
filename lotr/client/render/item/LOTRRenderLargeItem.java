package lotr.client.render.item;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import lotr.client.LOTRClientProxy;
import lotr.common.item.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.IItemRenderer;

public class LOTRRenderLargeItem implements IItemRenderer {
    private static Map<String, Float> sizeFolders = new HashMap<String, Float>();
    private static Map<Pair<Item, String>, ResourceLocation> largeItemTextures;
    private final Item theItem;
    private final ResourceLocation largeTexture;
    private final String folderName;
    private final float largeIconScale;
    private final int largeIconSize;

    private static ResourceLocation createLargeItemTexture(Item item, String folder) {
        return LOTRRenderLargeItem.createLargeItemTexture(item, "", folder);
    }

    private static ResourceLocation createLargeItemTexture(Item item, String extra, String folder) {
        String prefix = "lotr:";
        String itemName = item.getUnlocalizedName();
        itemName = itemName.substring(itemName.indexOf(prefix) + prefix.length());
        String s = prefix + "textures/items/" + folder + "/" + itemName;
        if(!StringUtils.isNullOrEmpty(extra)) {
            s = s + "_" + extra;
        }
        s = s + ".png";
        return new ResourceLocation(s);
    }

    public static ResourceLocation getOrCreateLargeItemTexture(Item item, String folder) {
        return LOTRRenderLargeItem.getOrCreateLargeItemTexture(item, "", folder);
    }

    public static ResourceLocation getOrCreateLargeItemTexture(Item item, String extra, String folder) {
        ResourceLocation texture;
        Pair<Item, String> key;
        if(StringUtils.isNullOrEmpty(extra)) {
            extra = "";
        }
        if((texture = largeItemTextures.get(key = Pair.of(item, extra))) == null) {
            texture = LOTRRenderLargeItem.createLargeItemTexture(item, extra, folder);
            largeItemTextures.put(key, texture);
        }
        return texture;
    }

    public static LOTRRenderLargeItem getLargeIconSize(Item item) {
        for(String folder : sizeFolders.keySet()) {
            float iconScale = sizeFolders.get(folder).floatValue();
            try {
                ResourceLocation resLoc = LOTRRenderLargeItem.createLargeItemTexture(item, folder);
                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(resLoc);
                if(res == null) continue;
                try {
                    int height;
                    InputStream is = res.getInputStream();
                    BufferedImage img = ImageIO.read(ImageIO.createImageInputStream(is));
                    if(img == null) continue;
                    int width = img.getWidth();
                    if(width == (height = img.getHeight())) {
                        return new LOTRRenderLargeItem(item, resLoc, folder, iconScale, width);
                    }
                    System.out.println("LOTR: Large item texture " + item.getUnlocalizedName() + " width =/= height!");
                }
                catch(IOException e) {
                    System.out.println("LOTR: Error loading large item texture");
                    e.printStackTrace();
                }
            }
            catch(IOException resLoc) {
            }
        }
        return null;
    }

    public LOTRRenderLargeItem(Item item, ResourceLocation res, String dir, float f, int i) {
        this.theItem = item;
        this.largeTexture = res;
        this.folderName = dir;
        this.largeIconScale = f;
        this.largeIconSize = i;
    }

    private ResourceLocation getSubtypeTexture(String extra) {
        return LOTRRenderLargeItem.getOrCreateLargeItemTexture(this.theItem, extra, this.folderName);
    }

    private void doTransformations() {
        GL11.glTranslatef(-(this.largeIconScale - 1.0f) / 2.0f, -(this.largeIconScale - 1.0f) / 2.0f, 0.0f);
        GL11.glScalef(this.largeIconScale, this.largeIconScale, 1.0f);
    }

    public void renderLargeItem() {
        this.renderLargeItem(null);
    }

    public void renderLargeItem(String extra) {
        this.doTransformations();
        ResourceLocation res = extra == null ? this.largeTexture : this.getSubtypeTexture(extra);
        Minecraft.getMinecraft().getTextureManager().bindTexture(res);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        Tessellator tessellator = Tessellator.instance;
        ItemRenderer.renderItemIn2D(tessellator, 1.0f, 0.0f, 0.0f, 1.0f, this.largeIconSize, this.largeIconSize, 0.0625f);
    }

    @Override
    public boolean handleRenderType(ItemStack itemstack, IItemRenderer.ItemRenderType type) {
        return type == IItemRenderer.ItemRenderType.EQUIPPED || type == IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @Override
    public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack itemstack, IItemRenderer.ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(IItemRenderer.ItemRenderType type, ItemStack itemstack, Object... data) {
        EntityLivingBase entityliving;
        GL11.glPushMatrix();
        Entity holder = (Entity) data[1];
        boolean isFirstPerson = holder == Minecraft.getMinecraft().thePlayer && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0;
        Item item = itemstack.getItem();
        if(item instanceof LOTRItemSpear && holder instanceof EntityPlayer && ((EntityPlayer) holder).getItemInUse() == itemstack) {
            GL11.glRotatef(260.0f, 0.0f, 0.0f, 1.0f);
            GL11.glTranslatef(-1.0f, 0.0f, 0.0f);
        }
        if(item instanceof LOTRItemPike && holder instanceof EntityLivingBase && (entityliving = (EntityLivingBase) holder).getHeldItem() == itemstack && entityliving.swingProgress <= 0.0f) {
            if(entityliving.isSneaking()) {
                if(isFirstPerson) {
                    GL11.glRotatef(270.0f, 0.0f, 0.0f, 1.0f);
                    GL11.glTranslatef(-1.0f, 0.0f, 0.0f);
                }
                else {
                    GL11.glTranslatef(0.0f, -0.1f, 0.0f);
                    GL11.glRotatef(20.0f, 0.0f, 0.0f, 1.0f);
                }
            }
            else if(!isFirstPerson) {
                GL11.glTranslatef(0.0f, -0.3f, 0.0f);
                GL11.glRotatef(40.0f, 0.0f, 0.0f, 1.0f);
            }
        }
        if(item instanceof LOTRItemLance && holder instanceof EntityLivingBase && (entityliving = (EntityLivingBase) holder).getHeldItem() == itemstack) {
            if(isFirstPerson) {
                GL11.glRotatef(260.0f, 0.0f, 0.0f, 1.0f);
                GL11.glTranslatef(-1.0f, 0.0f, 0.0f);
            }
            else {
                GL11.glTranslatef(0.7f, 0.0f, 0.0f);
                GL11.glRotatef(-30.0f, 0.0f, 0.0f, 1.0f);
                GL11.glTranslatef(-1.0f, 0.0f, 0.0f);
            }
        }
        this.renderLargeItem();
        if(itemstack != null && itemstack.hasEffect(0)) {
            LOTRClientProxy.renderEnchantmentEffect();
        }
        GL11.glPopMatrix();
    }

    static {
        sizeFolders.put("large", Float.valueOf(2.0f));
        sizeFolders.put("large2", Float.valueOf(3.0f));
        largeItemTextures = new HashMap<Pair<Item, String>, ResourceLocation>();
    }
}
