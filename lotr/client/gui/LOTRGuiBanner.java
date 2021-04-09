package lotr.client.gui;

import java.util.Arrays;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import com.mojang.authlib.GameProfile;
import lotr.common.entity.item.LOTREntityBanner;
import lotr.common.fac.LOTRAlignmentValues;
import lotr.common.fellowship.LOTRFellowshipProfile;
import lotr.common.network.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.*;

public class LOTRGuiBanner extends LOTRGuiScreenBase {
    private static ResourceLocation guiTexture = new ResourceLocation("lotr:gui/banner_edit.png");
    public final LOTREntityBanner theBanner;
    public int xSize = 200;
    public int ySize = 250;
    private int guiLeft;
    private int guiTop;
    private boolean firstInit = true;
    private GuiButton modeButton;
    private LOTRGuiButtonOptions selfProtectionButton;
    private GuiButton buttonAddSlot;
    private GuiButton buttonRemoveSlot;
    private GuiTextField alignmentField;
    private static final int displayedPlayers = 5;
    private GuiTextField[] allowedPlayers = new GuiTextField[0];
    private boolean[] invalidUsernames = new boolean[0];
    private boolean[] checkUsernames = new boolean[0];
    private float currentScroll = 0.0f;
    private boolean isScrolling = false;
    private boolean wasMouseDown;
    private int scrollBarWidth = 14;
    private int scrollBarHeight = 120;
    private int scrollBarX = 180;
    private int scrollBarY = 68;
    private int scrollBarBorder = 1;
    private int scrollWidgetWidth = 12;
    private int scrollWidgetHeight = 17;

    public LOTRGuiBanner(LOTREntityBanner banner) {
        this.theBanner = banner;
    }

    @Override
    public void initGui() {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        this.modeButton = new GuiButton(0, this.guiLeft + this.xSize / 2 - 80, this.guiTop + 20, 160, 20, "");
        this.buttonList.add(this.modeButton);
        this.selfProtectionButton = new LOTRGuiButtonOptions(1, this.guiLeft + this.xSize / 2 - 80, this.guiTop + 220, 160, 20, StatCollector.translateToLocal("lotr.gui.bannerEdit.selfProtection"));
        this.buttonList.add(this.selfProtectionButton);
        this.buttonAddSlot = new LOTRGuiBannerButton(0, this.guiLeft + 179, this.guiTop + 190);
        this.buttonList.add(this.buttonAddSlot);
        this.buttonRemoveSlot = new LOTRGuiBannerButton(1, this.guiLeft + 187, this.guiTop + 190);
        this.buttonList.add(this.buttonRemoveSlot);
        this.alignmentField = new GuiTextField(this.fontRendererObj, this.guiLeft + this.xSize / 2 - 70, this.guiTop + 100, 140, 20);
        this.alignmentField.setText(String.valueOf(this.theBanner.getAlignmentProtection()));
        this.alignmentField.setEnabled(false);
        this.refreshWhitelist();
        for(int i = 0; i < this.allowedPlayers.length; ++i) {
            String name;
            GuiTextField textBox = this.allowedPlayers[i];
            GameProfile profile = this.theBanner.getWhitelistedPlayer(i);
            if(profile != null && !StringUtils.isNullOrEmpty(name = profile.getName())) {
                textBox.setText(name);
            }
            this.allowedPlayers[i] = textBox;
        }
        this.allowedPlayers[0].setEnabled(false);
        if(this.firstInit) {
            this.firstInit = false;
        }
        Arrays.fill(this.checkUsernames, false);
    }

