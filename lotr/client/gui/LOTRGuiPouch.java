package lotr.client.gui;

import org.lwjgl.opengl.GL11;
import lotr.common.inventory.LOTRContainerPouch;
import lotr.common.network.LOTRPacketHandler;
import lotr.common.network.LOTRPacketRenamePouch;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class LOTRGuiPouch extends GuiContainer {
    private static ResourceLocation texture = new ResourceLocation("lotr:gui/pouch.png");
    private LOTRContainerPouch thePouch;
    private GuiTextField theGuiTextField;

    public LOTRGuiPouch(EntityPlayer entityplayer) {
        super(new LOTRContainerPouch(entityplayer));
        this.thePouch = (LOTRContainerPouch) this.inventorySlots;
        this.ySize = 180;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.theGuiTextField = new GuiTextField(this.fontRendererObj, this.guiLeft + this.xSize / 2 - 80, this.guiTop + 7, 160, 20);
        this.theGuiTextField.setText(this.thePouch.getDisplayName());
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        this.fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int rows = this.thePouch.capacity / 9;
        for(int l = 0; l < rows; ++l) {
            this.drawTexturedModalRect(this.guiLeft + 7, this.guiTop + 29 + l * 18, 0, 180, 162, 18);
        }
        GL11.glDisable(2896);
        this.theGuiTextField.drawTextBox();
        GL11.glEnable(2896);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.theGuiTextField.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char c, int i) {
        if(this.theGuiTextField.textboxKeyTyped(c, i)) {
            this.renamePouch();
        }
        else {
            super.keyTyped(c, i);
        }
    }

    @Override
    protected void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        this.theGuiTextField.mouseClicked(i, j, k);
    }

    @Override
    protected boolean checkHotbarKeys(int i) {
        return false;
    }

    private void renamePouch() {
        String name = this.theGuiTextField.getText();
        this.thePouch.renamePouch(name);
        LOTRPacketRenamePouch packet = new LOTRPacketRenamePouch(name);
        LOTRPacketHandler.networkWrapper.sendToServer(packet);
    }
}
