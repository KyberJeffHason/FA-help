package lotr.common.entity.item;

import java.util.*;

import com.mojang.authlib.GameProfile;

import lotr.common.LOTRBannerProtection;

public class LOTRBannerWhitelistEntry {
	public final GameProfile profile;
	private Set<LOTRBannerProtection.Permission> perms = new HashSet<>();

	public LOTRBannerWhitelistEntry(GameProfile p) {
		profile = p;
		if (profile == null) {
			throw new IllegalArgumentException("Banner whitelist entry cannot have a null profile!");
		}
	}

	public LOTRBannerWhitelistEntry setFullPerms() {
		clearPermissions();
		addPermission(LOTRBannerProtection.Permission.FULL);
		return this;
	}

	public boolean isPermissionEnabled(LOTRBannerProtection.Permission p) {
		return perms.contains(p);
	}

	public boolean allowsPermission(LOTRBannerProtection.Permission p) {
		return isPermissionEnabled(LOTRBannerProtection.Permission.FULL) || isPermissionEnabled(p);
	}

	public void addPermission(LOTRBannerProtection.Permission p) {
		perms.add(p);
	}

	public void removePermission(LOTRBannerProtection.Permission p) {
		perms.remove(p);
	}

	public void clearPermissions() {
		perms.clear();
	}

	public Set<LOTRBannerProtection.Permission> listPermissions() {
		return perms;
	}

	public int encodePermBitFlags() {
		return LOTRBannerWhitelistEntry.static_encodePermBitFlags(perms);
	}

	public void decodePermBitFlags(int i) {
		perms.clear();
		List<LOTRBannerProtection.Permission> decoded = LOTRBannerWhitelistEntry.static_decodePermBitFlags(i);
		for (LOTRBannerProtection.Permission p : decoded) {
			addPermission(p);
		}
	}

	public static int static_encodePermBitFlags(Collection<LOTRBannerProtection.Permission> permList) {
		int i = 0;
		for (LOTRBannerProtection.Permission p : permList) {
			i |= p.bitFlag;
		}
		return i;
	}

	public static List<LOTRBannerProtection.Permission> static_decodePermBitFlags(int i) {
		ArrayList<LOTRBannerProtection.Permission> decoded = new ArrayList<>();
		for (LOTRBannerProtection.Permission p : LOTRBannerProtection.Permission.values()) {
			if ((i & p.bitFlag) == 0) {
				continue;
			}
			decoded.add(p);
		}
		return decoded;
	}
}