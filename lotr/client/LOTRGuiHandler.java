package lotr.client;

import java.util.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import com.google.common.collect.Lists;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.client.gui.*;
import lotr.common.*;
import lotr.common.entity.npc.LOTREntityNPCRideable;
import lotr.common.inventory.LOTRContainerCoinExchange;
import lotr.common.item.LOTRItemCoin;
import lotr.common.network.LOTRPacketHandler;
import lotr.common.network.LOTRPacketMountOpenInv;
import lotr.common.world.LOTRWorldProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.event.HoverEvent;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

public class LOTRGuiHandler {
    private static RenderItem itemRenderer = new RenderItem();
    public static final Set<Class<? extends Container>> coinCount_excludedContainers = new HashSet<Class<? extends Container>>();
    public static final Set<Class<? extends GuiContainer>> coinCount_excludedGUIs = new HashSet<Class<? extends GuiContainer>>();
    public static final Set<Class<? extends IInventory>> coinCount_excludedInvTypes = new HashSet<Class<? extends IInventory>>();
    private int descScrollIndex = -1;

    public LOTRGuiHandler() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen gui = event.gui;
        if(LOTRConfig.customMainMenu && gui != null && gui.getClass() == GuiMainMenu.class) {
            event.gui = gui = new LOTRGuiMainMenu();
        }
        if(gui != null && gui.getClass() == GuiDownloadTerrain.class) {
            Minecraft mc = Minecraft.getMinecraft();
            WorldProvider provider = mc.theWorld.provider;
            if(provider instanceof LOTRWorldProvider) {
                event.gui = gui = new LOTRGuiDownloadTerrain(mc.thePlayer.sendQueue);
            }
        }
    }

    @SubscribeEvent
    public void preInitGui(GuiScreenEvent.InitGuiEvent.Pre event) {
        LOTREntityNPCRideable mount;
        GuiScreen gui = event.gui;
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP entityplayer = mc.thePlayer;
        WorldClient world = mc.theWorld;
        if((gui instanceof GuiInventory || gui instanceof GuiContainerCreative) && entityplayer != null && world != null && entityplayer.ridingEntity instanceof LOTREntityNPCRideable && (mount = (LOTREntityNPCRideable) entityplayer.ridingEntity).getMountInventory() != null) {
            entityplayer.closeScreen();
            LOTRPacketMountOpenInv packet = new LOTRPacketMountOpenInv();
            LOTRPacketHandler.networkWrapper.sendToServer(packet);
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public void postInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiButton buttonDifficulty;
        GuiScreen gui = event.gui;
        List buttons = event.buttonList;
        if(gui instanceof GuiOptions && (buttonDifficulty = this.getDifficultyButton((GuiOptions) gui, buttons)) != null) {
            LOTRGuiButtonLock lock = new LOTRGuiButtonLock(1000000, buttonDifficulty.xPosition + buttonDifficulty.width + 4, buttonDifficulty.yPosition);
            lock.enabled = !LOTRLevelData.isDifficultyLocked();
            buttons.add(lock);
            buttonDifficulty.enabled = !LOTRLevelData.isDifficultyLocked();
        }
    }

    @SubscribeEvent
    public void postActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiScreen gui = event.gui;
        List buttons = event.buttonList;
        GuiButton button = event.button;
        if(gui instanceof GuiOptions && button instanceof LOTRGuiButtonLock && button.enabled && mc.isSingleplayer()) {
            LOTRLevelData.setSavedDifficulty(mc.gameSettings.difficulty);
            LOTRLevelData.setDifficultyLocked(true);
            button.enabled = false;
            GuiButton buttonDifficulty = this.getDifficultyButton((GuiOptions) gui, buttons);
            if(buttonDifficulty != null) {
                buttonDifficulty.enabled = false;
            }
        }
    }

    private GuiButton getDifficultyButton(GuiOptions gui, List buttons) {
        for(Object obj : buttons) {
            GuiOptionButton button;
            if(!(obj instanceof GuiOptionButton) || (button = (GuiOptionButton) obj).returnEnumOptions() != GameSettings.Options.DIFFICULTY) continue;
            return button;
        }
        return null;
    }

    @SubscribeEvent
    public void preDrawScreen(GuiScreenEvent.DrawScreenEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiScreen gui = event.gui;
        int mouseX = event.mouseX;
        int mouseY = event.mouseY;
        if(gui instanceof GuiModList) {
            ModContainer mod = LOTRMod.getModContainer();
            ModMetadata meta = mod.getMetadata();
            if(this.descScrollIndex == -1) {
                meta.description = LOTRModInfo.concatenateDescription(0);
                this.descScrollIndex = 0;
            }
            while(Mouse.next()) {
                int dwheel = Mouse.getEventDWheel();
                if(dwheel == 0) continue;
                int scroll = Integer.signum(dwheel);
                this.descScrollIndex -= scroll;
                this.descScrollIndex = MathHelper.clamp_int(this.descScrollIndex, 0, LOTRModInfo.description.length - 1);
                meta.description = LOTRModInfo.concatenateDescription(this.descScrollIndex);
            }
        }
        if(gui instanceof GuiContainer && LOTRConfig.displayCoinCounts) {
            mc.theWorld.theProfiler.startSection("invCoinCount");
            GuiContainer guiContainer = (GuiContainer) gui;
            Container container = guiContainer.inventorySlots;
            if(!coinCount_excludedContainers.contains(container.getClass()) && !coinCount_excludedGUIs.contains(guiContainer.getClass())) {
                int guiLeft = -1;
                int guiTop = -1;
                int guiXSize = -1;
                ArrayList<IInventory> differentInvs = new ArrayList<IInventory>();
                HashMap<IInventory, Integer> invHighestY = new HashMap<IInventory, Integer>();
                for(int i = 0; i < container.inventorySlots.size(); ++i) {
                    Slot slot = container.getSlot(i);
                    IInventory inv = slot.inventory;
                    if(inv == null || coinCount_excludedInvTypes.contains(inv.getClass())) continue;
                    if(!differentInvs.contains(inv)) {
                        differentInvs.add(inv);
                    }
                    int slotY = slot.yDisplayPosition;
                    if(!invHighestY.containsKey(inv)) {
                        invHighestY.put(inv, slotY);
                        continue;
                    }
                    int highestY = invHighestY.get(inv);
                    if(slotY >= highestY) continue;
                    invHighestY.put(inv, slotY);
                }
                for(IInventory inv : differentInvs) {
                    int coins = LOTRItemCoin.getContainerValue(inv);
                    if(coins <= 0) continue;
                    if(guiLeft == -1) {
                        guiLeft = LOTRReflectionClient.getGuiLeft(guiContainer);
                        guiTop = LOTRReflectionClient.getGuiTop(guiContainer);
                        guiXSize = LOTRReflectionClient.getGuiXSize(guiContainer);
                    }
                    int x = gui.width / 2 + guiXSize / 2 + 8;
                    int y = invHighestY.get(inv) + guiTop;
                    String sCoins = String.valueOf(coins);
                    int border = 2;
                    int rectX0 = x - border;
                    int rectX1 = x + 16 + 2 + mc.fontRenderer.getStringWidth(sCoins) + border + 1;
                    int rectY0 = y - border;
                    int rectY1 = y + 16 + border;
                    float a0 = 1.0f;
                    float a1 = 0.1f;
                    GL11.glDisable(3553);
                    GL11.glDisable(3008);
                    GL11.glShadeModel(7425);
                    GL11.glPushMatrix();
                    GL11.glTranslatef(0.0f, 0.0f, -500.0f);
                    Tessellator tessellator = Tessellator.instance;
                    tessellator.startDrawingQuads();
                    tessellator.setColorRGBA_F(0.0f, 0.0f, 0.0f, a1);
                    tessellator.addVertex(rectX1, rectY0, 0.0);
                    tessellator.setColorRGBA_F(0.0f, 0.0f, 0.0f, a0);
                    tessellator.addVertex(rectX0, rectY0, 0.0);
                    tessellator.addVertex(rectX0, rectY1, 0.0);
                    tessellator.setColorRGBA_F(0.0f, 0.0f, 0.0f, a1);
                    tessellator.addVertex(rectX1, rectY1, 0.0);
                    tessellator.draw();
                    GL11.glPopMatrix();
                    GL11.glShadeModel(7424);
                    GL11.glEnable(3008);
                    GL11.glEnable(3553);
                    GL11.glPushMatrix();
                    GL11.glTranslatef(0.0f, 0.0f, 500.0f);
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                    itemRenderer.renderItemIntoGUI(mc.fontRenderer, mc.getTextureManager(), new ItemStack(LOTRMod.silverCoin), x, y);
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                    GL11.glDisable(2896);
                    mc.fontRenderer.drawString(sCoins, x + 16 + 2, y + (16 - mc.fontRenderer.FONT_HEIGHT + 2) / 2, 16777215);
                    GL11.glPopMatrix();
                    GL11.glDisable(2896);
                    GL11.glEnable(3008);
                    GL11.glEnable(3042);
                    GL11.glDisable(2884);
                }
                mc.theWorld.theProfiler.endSection();
            }
        }
    }

    @SubscribeEvent
    public void postDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        HoverEvent hoverevent;
        IChatComponent component;
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP entityplayer = mc.thePlayer;
        GuiScreen gui = event.gui;
        int mouseX = event.mouseX;
        int mouseY = event.mouseY;
        if(gui instanceof GuiChat && (component = mc.ingameGUI.getChatGUI().func_146236_a(Mouse.getX(), Mouse.getY())) != null && component.getChatStyle().getChatHoverEvent() != null && (hoverevent = component.getChatStyle().getChatHoverEvent()).getAction() == LOTRChatEvents.SHOW_LOTR_ACHIEVEMENT) {
            LOTRGuiAchievementHoverEvent proxyGui = new LOTRGuiAchievementHoverEvent();
            proxyGui.setWorldAndResolution(mc, gui.width, gui.height);
            try {
                String unformattedText = hoverevent.getValue().getUnformattedText();
                int splitIndex = unformattedText.indexOf("$");
                String categoryName = unformattedText.substring(0, splitIndex);
                LOTRAchievement.Category category = LOTRAchievement.categoryForName(categoryName);
                int achievementID = Integer.parseInt(unformattedText.substring(splitIndex + 1));
                LOTRAchievement achievement = LOTRAchievement.achievementForCategoryAndID(category, achievementID);
                ChatComponentTranslation name = new ChatComponentTranslation("lotr.gui.achievements.hover.name", achievement.getAchievementChatComponent(entityplayer));
                ChatComponentTranslation subtitle = new ChatComponentTranslation("lotr.gui.achievements.hover.subtitle", achievement.getDimension().getDimensionName(), category.getDisplayName());
                subtitle.getChatStyle().setItalic(true);
                String desc = achievement.getDescription(entityplayer);
                ArrayList<String> list = Lists.newArrayList(name.getFormattedText(), subtitle.getFormattedText());
                list.addAll(mc.fontRenderer.listFormattedStringToWidth(desc, 150));
                proxyGui.func_146283_a(list, mouseX, mouseY);
            }
            catch(Exception e) {
                proxyGui.drawCreativeTabHoveringText((EnumChatFormatting.RED) + "Invalid LOTRAchievement!", mouseX, mouseY);
            }
        }
    }

    static {
        coinCount_excludedGUIs.add(GuiContainerCreative.class);
        coinCount_excludedInvTypes.add(LOTRContainerCoinExchange.InventoryCoinExchangeSlot.class);
        coinCount_excludedInvTypes.add(InventoryCraftResult.class);
    }
}
