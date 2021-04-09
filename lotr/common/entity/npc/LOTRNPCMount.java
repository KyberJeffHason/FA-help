package lotr.common.entity.npc;

import net.minecraft.item.ItemStack;

public interface LOTRNPCMount {
    public boolean isMountSaddled();

    public boolean getBelongsToNPC();

    public void setBelongsToNPC(boolean var1);

    public void super_moveEntityWithHeading(float var1, float var2);

    public String getMountArmorTexture();

    public boolean isMountArmorValid(ItemStack var1);
}
