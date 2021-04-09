package lotr.client.render.tileentity;

import java.util.Random;
import org.lwjgl.opengl.GL11;
import lotr.common.entity.LOTRPlateFallingInfo;
import lotr.common.tileentity.LOTRTileEntityPlate;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

public class LOTRRenderPlateFood extends TileEntitySpecialRenderer {
    private Random rand = new Random();

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f) {
        float plateFallOffset;
        LOTRTileEntityPlate plate = (LOTRTileEntityPlate) tileentity;
        ItemStack plateItem = plate.getFoodItem();
        LOTRPlateFallingInfo fallInfo = plate.plateFallInfo;
        float f2 = plateFallOffset = fallInfo == null ? 0.0f : fallInfo.getPlateOffsetY(f);
        if(plateItem != null) {
            GL11.glPushMatrix();
            GL11.glDisable(2884);
            GL11.glEnable(32826);
            GL11.glTranslatef((float) d + 0.5f, (float) d1, (float) d2 + 0.5f);
            this.bindTexture(TextureMap.locationItemsTexture);
            IIcon icon = plateItem.getIconIndex();
            Tessellator tessellator = Tessellator.instance;
            float f4 = icon.getMinU();
            float f1 = icon.getMaxU();
            float f22 = icon.getMinV();
            float f3 = icon.getMaxV();
            int foods = plateItem.stackSize;
            float lowerOffset = 0.125f;
            for(int l = 0; l < foods; ++l) {
                GL11.glPushMatrix();
                float offset = 0.0f;
                if(fallInfo != null) {
                    offset = fallInfo.getFoodOffsetY(l, f);
                }
                offset = Math.max(offset, lowerOffset);
                GL11.glTranslatef(0.0f, offset, 0.0f);
                lowerOffset = offset + 0.03125f;
                this.rand.setSeed(plate.xCoord * 3129871 ^ plate.zCoord * 116129781L ^ plate.yCoord + l * 5930563L);
                float rotation = this.rand.nextFloat() * 360.0f;
                GL11.glRotatef(rotation, 0.0f, 1.0f, 0.0f);
                GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
                GL11.glTranslatef(-0.25f, -0.25f, 0.0f);
                GL11.glScalef(0.5625f, 0.5625f, 0.5625f);
                ItemRenderer.renderItemIn2D(tessellator, f1, f22, f4, f3, icon.getIconWidth(), icon.getIconHeight(), 0.0625f);
                GL11.glPopMatrix();
            }
            GL11.glDisable(32826);
            GL11.glEnable(2884);
            GL11.glPopMatrix();
        }
    }
}