    private void refreshWhitelist() {
        int length = this.theBanner.getWhitelistLength();
        GuiTextField[] allowedPlayers_new = new GuiTextField[length];
        boolean[] invalidUsernames_new = new boolean[length];
        boolean[] checkUsernames_new = new boolean[length];
        for(int i = 0; i < length; ++i) {
            allowedPlayers_new[i] = i < this.allowedPlayers.length ? this.allowedPlayers[i] : new GuiTextField(this.fontRendererObj, 0, 0, 140, 20);
            if(i < this.invalidUsernames.length) {
                invalidUsernames_new[i] = this.invalidUsernames[i];
            }
            if(i >= this.checkUsernames.length) continue;
            checkUsernames_new[i] = this.checkUsernames[i];
        }
        this.allowedPlayers = allowedPlayers_new;
        this.invalidUsernames = invalidUsernames_new;
        this.checkUsernames = checkUsernames_new;
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        String s;
        this.setupScrollBar(i, j);
        this.alignmentField.setVisible(false);
        this.alignmentField.setEnabled(false);
        for(GuiTextField textBox : this.allowedPlayers) {
            textBox.setVisible(false);
            textBox.setEnabled(false);
        }
        this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(guiTexture);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        String title = StatCollector.translateToLocal("lotr.gui.bannerEdit.title");
        this.fontRendererObj.drawString(title, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(title) / 2, this.guiTop + 6, 4210752);
        if(this.theBanner.isPlayerSpecificProtection()) {
            this.modeButton.displayString = StatCollector.translateToLocal("lotr.gui.bannerEdit.protectionMode.playerSpecific");
            s = StatCollector.translateToLocal("lotr.gui.bannerEdit.protectionMode.playerSpecific.desc.1");
            this.fontRendererObj.drawString(s, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, this.guiTop + 46, 4210752);
            s = StatCollector.translateToLocal("lotr.gui.bannerEdit.protectionMode.playerSpecific.desc.2");
            this.fontRendererObj.drawString(s, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, this.guiTop + 46 + this.fontRendererObj.FONT_HEIGHT, 4210752);
            s = LOTRFellowshipProfile.getFellowshipCodeHint();
            this.fontRendererObj.drawString(s, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, this.guiTop + 200, 4210752);
            int start = 0 + Math.round(this.currentScroll * (this.allowedPlayers.length - 5));
            int end = start + 5 - 1;
            start = Math.max(start, 0);
            end = Math.min(end, this.allowedPlayers.length - 1);
            for(int index = start; index <= end; ++index) {
                int displayIndex = index - start;
                GuiTextField textBox = this.allowedPlayers[index];
                textBox.setVisible(true);
                textBox.setEnabled(index != 0);
                textBox.xPosition = this.guiLeft + this.xSize / 2 - 70;
                textBox.yPosition = this.guiTop + 70 + displayIndex * (textBox.height + 4);
                textBox.drawTextBox();
                String number = index + 1 + ".";
                this.fontRendererObj.drawString(number, this.guiLeft + 24 - this.fontRendererObj.getStringWidth(number), textBox.yPosition + 6, 4210752);
            }
            if(this.hasScrollBar()) {
                this.mc.getTextureManager().bindTexture(guiTexture);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                this.drawTexturedModalRect(this.guiLeft + this.scrollBarX, this.guiTop + this.scrollBarY, 200, 0, this.scrollBarWidth, this.scrollBarHeight);
                if(this.canScroll()) {
                    int scroll = (int) (this.currentScroll * (this.scrollBarHeight - this.scrollBarBorder * 2 - this.scrollWidgetHeight));
                    this.drawTexturedModalRect(this.guiLeft + this.scrollBarX + this.scrollBarBorder, this.guiTop + this.scrollBarY + this.scrollBarBorder + scroll, 214, 0, this.scrollWidgetWidth, this.scrollWidgetHeight);
                }
            }
        }
        else {
            this.modeButton.displayString = StatCollector.translateToLocal("lotr.gui.bannerEdit.protectionMode.faction");
            s = StatCollector.translateToLocalFormatted("lotr.gui.bannerEdit.protectionMode.faction.desc.1", new Object[0]);
            this.fontRendererObj.drawString(s, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, this.guiTop + 46, 4210752);
            s = StatCollector.translateToLocalFormatted("lotr.gui.bannerEdit.protectionMode.faction.desc.2", Float.valueOf(this.theBanner.getAlignmentProtection()), this.theBanner.getBannerType().faction.factionName());
            this.fontRendererObj.drawString(s, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, this.guiTop + 46 + this.fontRendererObj.FONT_HEIGHT, 4210752);
            s = StatCollector.translateToLocal("lotr.gui.bannerEdit.protectionMode.faction.desc.3");
            this.fontRendererObj.drawString(s, this.guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, this.guiTop + 46 + this.fontRendererObj.FONT_HEIGHT * 2, 4210752);
            s = StatCollector.translateToLocal("lotr.gui.bannerEdit.protectionMode.faction.alignment");
            this.fontRendererObj.drawString(s, this.alignmentField.xPosition, this.alignmentField.yPosition - this.fontRendererObj.FONT_HEIGHT - 3, 4210752);
            this.alignmentField.setVisible(true);
            this.alignmentField.setEnabled(true);
            this.alignmentField.drawTextBox();
        }
        super.drawScreen(i, j, f);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.selfProtectionButton.setState(this.theBanner.isSelfProtection());
        this.buttonAddSlot.visible = this.buttonRemoveSlot.visible = this.theBanner.isPlayerSpecificProtection();
        this.buttonAddSlot.enabled = this.theBanner.getWhitelistLength() < LOTREntityBanner.WHITELIST_MAX;
        this.buttonRemoveSlot.enabled = this.theBanner.getWhitelistLength() > LOTREntityBanner.WHITELIST_MIN;
        this.alignmentField.updateCursorCounter();
        this.alignmentField.setVisible(!this.theBanner.isPlayerSpecificProtection());
        this.alignmentField.setEnabled(this.alignmentField.getVisible());
        if(this.alignmentField.getVisible() && !this.alignmentField.isFocused()) {
            float alignment;
            float prevAlignment = this.theBanner.getAlignmentProtection();
            try {
                alignment = Float.parseFloat(this.alignmentField.getText());
            }
            catch(NumberFormatException e) {
                alignment = 0.0f;
            }
            alignment = Math.max(alignment, LOTREntityBanner.ALIGNMENT_PROTECTION_MIN);
            alignment = Math.min(alignment, LOTREntityBanner.ALIGNMENT_PROTECTION_MAX);
            this.theBanner.setAlignmentProtection(alignment);
            this.alignmentField.setText(LOTRAlignmentValues.formatAlignForDisplay(alignment));
            if(alignment != prevAlignment) {
                this.sendBannerData(false);
            }
        }
        for(GuiTextField textBox : this.allowedPlayers) {
            textBox.updateCursorCounter();
        }
    }

