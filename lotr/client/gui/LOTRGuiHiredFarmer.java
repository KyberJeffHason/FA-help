package lotr.client.gui;

import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;

public class LOTRGuiHiredFarmer extends LOTRGuiHiredNPC {
    private LOTRGuiButtonOptions buttonGuardMode;
    private LOTRGuiSlider sliderGuardRange;

    public LOTRGuiHiredFarmer(LOTREntityNPC npc) {
        super(npc);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonGuardMode = new LOTRGuiButtonOptions(0, this.guiLeft + this.xSize / 2 - 80, this.guiTop + 70, 160, 20, StatCollector.translateToLocal("lotr.gui.farmer.mode"));
        this.buttonList.add(this.buttonGuardMode);
        this.buttonGuardMode.setState(this.theNPC.hiredNPCInfo.isGuardMode());
        this.sliderGuardRange = new LOTRGuiSlider(1, this.guiLeft + this.xSize / 2 - 80, this.guiTop + 94, 160, 20, StatCollector.translateToLocal("lotr.gui.farmer.range"));
        this.buttonList.add(this.sliderGuardRange);
        this.sliderGuardRange.setMinMaxValues(LOTRHiredNPCInfo.GUARD_RANGE_MIN, LOTRHiredNPCInfo.GUARD_RANGE_MAX);
        this.sliderGuardRange.setSliderValue(this.theNPC.hiredNPCInfo.getGuardRange());
        this.sliderGuardRange.visible = this.theNPC.hiredNPCInfo.isGuardMode();
        this.buttonList.add(new LOTRGuiButtonOptions(2, this.guiLeft + this.xSize / 2 - 80, this.guiTop + 142, 160, 20, StatCollector.translateToLocal("lotr.gui.farmer.openInv")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(button instanceof LOTRGuiSlider) {
            return;
        }
        if(button.enabled) {
            this.sendActionPacket(button.id);
        }
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        super.drawScreen(i, j, f);
        String s = this.theNPC.hiredNPCInfo.getStatusString();
        this.fontRendererObj.drawString(s, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, this.guiTop + 50, 4210752);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.buttonGuardMode.setState(this.theNPC.hiredNPCInfo.isGuardMode());
        this.sliderGuardRange.visible = this.theNPC.hiredNPCInfo.isGuardMode();
        if(this.sliderGuardRange.dragging) {
            int i = this.sliderGuardRange.getSliderValue();
            this.theNPC.hiredNPCInfo.setGuardRange(i);
            this.sendActionPacket(this.sliderGuardRange.id, i);
        }
    }
}
