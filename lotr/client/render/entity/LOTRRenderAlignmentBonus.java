package lotr.client.render.entity;

import org.lwjgl.opengl.GL11;
import lotr.client.LOTRClientProxy;
import lotr.client.LOTRTickHandlerClient;
import lotr.client.fx.LOTREntityAlignmentBonus;
import lotr.common.LOTRLevelData;
import lotr.common.LOTRPlayerData;
import lotr.common.fac.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class LOTRRenderAlignmentBonus extends Render {
    public LOTRRenderAlignmentBonus() {
        this.shadowSize = 0.0f;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return LOTRClientProxy.alignmentTexture;
    }

    @Override
    public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
        float renderBonus;
        EntityClientPlayerMP entityplayer = Minecraft.getMinecraft().thePlayer;
        LOTRPlayerData playerData = LOTRLevelData.getData(entityplayer);
        LOTRFaction viewingFaction = playerData.getViewingFaction();
        LOTREntityAlignmentBonus alignmentBonus = (LOTREntityAlignmentBonus) entity;
        LOTRFaction mainFaction = alignmentBonus.mainFaction;
        LOTRAlignmentBonusMap factionBonusMap = alignmentBonus.factionBonusMap;
        LOTRFaction renderFaction = null;
        boolean showConquest = false;
        if(alignmentBonus.conquestBonus > 0.0f && playerData.isPledgedTo(viewingFaction)) {
            renderFaction = viewingFaction;
            showConquest = true;
        }
        else if(alignmentBonus.conquestBonus < 0.0f && (viewingFaction == mainFaction || playerData.isPledgedTo(viewingFaction))) {
            renderFaction = viewingFaction;
            showConquest = true;
        }
        else if(!factionBonusMap.isEmpty()) {
            if(factionBonusMap.containsKey(viewingFaction)) {
                renderFaction = viewingFaction;
            }
            else if(factionBonusMap.size() == 1 && mainFaction.isPlayableAlignmentFaction()) {
                renderFaction = mainFaction;
            }
            else if(mainFaction.isPlayableAlignmentFaction() && alignmentBonus.prevMainAlignment >= 0.0f && factionBonusMap.get(mainFaction).floatValue() < 0.0f) {
                renderFaction = mainFaction;
            }
            else {
                float bonus;
                float alignment;
                for(LOTRFaction faction : factionBonusMap.keySet()) {
                    if(!faction.isPlayableAlignmentFaction() || !((bonus = factionBonusMap.get(faction).floatValue()) > 0.0f)) continue;
                    alignment = playerData.getAlignment(faction);
                    if(renderFaction != null && !(alignment > playerData.getAlignment(renderFaction))) continue;
                    renderFaction = faction;
                }
                if(renderFaction == null) {
                    if(mainFaction.isPlayableAlignmentFaction() && factionBonusMap.get(mainFaction).floatValue() < 0.0f) {
                        renderFaction = mainFaction;
                    }
                    else {
                        for(LOTRFaction faction : factionBonusMap.keySet()) {
                            if(!faction.isPlayableAlignmentFaction() || !((bonus = factionBonusMap.get(faction).floatValue()) < 0.0f)) continue;
                            alignment = playerData.getAlignment(faction);
                            if(renderFaction != null && !(alignment > playerData.getAlignment(renderFaction))) continue;
                            renderFaction = faction;
                        }
                    }
                }
            }
        }
        float f2 = renderBonus = factionBonusMap.containsKey(renderFaction) ? factionBonusMap.get(renderFaction).floatValue() : 0.0f;
        if(renderFaction != null && (renderBonus != 0.0f || showConquest)) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) d, (float) d1, (float) d2);
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GL11.glRotatef(-this.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
            GL11.glRotatef(this.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
            GL11.glScalef(-0.025f, -0.025f, 0.025f);
            GL11.glDisable(2896);
            GL11.glDepthMask(false);
            GL11.glDisable(2929);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            int age = alignmentBonus.particleAge;
            float alpha = age < 60 ? 1.0f : (80 - age) / 20.0f;
            this.renderBonusText(alignmentBonus, playerData, viewingFaction, renderFaction, !factionBonusMap.isEmpty(), renderBonus, showConquest, alpha);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDisable(3042);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glEnable(2896);
            GL11.glPopMatrix();
        }
    }

    private void renderBonusText(LOTREntityAlignmentBonus alignmentBonus, LOTRPlayerData playerData, LOTRFaction viewingFaction, LOTRFaction renderFaction, boolean showAlign, float align, boolean showConquest, float alpha) {
        boolean isViewingFaction;
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;
        String strAlign = LOTRAlignmentValues.formatAlignForDisplay(align);
        String name = alignmentBonus.name;
        float conq = alignmentBonus.conquestBonus;
        GL11.glPushMatrix();
        boolean bl = isViewingFaction = renderFaction == viewingFaction;
        if(!isViewingFaction) {
            float scale = 0.5f;
            GL11.glScalef(scale, scale, 1.0f);
            strAlign = strAlign + " (" + renderFaction.factionName() + "...)";
        }
        int x = -MathHelper.floor_double((fr.getStringWidth(strAlign) + 18) / 2.0);
        int y = -12;
        if(showAlign) {
            this.bindEntityTexture(alignmentBonus);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
            LOTRTickHandlerClient.drawTexturedModalRect(x, y - 5, 0, 36, 16, 16);
            LOTRTickHandlerClient.drawAlignmentText(fr, x + 18, y, strAlign, alpha);
            LOTRTickHandlerClient.drawAlignmentText(fr, -MathHelper.floor_double(fr.getStringWidth(name) / 2.0), y += 14, name, alpha);
        }
        if(showConquest && conq != 0.0f) {
            boolean negative = conq < 0.0f;
            String strConq = LOTRAlignmentValues.formatConqForDisplay(conq, true);
            x = -MathHelper.floor_double((fr.getStringWidth(strConq) + 18) / 2.0);
            if(showAlign) {
                y += 16;
            }
            this.bindEntityTexture(alignmentBonus);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
            LOTRTickHandlerClient.drawTexturedModalRect(x, y - 5, negative ? 16 : 0, 228, 16, 16);
            LOTRTickHandlerClient.drawConquestText(fr, x + 18, y, strConq, negative, alpha);
        }
        GL11.glPopMatrix();
    }
}