    private void setupScrollBar(int i, int j) {
        boolean isMouseDown = Mouse.isButtonDown(0);
        int i1 = this.guiLeft + this.scrollBarX;
        int j1 = this.guiTop + this.scrollBarY;
        int i2 = i1 + this.scrollBarWidth;
        int j2 = j1 + this.scrollBarHeight;
        if(!this.wasMouseDown && isMouseDown && i >= i1 && j >= j1 && i < i2 && j < j2) {
            this.isScrolling = this.canScroll();
        }
        if(!isMouseDown) {
            this.isScrolling = false;
        }
        this.wasMouseDown = isMouseDown;
        if(this.isScrolling) {
            this.currentScroll = (j - j1 - this.scrollWidgetHeight / 2.0f) / ((float) (j2 - j1) - (float) this.scrollWidgetHeight);
            if(this.currentScroll < 0.0f) {
                this.currentScroll = 0.0f;
            }
            if(this.currentScroll > 1.0f) {
                this.currentScroll = 1.0f;
            }
        }
    }

    private boolean hasScrollBar() {
        return this.theBanner.isPlayerSpecificProtection();
    }

    private boolean canScroll() {
        return true;
    }

    @Override
    protected void keyTyped(char c, int i) {
        if(this.alignmentField.getVisible() && this.alignmentField.textboxKeyTyped(c, i)) {
            return;
        }
        for(int l = 1; l < this.allowedPlayers.length; ++l) {
            GuiTextField textBox = this.allowedPlayers[l];
            if(!textBox.getVisible() || !textBox.textboxKeyTyped(c, i)) continue;
            this.checkUsernames[l] = true;
            return;
        }
        super.keyTyped(c, i);
    }

