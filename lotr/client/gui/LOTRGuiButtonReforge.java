package lotr.client.gui;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class LOTRGuiButtonReforge extends GuiButton {
    public LOTRGuiButtonReforge(int i, int x, int y) {
        super(i, x, y, 20, 20, "");
    }

    @Override
    public void drawButton(Minecraft mc, int i, int j) {
        if(this.visible) {
            mc.getTextureManager().bindTexture(LOTRGuiAnvil.anvilTexture);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            this.field_146123_n = i >= this.xPosition && j >= this.yPosition && i < this.xPosition + this.width && j < this.yPosition + this.height;
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 176 + (this.field_146123_n ? this.width : 0), 39, this.width, this.height);
            this.mouseDragged(mc, i, j);
        }
    }
}
