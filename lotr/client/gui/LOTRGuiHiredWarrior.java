package lotr.client.gui;

import lotr.common.LOTRSquadrons;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo;
import lotr.common.network.LOTRPacketHandler;
import lotr.common.network.LOTRPacketNPCSquadron;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;

public class LOTRGuiHiredWarrior extends LOTRGuiHiredNPC {
    private static String[] pageTitles = new String[] {"overview", "options"};
    private GuiButton buttonLeft;
    private GuiButton buttonRight;
    private LOTRGuiButtonOptions buttonOpenInv;
    private LOTRGuiButtonOptions buttonTeleport;
    private LOTRGuiButtonOptions buttonGuardMode;
    private LOTRGuiSlider sliderGuardRange;
    private GuiTextField squadronNameField;
    private boolean updatePage;
    private boolean sendSquadronUpdate = false;

    public LOTRGuiHiredWarrior(LOTREntityNPC npc) {
        super(npc);
    }

    @Override
    public void initGui() {
        super.initGui();
        if(this.page == 0) {
            this.buttonOpenInv = new LOTRGuiButtonOptions(0, this.guiLeft + this.xSize / 2 - 80, this.guiTop + 142, 160, 20, StatCollector.translateToLocal("lotr.gui.warrior.openInv"));
            this.buttonList.add(this.buttonOpenInv);
        }
        if(this.page == 1) {
            this.buttonTeleport = new LOTRGuiButtonOptions(0, this.guiLeft + this.xSize / 2 - 80, this.guiTop + 180, 160, 20, StatCollector.translateToLocal("lotr.gui.warrior.teleport"));
            this.buttonList.add(this.buttonTeleport);
            this.buttonGuardMode = new LOTRGuiButtonOptions(1, this.guiLeft + this.xSize / 2 - 80, this.guiTop + 50, 160, 20, StatCollector.translateToLocal("lotr.gui.warrior.guardMode"));
            this.buttonList.add(this.buttonGuardMode);
            this.sliderGuardRange = new LOTRGuiSlider(2, this.guiLeft + this.xSize / 2 - 80, this.guiTop + 74, 160, 20, StatCollector.translateToLocal("lotr.gui.warrior.guardRange"));
            this.buttonList.add(this.sliderGuardRange);
            this.sliderGuardRange.setMinMaxValues(LOTRHiredNPCInfo.GUARD_RANGE_MIN, LOTRHiredNPCInfo.GUARD_RANGE_MAX);
            this.sliderGuardRange.setSliderValue(this.theNPC.hiredNPCInfo.getGuardRange());
            this.squadronNameField = new GuiTextField(this.fontRendererObj, this.guiLeft + this.xSize / 2 - 80, this.guiTop + 130, 160, 20);
            this.squadronNameField.setMaxStringLength(LOTRSquadrons.SQUADRON_LENGTH_MAX);
            String squadron = this.theNPC.hiredNPCInfo.getSquadron();
            if(!StringUtils.isNullOrEmpty(squadron)) {
                this.squadronNameField.setText(squadron);
            }
        }
        this.buttonLeft = new LOTRGuiButtonLeftRight(1000, true, this.guiLeft - 160, this.guiTop + 50, "");
        this.buttonRight = new LOTRGuiButtonLeftRight(1001, false, this.guiLeft + this.xSize + 40, this.guiTop + 50, "");
        this.buttonList.add(this.buttonLeft);
        this.buttonList.add(this.buttonRight);
        this.buttonLeft.displayString = this.page == 0 ? pageTitles[pageTitles.length - 1] : pageTitles[this.page - 1];
        this.buttonRight.displayString = this.page == pageTitles.length - 1 ? pageTitles[0] : pageTitles[this.page + 1];
        this.buttonLeft.displayString = StatCollector.translateToLocal("lotr.gui.warrior." + this.buttonLeft.displayString);
        this.buttonRight.displayString = StatCollector.translateToLocal("lotr.gui.warrior." + this.buttonRight.displayString);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(button instanceof LOTRGuiSlider) {
            return;
        }
        if(button.enabled) {
            if(button instanceof LOTRGuiButtonLeftRight) {
                if(button == this.buttonLeft) {
                    --this.page;
                    if(this.page < 0) {
                        this.page = pageTitles.length - 1;
                    }
                }
                else if(button == this.buttonRight) {
                    ++this.page;
                    if(this.page >= pageTitles.length) {
                        this.page = 0;
                    }
                }
                this.buttonList.clear();
                this.updatePage = true;
            }
            else {
                this.sendActionPacket(button.id);
            }
        }
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        String s;
        super.drawScreen(i, j, f);
        if(this.page == 0) {
            s = StatCollector.translateToLocalFormatted("lotr.gui.warrior.health", Math.round(this.theNPC.getHealth()), Math.round(this.theNPC.getMaxHealth()));
            this.fontRendererObj.drawString(s, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, this.guiTop + 50, 4210752);
            s = this.theNPC.hiredNPCInfo.getStatusString();
            this.fontRendererObj.drawString(s, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, this.guiTop + 62, 4210752);
            s = StatCollector.translateToLocalFormatted("lotr.gui.warrior.kills", this.theNPC.hiredNPCInfo.mobKills);
            this.fontRendererObj.drawString(s, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, this.guiTop + 74, 4210752);
        }
        if(this.page == 1) {
            s = StatCollector.translateToLocal("lotr.gui.warrior.squadron");
            this.fontRendererObj.drawString(s, this.squadronNameField.xPosition, this.squadronNameField.yPosition - this.fontRendererObj.FONT_HEIGHT - 3, 4210752);
            this.squadronNameField.drawTextBox();
        }
    }

    @Override
    public void updateScreen() {
        if(this.updatePage) {
            this.initGui();
            this.updatePage = false;
        }
        super.updateScreen();
        if(this.page == 1) {
            this.buttonTeleport.setState(this.theNPC.hiredNPCInfo.teleportAutomatically);
            this.buttonTeleport.enabled = !this.theNPC.hiredNPCInfo.isGuardMode();
            this.buttonGuardMode.setState(this.theNPC.hiredNPCInfo.isGuardMode());
            this.sliderGuardRange.visible = this.theNPC.hiredNPCInfo.isGuardMode();
            if(this.sliderGuardRange.dragging) {
                int i = this.sliderGuardRange.getSliderValue();
                this.theNPC.hiredNPCInfo.setGuardRange(i);
                this.sendActionPacket(this.sliderGuardRange.id, i);
            }
            this.squadronNameField.updateCursorCounter();
        }
    }

    @Override
    protected void keyTyped(char c, int i) {
        if(this.page == 1 && this.squadronNameField != null && this.squadronNameField.getVisible() && this.squadronNameField.textboxKeyTyped(c, i)) {
            this.theNPC.hiredNPCInfo.setSquadron(this.squadronNameField.getText());
            this.sendSquadronUpdate = true;
            return;
        }
        super.keyTyped(c, i);
    }

    @Override
    protected void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        if(this.page == 1 && this.squadronNameField != null) {
            this.squadronNameField.mouseClicked(i, j, k);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if(this.sendSquadronUpdate) {
            String squadron = this.theNPC.hiredNPCInfo.getSquadron();
            LOTRPacketNPCSquadron packet = new LOTRPacketNPCSquadron(this.theNPC, squadron);
            LOTRPacketHandler.networkWrapper.sendToServer(packet);
        }
    }
}