    @Override
    protected void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        if(this.alignmentField.getVisible()) {
            this.alignmentField.mouseClicked(i, j, k);
        }
        for(int l = 1; l < this.allowedPlayers.length; ++l) {
            GuiTextField textBox = this.allowedPlayers[l];
            if(!textBox.getVisible()) continue;
            textBox.mouseClicked(i, j, k);
            if(!textBox.isFocused() && this.checkUsernames[l]) {
                this.checkUsernameValid(l);
                this.checkUsernames[l] = false;
            }
            if(!textBox.isFocused() || !this.invalidUsernames[l]) continue;
            this.invalidUsernames[l] = false;
            textBox.setTextColor(16777215);
            textBox.setText("");
        }
    }

    private void checkUsernameValid(int index) {
        GuiTextField textBox = this.allowedPlayers[index];
        String username = textBox.getText();
        if(!StringUtils.isNullOrEmpty(username) && !this.invalidUsernames[index]) {
            LOTRPacketBannerRequestInvalidName packet = new LOTRPacketBannerRequestInvalidName(this.theBanner, index, username);
            LOTRPacketHandler.networkWrapper.sendToServer(packet);
        }
    }

    public void setUsernameInvalid(int index, String prevText) {
        GuiTextField textBox = this.allowedPlayers[index];
        String text = textBox.getText();
        if(text.equals(prevText)) {
            this.invalidUsernames[index] = true;
            textBox.setTextColor(16711680);
            textBox.setText(StatCollector.translateToLocal("lotr.gui.bannerEdit.invalidUsername"));
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();
        if(i != 0 && this.canScroll()) {
            int j = this.allowedPlayers.length - 5;
            if(i > 0) {
                i = 1;
            }
            if(i < 0) {
                i = -1;
            }
            this.currentScroll = (float) (this.currentScroll - (double) i / (double) j);
            if(this.currentScroll < 0.0f) {
                this.currentScroll = 0.0f;
            }
            if(this.currentScroll > 1.0f) {
                this.currentScroll = 1.0f;
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(button.enabled) {
            if(button == this.modeButton) {
                this.theBanner.setPlayerSpecificProtection(!this.theBanner.isPlayerSpecificProtection());
            }
            if(button == this.selfProtectionButton) {
                this.theBanner.setSelfProtection(!this.theBanner.isSelfProtection());
            }
            if(button == this.buttonAddSlot) {
                this.theBanner.resizeWhitelist(this.theBanner.getWhitelistLength() + 1);
                this.refreshWhitelist();
            }
            if(button == this.buttonRemoveSlot) {
                this.theBanner.resizeWhitelist(this.theBanner.getWhitelistLength() - 1);
                this.refreshWhitelist();
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.sendBannerData(true);
    }

    private void sendBannerData(boolean sendNames) {
        LOTRPacketEditBanner packet = new LOTRPacketEditBanner(this.theBanner);
        packet.playerSpecificProtection = this.theBanner.isPlayerSpecificProtection();
        packet.selfProtection = this.theBanner.isSelfProtection();
        packet.alignmentProtection = this.theBanner.getAlignmentProtection();
        packet.whitelistLength = this.theBanner.getWhitelistLength();
        if(sendNames) {
            String[] whitelistSlots = new String[this.allowedPlayers.length];
            for(int index = 1; index < this.allowedPlayers.length; ++index) {
                String username;
                String text = this.allowedPlayers[index].getText();
                if(StringUtils.isNullOrEmpty(text) || this.invalidUsernames[index]) {
                    this.theBanner.whitelistPlayer(index, null);
                }
                else if(LOTRFellowshipProfile.hasFellowshipCode(text)) {
                    this.theBanner.whitelistPlayer(index, new LOTRFellowshipProfile(this.theBanner, null, LOTRFellowshipProfile.stripFellowshipCode(text)));
                }
                else {
                    this.theBanner.whitelistPlayer(index, new GameProfile(null, text));
                }
                GameProfile profile = this.theBanner.getWhitelistedPlayer(index);
                whitelistSlots[index] = profile == null ? null : (StringUtils.isNullOrEmpty(username = profile.getName()) ? null : username);
            }
            packet.whitelistSlots = whitelistSlots;
        }
        LOTRPacketHandler.networkWrapper.sendToServer(packet);
    }
}
