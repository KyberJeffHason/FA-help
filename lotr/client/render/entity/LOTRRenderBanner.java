package lotr.client.render.entity;

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL11;
import lotr.client.LOTRClientProxy;
import lotr.client.model.LOTRModelBanner;
import lotr.common.LOTRBannerProtection;
import lotr.common.entity.item.LOTREntityBanner;
import lotr.common.item.LOTRItemBanner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;

public class LOTRRenderBanner extends Render {
    private static Map<LOTRItemBanner.BannerType, ResourceLocation> bannerTextures = new HashMap<LOTRItemBanner.BannerType, ResourceLocation>();
    private static ResourceLocation standTexture = new ResourceLocation("lotr:item/banner/stand.png");
    private static LOTRModelBanner model = new LOTRModelBanner();
    private static Frustrum bannerFrustum = new Frustrum();

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return this.getStandTexture(entity);
    }

    public static ResourceLocation getStandTexture(LOTRItemBanner.BannerType type) {
        return standTexture;
    }

    private ResourceLocation getStandTexture(Entity entity) {
        LOTREntityBanner banner = (LOTREntityBanner) entity;
        return LOTRRenderBanner.getStandTexture(banner.getBannerType());
    }

    public static ResourceLocation getBannerTexture(LOTRItemBanner.BannerType type) {
        ResourceLocation r = bannerTextures.get(type);
        if(r == null) {
            r = new ResourceLocation("lotr:item/banner/banner_" + type.bannerName + ".png");
            bannerTextures.put(type, r);
        }
        return r;
    }

    private ResourceLocation getBannerTexture(Entity entity) {
        LOTREntityBanner banner = (LOTREntityBanner) entity;
        return LOTRRenderBanner.getBannerTexture(banner.getBannerType());
    }

    @Override
    public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
        int light;
        int ly;
        int lx;
        LOTREntityBanner banner = (LOTREntityBanner) entity;
        Minecraft mc = Minecraft.getMinecraft();
        boolean debug = mc.gameSettings.showDebugInfo;
        boolean protecting = banner.isProtectingTerritory();
        boolean accessible = protecting && LOTRBannerProtection.forPlayer(mc.thePlayer).protects(banner) == LOTRBannerProtection.ProtectType.NONE;
        boolean renderBox = debug && protecting;
        boolean seeThroughWalls = renderBox && mc.thePlayer.capabilities.isCreativeMode;
        int protectColor = 65280;
        bannerFrustum.setPosition(d + RenderManager.renderPosX, d1 + RenderManager.renderPosY, d2 + RenderManager.renderPosZ);
        if(bannerFrustum.isBoundingBoxInFrustum(banner.boundingBox)) {
            GL11.glPushMatrix();
            GL11.glDisable(2884);
            GL11.glTranslatef((float) d, (float) d1 + 1.5f, (float) d2);
            GL11.glScalef(-1.0f, -1.0f, 1.0f);
            GL11.glRotatef(180.0f - entity.rotationYaw, 0.0f, 1.0f, 0.0f);
            GL11.glTranslatef(0.0f, 0.01f, 0.0f);
            if(seeThroughWalls) {
                GL11.glDisable(2929);
                GL11.glDisable(3553);
                GL11.glDisable(2896);
                light = LOTRClientProxy.TESSELLATOR_MAX_BRIGHTNESS;
                lx = light % 65536;
                ly = light / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lx / 1.0f, ly / 1.0f);
                GL11.glColor4f((protectColor >> 16 & 0xFF) / 255.0f, (protectColor >> 8 & 0xFF) / 255.0f, (protectColor >> 0 & 0xFF) / 255.0f, 1.0f);
            }
            this.bindTexture(this.getStandTexture(entity));
            model.renderStand(0.0625f);
            model.renderPost(0.0625f);
            this.bindTexture(this.getBannerTexture(entity));
            model.renderBanner(0.0625f);
            if(seeThroughWalls) {
                GL11.glEnable(2929);
                GL11.glEnable(3553);
                GL11.glEnable(2896);
            }
            GL11.glEnable(2884);
            GL11.glPopMatrix();
        }
        if(renderBox) {
            GL11.glPushMatrix();
            GL11.glDepthMask(false);
            GL11.glDisable(3553);
            GL11.glDisable(2884);
            GL11.glDisable(3042);
            light = LOTRClientProxy.TESSELLATOR_MAX_BRIGHTNESS;
            lx = light % 65536;
            ly = light / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lx / 1.0f, ly / 1.0f);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDisable(2896);
            float width = entity.width / 2.0f;
            AxisAlignedBB aabb = banner.createProtectionCube().offset(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            RenderGlobal.drawOutlinedBoundingBox(aabb, protectColor);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glEnable(2896);
            GL11.glEnable(3553);
            GL11.glEnable(2884);
            GL11.glDisable(3042);
            GL11.glDepthMask(true);
            GL11.glPopMatrix();
        }
    }
}
