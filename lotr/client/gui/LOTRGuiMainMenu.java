package lotr.client.gui;

import java.util.*;
import org.lwjgl.opengl.GL11;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import lotr.common.world.map.LOTRWaypoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.*;
import net.minecraftforge.client.ForgeHooksClient;

public class LOTRGuiMainMenu extends GuiMainMenu {
    private static final ResourceLocation titleTexture = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation vignetteTexture = new ResourceLocation("textures/misc/vignette.png");
    private LOTRGuiMap mapGui = new LOTRGuiMap();
    private static LOTRGuiRendererMap mapRenderer;
    private static int tickCounter;
    private static Random rand;
    private static List<LOTRWaypoint> waypointRoute;
    private static int currentWPIndex;
    private static boolean randomWPStart;
    private static float mapSpeed;
    private static float mapVelX;
    private static float mapVelY;
    private static final float wpChangeDistance = 12.0f;
    private static final float mapSpeedMax = 0.8f;
    private static final float mapSpeedIncr = 0.01f;
    private static final float mapAccel = 0.02f;
    private static final float zoomBase = -0.1f;
    private static final float zoomOscilSpeed = 0.003f;
    private static final float zoomOscilMax = 0.8f;

    public LOTRGuiMainMenu() {
        mapRenderer = new LOTRGuiRendererMap();
        mapRenderer.setSepia(false);
        if(waypointRoute.isEmpty()) {
            LOTRGuiMainMenu.setupWaypoints();
            currentWPIndex = randomWPStart ? rand.nextInt(waypointRoute.size()) : 0;
        }
        LOTRWaypoint wp = waypointRoute.get(currentWPIndex);
        LOTRGuiMainMenu.mapRenderer.prevMapX = LOTRGuiMainMenu.mapRenderer.mapX = wp.getX();
        LOTRGuiMainMenu.mapRenderer.prevMapY = LOTRGuiMainMenu.mapRenderer.mapY = wp.getY();
    }

    private static void setupWaypoints() {
        waypointRoute.clear();
        waypointRoute.add(LOTRWaypoint.HOBBITON);
        waypointRoute.add(LOTRWaypoint.BRANDYWINE_BRIDGE);
        waypointRoute.add(LOTRWaypoint.BUCKLEBURY);
        waypointRoute.add(LOTRWaypoint.WITHYWINDLE_VALLEY);
        waypointRoute.add(LOTRWaypoint.BREE);
        waypointRoute.add(LOTRWaypoint.WEATHERTOP);
        waypointRoute.add(LOTRWaypoint.RIVENDELL);
        waypointRoute.add(LOTRWaypoint.WEST_GATE);
        waypointRoute.add(LOTRWaypoint.DIMRILL_DALE);
        waypointRoute.add(LOTRWaypoint.CERIN_AMROTH);
        waypointRoute.add(LOTRWaypoint.CARAS_GALADHON);
        waypointRoute.add(LOTRWaypoint.NORTH_UNDEEP);
        waypointRoute.add(LOTRWaypoint.SOUTH_UNDEEP);
        waypointRoute.add(LOTRWaypoint.ARGONATH);
        waypointRoute.add(LOTRWaypoint.RAUROS);
        waypointRoute.add(LOTRWaypoint.EDORAS);
        waypointRoute.add(LOTRWaypoint.HELMS_DEEP);
        waypointRoute.add(LOTRWaypoint.ISENGARD);
        waypointRoute.add(LOTRWaypoint.DUNHARROW);
        waypointRoute.add(LOTRWaypoint.ERECH);
        waypointRoute.add(LOTRWaypoint.MINAS_TIRITH);
        waypointRoute.add(LOTRWaypoint.MINAS_MORGUL);
        waypointRoute.add(LOTRWaypoint.MOUNT_DOOM);
        waypointRoute.add(LOTRWaypoint.MORANNON);
        waypointRoute.add(LOTRWaypoint.EAST_RHOVANION_ROAD);
        waypointRoute.add(LOTRWaypoint.OLD_RHOVANION);
        waypointRoute.add(LOTRWaypoint.RUNNING_FORD);
        waypointRoute.add(LOTRWaypoint.DALE_CITY);
        waypointRoute.add(LOTRWaypoint.THRANDUIL_HALLS);
        waypointRoute.add(LOTRWaypoint.ENCHANTED_RIVER);
        waypointRoute.add(LOTRWaypoint.FOREST_GATE);
        waypointRoute.add(LOTRWaypoint.BEORN);
        waypointRoute.add(LOTRWaypoint.EAGLES_EYRIE);
        waypointRoute.add(LOTRWaypoint.GOBLIN_TOWN);
        waypointRoute.add(LOTRWaypoint.MOUNT_GRAM);
        waypointRoute.add(LOTRWaypoint.FORNOST);
        waypointRoute.add(LOTRWaypoint.ANNUMINAS);
        waypointRoute.add(LOTRWaypoint.MITHLOND_NORTH);
        waypointRoute.add(LOTRWaypoint.TOWER_HILLS);
    }

