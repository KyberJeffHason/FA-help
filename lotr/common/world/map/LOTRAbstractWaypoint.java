package lotr.common.world.map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface LOTRAbstractWaypoint {
    public int getX();

    public int getY();

    public int getXCoord();

    public int getZCoord();

    public int getYCoord(World var1, int var2, int var3);

    public int getYCoordDisplay();

    public String getCodeName();

    public String getDisplayName();

    public String getLoreText(EntityPlayer var1);

    public boolean hasPlayerUnlocked(EntityPlayer var1);

    public boolean isUnlockable(EntityPlayer var1);

    public boolean isHidden();

    public int getID();
}
