package lotr.common.fellowship;

import java.util.*;
import lotr.common.LOTRTitle;
import net.minecraft.item.ItemStack;

public class LOTRFellowshipClient {
    private UUID fellowshipID;
    private String fellowshipName;
    private ItemStack fellowshipIcon;
    private boolean isOwned;
    private boolean isAdminned;
    private String ownerName;
    private List<String> allPlayerNames = new ArrayList<String>();
    private List<String> memberNames = new ArrayList<String>();
    private Map<String, LOTRTitle.PlayerTitle> titleMap = new HashMap<String, LOTRTitle.PlayerTitle>();
    private Set<String> adminNames = new HashSet<String>();
    private boolean preventPVP;
    private boolean preventHiredFF;
    private boolean showMapLocations;

    public LOTRFellowshipClient(UUID id, String name, boolean owned, boolean admin, List<String> members) {
        this.fellowshipID = id;
        this.fellowshipName = name;
        this.isOwned = owned;
        this.isAdminned = admin;
        this.ownerName = members.get(0);
        this.allPlayerNames = members;
        this.memberNames = new ArrayList<String>(this.allPlayerNames);
        this.memberNames.remove(this.ownerName);
    }

    public void setTitles(Map<String, LOTRTitle.PlayerTitle> titles) {
        this.titleMap = titles;
    }

    public void setAdmins(Set<String> admins) {
        this.adminNames = admins;
    }

    public void setIcon(ItemStack itemstack) {
        this.fellowshipIcon = itemstack;
    }

    public void setPreventPVP(boolean flag) {
        this.preventPVP = flag;
    }

    public void setPreventHiredFriendlyFire(boolean flag) {
        this.preventHiredFF = flag;
    }

    public void setShowMapLocations(boolean flag) {
        this.showMapLocations = flag;
    }

    public UUID getFellowshipID() {
        return this.fellowshipID;
    }

    public String getName() {
        return this.fellowshipName;
    }

    public boolean isOwned() {
        return this.isOwned;
    }

    public boolean isAdminned() {
        return this.isAdminned;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public List<String> getMemberNames() {
        return this.memberNames;
    }

    public List<String> getAllPlayerNames() {
        return this.allPlayerNames;
    }

    public boolean isPlayerIn(String name) {
        return this.allPlayerNames.contains(name);
    }

    public int getMemberCount() {
        return this.allPlayerNames.size();
    }

    public LOTRTitle.PlayerTitle getTitleFor(String name) {
        return this.titleMap.get(name);
    }

    public boolean isAdmin(String name) {
        return this.adminNames.contains(name);
    }

    public ItemStack getIcon() {
        return this.fellowshipIcon;
    }

    public boolean getPreventPVP() {
        return this.preventPVP;
    }

    public boolean getPreventHiredFriendlyFire() {
        return this.preventHiredFF;
    }

    public boolean getShowMapLocations() {
        return this.showMapLocations;
    }

    public void updateDataFrom(LOTRFellowshipClient other) {
        this.fellowshipName = other.fellowshipName;
        this.fellowshipIcon = other.fellowshipIcon;
        this.isOwned = other.isOwned;
        this.isAdminned = other.isAdminned;
        this.ownerName = other.ownerName;
        this.allPlayerNames = other.allPlayerNames;
        this.memberNames = other.memberNames;
        this.titleMap = other.titleMap;
        this.adminNames = other.adminNames;
        this.preventPVP = other.preventPVP;
        this.preventHiredFF = other.preventHiredFF;
        this.showMapLocations = other.showMapLocations;
    }
}