    @Override
    public void initGui() {
        super.initGui();
        int lowerButtonMaxY = 0;
        for(Object obj : this.buttonList) {
            GuiButton button = (GuiButton) obj;
            int buttonMaxY = button.yPosition + button.height;
            if(buttonMaxY <= lowerButtonMaxY) continue;
            lowerButtonMaxY = buttonMaxY;
        }
        int idealMoveDown = 50;
        int lowestSuitableHeight = this.height - 25;
        int moveDown = Math.min(idealMoveDown, lowestSuitableHeight - lowerButtonMaxY);
        moveDown = Math.max(moveDown, 0);
        for(int i = 0; i < this.buttonList.size(); ++i) {
            GuiButton button = (GuiButton) this.buttonList.get(i);
            button.yPosition += moveDown;
            if(button.getClass() != GuiButton.class) continue;
            LOTRGuiButtonRedBook newButton = new LOTRGuiButtonRedBook(button.id, button.xPosition, button.yPosition, button.width, button.height, button.displayString);
            this.buttonList.set(i, newButton);
        }
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int i, int j) {
        super.setWorldAndResolution(mc, i, j);
        this.mapGui.setWorldAndResolution(mc, i, j);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ++tickCounter;
        mapRenderer.updateTick();
        LOTRWaypoint wp = waypointRoute.get(currentWPIndex);
        float dx = wp.getX() - LOTRGuiMainMenu.mapRenderer.mapX;
        float dy = wp.getY() - LOTRGuiMainMenu.mapRenderer.mapY;
        float distSq = dx * dx + dy * dy;
        float dist = (float) Math.sqrt(distSq);
        if(dist <= 12.0f) {
            if(++currentWPIndex >= waypointRoute.size()) {
                currentWPIndex = 0;
            }
        }
        else {
            mapSpeed += 0.01f;
            mapSpeed = Math.min(mapSpeed, 0.8f);
            float vXNew = dx / dist * mapSpeed;
            float vYNew = dy / dist * mapSpeed;
            float a = 0.02f;
            mapVelX += (vXNew - mapVelX) * a;
            mapVelY += (vYNew - mapVelY) * a;
        }
        LOTRGuiMainMenu.mapRenderer.mapX += mapVelX;
        LOTRGuiMainMenu.mapRenderer.mapY += mapVelY;
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        GL11.glEnable(3008);
        GL11.glEnable(3042);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        LOTRGuiMainMenu.mapRenderer.zoomExp = -0.1f + MathHelper.cos((tickCounter + f) * 0.003f) * 0.8f;
        LOTRGuiMainMenu.mapRenderer.zoomStable = (float) Math.pow(2.0, -0.10000000149011612);
        mapRenderer.renderMap(this, this.mapGui, f);
        mapRenderer.renderVignettes(this, this.zLevel, 2);
        GL11.glDisable(3042);
        Tessellator tessellator = Tessellator.instance;
        int short1 = 274;
        int k = this.width / 2 - short1 / 2;
        int b0 = 30;
        this.mc.getTextureManager().bindTexture(titleTexture);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.drawTexturedModalRect(k + 0, b0 + 0, 0, 0, 155, 44);
        this.drawTexturedModalRect(k + 155, b0 + 0, 0, 45, 155, 44);
        String modSubtitle = StatCollector.translateToLocal("lotr.menu.title");
        this.drawString(this.fontRendererObj, modSubtitle, this.width / 2 - this.fontRendererObj.getStringWidth(modSubtitle) / 2, 80, -1);
        List<String> brandings = Lists.reverse(FMLCommonHandler.instance().getBrandings(true));
        for(int l = 0; l < brandings.size(); ++l) {
            String brd = brandings.get(l);
            if(Strings.isNullOrEmpty(brd)) continue;
            this.drawString(this.fontRendererObj, brd, 2, this.height - (10 + l * (this.fontRendererObj.FONT_HEIGHT + 1)), -1);
        }
        ForgeHooksClient.renderMainMenu(this, this.fontRendererObj, this.width, this.height);
        String copyright = "Copyright Mojang AB. Do not distribute!";
        this.drawString(this.fontRendererObj, copyright, this.width - this.fontRendererObj.getStringWidth(copyright) - 2, this.height - 10, -1);
        String field_92025_p = (String) ObfuscationReflectionHelper.getPrivateValue(GuiMainMenu.class, this, "field_92025_p");
        String field_146972_A = (String) ObfuscationReflectionHelper.getPrivateValue(GuiMainMenu.class, this, "field_146972_A");
        int field_92024_r = (Integer) ObfuscationReflectionHelper.getPrivateValue(GuiMainMenu.class, this, "field_92024_r");
        int field_92022_t = (Integer) ObfuscationReflectionHelper.getPrivateValue(GuiMainMenu.class, this, "field_92022_t");
        int field_92021_u = (Integer) ObfuscationReflectionHelper.getPrivateValue(GuiMainMenu.class, this, "field_92021_u");
        int field_92020_v = (Integer) ObfuscationReflectionHelper.getPrivateValue(GuiMainMenu.class, this, "field_92020_v");
        int field_92019_w = (Integer) ObfuscationReflectionHelper.getPrivateValue(GuiMainMenu.class, this, "field_92019_w");
        if(field_92025_p != null && field_92025_p.length() > 0) {
            Gui.drawRect(field_92022_t - 2, field_92021_u - 2, field_92020_v + 2, field_92019_w - 1, 1428160512);
            this.drawString(this.fontRendererObj, field_92025_p, field_92022_t, field_92021_u, -1);
            this.drawString(this.fontRendererObj, field_146972_A, (this.width - field_92024_r) / 2, ((GuiButton) this.buttonList.get(0)).yPosition - 12, -1);
        }
        for(Object button : this.buttonList) {
            ((GuiButton) button).drawButton(this.mc, i, j);
        }
        for(Object label : this.labelList) {
            ((GuiLabel) label).func_146159_a(this.mc, i, j);
        }
    }

    static {
        rand = new Random();
        waypointRoute = new ArrayList<LOTRWaypoint>();
        randomWPStart = false;
    }
}
