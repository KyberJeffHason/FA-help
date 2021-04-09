package lotr.client.gui;

import org.lwjgl.opengl.GL11;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRUnitTradeEntry;
import lotr.common.fac.LOTRAlignmentValues;
import lotr.common.fac.LOTRFaction;
import lotr.common.network.LOTRPacketHandler;
import lotr.common.network.LOTRPacketHiredUnitCommand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public abstract class LOTRGuiHiredNPC extends LOTRGuiScreenBase {
    private static ResourceLocation guiTexture = new ResourceLocation("lotr:gui/npc/hired.png");
    public int xSize = 200;
    public int ySize = 220;
    public int guiLeft;
    public int guiTop;
    public LOTREntityNPC theNPC;
    public int page;

    public LOTRGuiHiredNPC(LOTREntityNPC npc) {
        this.theNPC = npc;
    }

    @Override
    public void initGui() {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.getTextureManager().bindTexture(guiTexture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        String s = this.theNPC.getNPCName();
        this.fontRendererObj.drawString(s, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, this.guiTop + 11, 3618615);
        s = this.theNPC.getEntityClassName();
        this.fontRendererObj.drawString(s, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, this.guiTop + 26, 3618615);
        if(this.page == 0) {
            int x = this.guiLeft + 6;
            int y = this.guiTop + 186;
            s = StatCollector.translateToLocal("lotr.hiredNPC.commandReq");
            this.fontRendererObj.drawString(s, x, y, 3618615);
            LOTRFaction fac = this.theNPC.getHiringFaction();
            s = LOTRAlignmentValues.formatAlignForDisplay(this.theNPC.hiredNPCInfo.alignmentRequiredToCommand);
            s = StatCollector.translateToLocalFormatted("lotr.hiredNPC.commandReq.align", s, fac.factionName());
            this.fontRendererObj.drawString(s, x += 4, y += this.fontRendererObj.FONT_HEIGHT, 3618615);
            y += this.fontRendererObj.FONT_HEIGHT;
            LOTRUnitTradeEntry.PledgeType pledge = this.theNPC.hiredNPCInfo.pledgeType;
            String pledgeReq = pledge.getCommandReqText(fac);
            if(pledgeReq != null) {
                this.fontRendererObj.drawString(pledgeReq, x, y, 3618615);
                y += this.fontRendererObj.FONT_HEIGHT;
            }
        }
        super.drawScreen(i, j, f);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if(!this.theNPC.isEntityAlive() || this.theNPC.hiredNPCInfo.getHiringPlayer() != this.mc.thePlayer || this.theNPC.getDistanceSqToEntity(this.mc.thePlayer) > 64.0) {
            this.mc.thePlayer.closeScreen();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.sendActionPacket(-1);
    }

    public void sendActionPacket(int action) {
        this.sendActionPacket(action, 0);
    }

    public void sendActionPacket(int action, int value) {
        LOTRPacketHiredUnitCommand packet = new LOTRPacketHiredUnitCommand(this.theNPC.getEntityId(), this.page, action, value);
        LOTRPacketHandler.networkWrapper.sendToServer(packet);
    }
}
