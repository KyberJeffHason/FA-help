package lotr.client.gui;

import org.lwjgl.opengl.GL11;
import lotr.common.entity.npc.*;
import lotr.common.inventory.LOTRContainerTrade;
import lotr.common.inventory.LOTRSlotTrade;
import lotr.common.network.LOTRPacketHandler;
import lotr.common.network.LOTRPacketSell;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class LOTRGuiTrade extends GuiContainer {
    public static final ResourceLocation guiTexture = new ResourceLocation("lotr:gui/npc/trade.png");
    private static int lockedTradeColor = -1610612736;
    public LOTREntityNPC theEntity;
    private LOTRContainerTrade containerTrade;
    private GuiButton buttonSell;
    private static int sellQueryX;
    private static int sellQueryY;
    private static int sellQueryWidth;

    public LOTRGuiTrade(InventoryPlayer inv, LOTRTradeable trader, World world) {
        super(new LOTRContainerTrade(inv, trader, world));
        this.containerTrade = (LOTRContainerTrade) this.inventorySlots;
        this.theEntity = (LOTREntityNPC) (trader);
        this.ySize = 270;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonSell = new LOTRGuiTradeButton(0, this.guiLeft + 79, this.guiTop + 164);
        this.buttonSell.enabled = false;
        this.buttonList.add(this.buttonSell);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        int cost;
        LOTRTradeEntry trade;
        int y;
        int l;
        int x;
        this.drawCenteredString(this.theEntity.getNPCName(), 89, 11, 4210752);
        this.fontRendererObj.drawString(StatCollector.translateToLocal("container.lotr.trade.buy"), 8, 28, 4210752);
        this.fontRendererObj.drawString(StatCollector.translateToLocal("container.lotr.trade.sell"), 8, 79, 4210752);
        this.fontRendererObj.drawString(StatCollector.translateToLocal("container.lotr.trade.sellOffer"), 8, 129, 4210752);
        this.fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, 176, 4210752);
        for(l = 0; l < this.containerTrade.tradeInvBuy.getSizeInventory(); ++l) {
            LOTRSlotTrade slotBuy = (LOTRSlotTrade) this.containerTrade.getSlotFromInventory(this.containerTrade.tradeInvBuy, l);
            trade = slotBuy.getTrade();
            if(trade == null) continue;
            if(trade.isAvailable()) {
                cost = slotBuy.cost();
                if(cost <= 0) continue;
                this.renderCost(Integer.toString(cost), slotBuy.xDisplayPosition + 8, slotBuy.yDisplayPosition + 22);
                continue;
            }
            GL11.glTranslatef(0.0f, 0.0f, 200.0f);
            x = slotBuy.xDisplayPosition;
            y = slotBuy.yDisplayPosition;
            Gui.drawRect(x, y, x + 16, y + 16, lockedTradeColor);
            GL11.glTranslatef(0.0f, 0.0f, -200.0f);
            this.drawCenteredString(StatCollector.translateToLocal("container.lotr.trade.locked"), slotBuy.xDisplayPosition + 8, slotBuy.yDisplayPosition + 22, 4210752);
        }
        for(l = 0; l < this.containerTrade.tradeInvSell.getSizeInventory(); ++l) {
            LOTRSlotTrade slotSell = (LOTRSlotTrade) this.containerTrade.getSlotFromInventory(this.containerTrade.tradeInvSell, l);
            trade = slotSell.getTrade();
            if(trade == null) continue;
            if(trade.isAvailable()) {
                cost = slotSell.cost();
                if(cost <= 0) continue;
                this.renderCost(Integer.toString(cost), slotSell.xDisplayPosition + 8, slotSell.yDisplayPosition + 22);
                continue;
            }
            GL11.glTranslatef(0.0f, 0.0f, 200.0f);
            x = slotSell.xDisplayPosition;
            y = slotSell.yDisplayPosition;
            Gui.drawRect(x, y, x + 16, y + 16, lockedTradeColor);
            GL11.glTranslatef(0.0f, 0.0f, -200.0f);
            this.drawCenteredString(StatCollector.translateToLocal("container.lotr.trade.locked"), slotSell.xDisplayPosition + 8, slotSell.yDisplayPosition + 22, 4210752);
        }
        int totalSellPrice = 0;
        for(int l2 = 0; l2 < this.containerTrade.tradeInvSellOffer.getSizeInventory(); ++l2) {
            LOTRTradeSellResult sellResult;
            Slot slotSell = this.containerTrade.getSlotFromInventory(this.containerTrade.tradeInvSellOffer, l2);
            ItemStack item = slotSell.getStack();
            if(item == null || (sellResult = LOTRTradeEntries.getItemSellResult(item, this.theEntity)) == null) continue;
            totalSellPrice += sellResult.totalSellValue;
        }
        if(totalSellPrice > 0) {
            this.fontRendererObj.drawString(StatCollector.translateToLocalFormatted("container.lotr.trade.sellPrice", totalSellPrice), 100, 169, 4210752);
        }
        this.buttonSell.enabled = totalSellPrice > 0;
    }

    private void renderCost(String s, int x, int y) {
        boolean halfSize;
        int l = this.fontRendererObj.getStringWidth(s);
        boolean bl = halfSize = l > 15;
        if(halfSize) {
            GL11.glPushMatrix();
            GL11.glScalef(0.5f, 0.5f, 1.0f);
            x *= 2;
            y *= 2;
            y += this.fontRendererObj.FONT_HEIGHT / 2;
        }
        this.drawCenteredString(s, x, y, 4210752);
        if(halfSize) {
            GL11.glPopMatrix();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.getTextureManager().bindTexture(guiTexture);
        Gui.func_146110_a(this.guiLeft, this.guiTop, 0.0f, 0.0f, this.xSize, this.ySize, 512.0f, 512.0f);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(button.enabled && button == this.buttonSell) {
            LOTRPacketSell packet = new LOTRPacketSell();
            LOTRPacketHandler.networkWrapper.sendToServer(packet);
        }
    }

    private void drawCenteredString(String s, int i, int j, int k) {
        this.fontRendererObj.drawString(s, i - this.fontRendererObj.getStringWidth(s) / 2, j, k);
    }

    static {
        sellQueryWidth = 12;
    }
}
