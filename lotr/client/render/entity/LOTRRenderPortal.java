package lotr.client.render.entity;

import org.lwjgl.opengl.GL11;
import lotr.client.LOTRClientProxy;
import lotr.client.model.LOTRModelPortal;
import lotr.common.entity.item.LOTREntityPortal;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class LOTRRenderPortal extends Render {
    public static ResourceLocation ringTexture = new ResourceLocation("lotr:misc/portal.png");
    public static ResourceLocation writingTexture = new ResourceLocation("lotr:misc/portal_writing.png");
    public static ModelBase ringModel = new LOTRModelPortal(0);
    public static ModelBase writingModelOuter = new LOTRModelPortal(1);
    public static ModelBase writingModelInner = new LOTRModelPortal(1);

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return ringTexture;
    }

    @Override
    public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
        LOTREntityPortal portal = (LOTREntityPortal) entity;
        GL11.glPushMatrix();
        GL11.glDisable(2884);
        GL11.glTranslatef((float) d, (float) d1 + 0.75f, (float) d2);
        GL11.glNormal3f(0.0f, 0.0f, 0.0f);
        GL11.glEnable(32826);
        GL11.glScalef(1.0f, -1.0f, 1.0f);
        float portalScale = portal.getScale();
        if(portalScale < LOTREntityPortal.MAX_SCALE) {
            portalScale += f1;
            GL11.glScalef(portalScale /= LOTREntityPortal.MAX_SCALE, portalScale, portalScale);
        }
        GL11.glRotatef(portal.getPortalRotation(f1), 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(10.0f, 1.0f, 0.0f, 0.0f);
        this.bindTexture(this.getEntityTexture(portal));
        float scale = 0.0625f;
        ringModel.render(null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, scale);
        GL11.glDisable(2896);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        Tessellator.instance.setBrightness(LOTRClientProxy.TESSELLATOR_MAX_BRIGHTNESS);
        this.bindTexture(writingTexture);
        writingModelOuter.render(null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, scale * 1.05f);
        this.bindTexture(writingTexture);
        writingModelInner.render(null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, scale * 0.85f);
        GL11.glEnable(2896);
        GL11.glDisable(32826);
        GL11.glEnable(2884);
        GL11.glPopMatrix();
    }
}
