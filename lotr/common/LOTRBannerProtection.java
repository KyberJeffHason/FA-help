package lotr.common;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.mojang.authlib.GameProfile;

import lotr.common.entity.LOTREntityInvasionSpawner;
import lotr.common.entity.item.LOTREntityBanner;
import lotr.common.fac.LOTRFaction;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public class LOTRBannerProtection {
	public static final int MAX_RANGE = 100000;
	private static Map<Pair, Integer> protectionBlocks = new HashMap<>();
	private static Map<UUID, Integer> lastWarningTimes;

	public static int getProtectionRange(Block block, int meta) {
		Integer i = protectionBlocks.get(Pair.of((Object) block, (Object) meta));
		if (i == null) {
			return 0;
		}
		return i;
	}

	public static boolean isProtected(World world, Entity entity, IFilter protectFilter, boolean sendMessage) {
		int i = MathHelper.floor_double(entity.posX);
		int j = MathHelper.floor_double(entity.boundingBox.minY);
		int k = MathHelper.floor_double(entity.posZ);
		return LOTRBannerProtection.isProtected(world, i, j, k, protectFilter, sendMessage);
	}

	public static boolean isProtected(World world, int i, int j, int k, IFilter protectFilter, boolean sendMessage) {
		return LOTRBannerProtection.isProtected(world, i, j, k, protectFilter, sendMessage, 0.0);
	}

	public static boolean isProtected(World world, int i, int j, int k, IFilter protectFilter, boolean sendMessage, double searchExtra) {
		if (!LOTRConfig.allowBannerProtection) {
			return false;
		}
		String protectorName = null;
		AxisAlignedBB originCube = AxisAlignedBB.getBoundingBox(i, j, k, i + 1, j + 1, k + 1).expand(searchExtra, 30000.0, searchExtra);
		AxisAlignedBB searchCube = originCube.expand(64.0, 30000.0, 64.0);
		List banners = world.getEntitiesWithinAABB(LOTREntityBanner.class, searchCube);
		if (!banners.isEmpty()) {
			for (Object banner2 : banners) {
				ProtectType result;
				LOTREntityBanner banner = (LOTREntityBanner) banner2;
				AxisAlignedBB protectionCube = banner.createProtectionCube();
				if (!banner.isProtectingTerritory() || !protectionCube.intersectsWith(searchCube) || !protectionCube.intersectsWith(originCube) || (result = protectFilter.protects(banner)) == ProtectType.NONE) {
					continue;
				}
				if (result == ProtectType.FACTION) {
					protectorName = banner.getBannerType().faction.factionName();
					break;
				}
				if (result == ProtectType.PLAYER_SPECIFIC) {
					GameProfile placingPlayer = banner.getPlacingPlayer();
					if (placingPlayer != null) {
						if (StringUtils.isBlank(placingPlayer.getName())) {
							MinecraftServer.getServer().func_147130_as().fillProfileProperties(placingPlayer, true);
						}
						protectorName = placingPlayer.getName();
						break;
					}
					protectorName = "?";
					break;
				}
				if (result != ProtectType.STRUCTURE) {
					continue;
				}
				protectorName = StatCollector.translateToLocal("chat.lotr.protectedStructure");
				break;
			}
		}
		if (protectorName != null) {
			if (sendMessage) {
				protectFilter.warnProtection(new ChatComponentTranslation("chat.lotr.protectedLand", new Object[] { protectorName }));
			}
			return true;
		}
		return false;
	}

	public static IFilter anyBanner() {
		return new IFilter() {

			@Override
			public ProtectType protects(LOTREntityBanner banner) {
				if (banner.isStructureProtection()) {
					return ProtectType.STRUCTURE;
				}
				return ProtectType.FACTION;
			}

			@Override
			public void warnProtection(IChatComponent message) {
			}
		};
	}

	public static IFilter forPlayer(EntityPlayer entityplayer) {
		return LOTRBannerProtection.forPlayer(entityplayer, Permission.FULL);
	}

	public static IFilter forPlayer(EntityPlayer entityplayer, Permission perm) {
		return new FilterForPlayer(entityplayer, perm);
	}

	public static IFilter forPlayer_returnMessage(final EntityPlayer entityplayer, final Permission perm, final IChatComponent[] protectionMessage) {
		return new IFilter() {
			private IFilter internalPlayerFilter;
			{
				internalPlayerFilter = LOTRBannerProtection.forPlayer(entityplayer, perm);
			}

			@Override
			public ProtectType protects(LOTREntityBanner banner) {
				return internalPlayerFilter.protects(banner);
			}

			@Override
			public void warnProtection(IChatComponent message) {
				internalPlayerFilter.warnProtection(message);
				protectionMessage[0] = message;
			}
		};
	}

	public static IFilter forNPC(final EntityLiving entity) {
		return new IFilter() {

			@Override
			public ProtectType protects(LOTREntityBanner banner) {
				if (banner.isStructureProtection()) {
					return ProtectType.STRUCTURE;
				}
				if (banner.getBannerType().faction.isBadRelation(LOTRMod.getNPCFaction(entity))) {
					return ProtectType.FACTION;
				}
				return ProtectType.NONE;
			}

			@Override
			public void warnProtection(IChatComponent message) {
			}
		};
	}

	public static IFilter forInvasionSpawner(LOTREntityInvasionSpawner spawner) {
		return LOTRBannerProtection.forFaction(spawner.getInvasionType().invasionFaction);
	}

	public static IFilter forFaction(final LOTRFaction theFaction) {
		return new IFilter() {

			@Override
			public ProtectType protects(LOTREntityBanner banner) {
				if (banner.isStructureProtection()) {
					return ProtectType.STRUCTURE;
				}
				if (banner.getBannerType().faction.isBadRelation(theFaction)) {
					return ProtectType.FACTION;
				}
				return ProtectType.NONE;
			}

			@Override
			public void warnProtection(IChatComponent message) {
			}
		};
	}

	public static IFilter forTNT(final EntityTNTPrimed bomb) {
		return new IFilter() {

			@Override
			public ProtectType protects(LOTREntityBanner banner) {
				if (banner.isStructureProtection()) {
					return ProtectType.STRUCTURE;
				}
				EntityLivingBase bomber = bomb.getTntPlacedBy();
				if (bomber == null) {
					return ProtectType.FACTION;
				}
				if (bomber instanceof EntityPlayer) {
					return LOTRBannerProtection.forPlayer((EntityPlayer) bomber, Permission.FULL).protects(banner);
				}
				if (bomber instanceof EntityLiving) {
					return LOTRBannerProtection.forNPC((EntityLiving) bomber).protects(banner);
				}
				return ProtectType.NONE;
			}

			@Override
			public void warnProtection(IChatComponent message) {
			}
		};
	}

	public static IFilter forTNTMinecart(EntityMinecartTNT minecart) {
		return new IFilter() {

			@Override
			public ProtectType protects(LOTREntityBanner banner) {
				if (banner.isStructureProtection()) {
					return ProtectType.STRUCTURE;
				}
				return ProtectType.FACTION;
			}

			@Override
			public void warnProtection(IChatComponent message) {
			}
		};
	}

	public static IFilter forThrown(final EntityThrowable throwable) {
		return new IFilter() {

			@Override
			public ProtectType protects(LOTREntityBanner banner) {
				if (banner.isStructureProtection()) {
					return ProtectType.STRUCTURE;
				}
				EntityLivingBase thrower = throwable.getThrower();
				if (thrower == null) {
					return ProtectType.FACTION;
				}
				if (thrower instanceof EntityPlayer) {
					return LOTRBannerProtection.forPlayer((EntityPlayer) thrower, Permission.FULL).protects(banner);
				}
				if (thrower instanceof EntityLiving) {
					return LOTRBannerProtection.forNPC((EntityLiving) thrower).protects(banner);
				}
				return ProtectType.NONE;
			}

			@Override
			public void warnProtection(IChatComponent message) {
			}
		};
	}

	private static void setWarningCooldown(EntityPlayer entityplayer) {
		lastWarningTimes.put(entityplayer.getUniqueID(), LOTRConfig.bannerWarningCooldown);
	}

	private static boolean hasWarningCooldown(EntityPlayer entityplayer) {
		return lastWarningTimes.containsKey(entityplayer.getUniqueID());
	}

	public static void updateWarningCooldowns() {
		HashSet<UUID> removes = new HashSet<>();
		for (Map.Entry<UUID, Integer> e : lastWarningTimes.entrySet()) {
			UUID player = e.getKey();
			int time = e.getValue();
			e.setValue(--time);
			if (time > 0) {
				continue;
			}
			removes.add(player);
		}
		for (UUID player : removes) {
			lastWarningTimes.remove(player);
		}
	}

	static {
		Pair BRONZE = Pair.of((Object) LOTRMod.blockOreStorage, (Object) 2);
		Pair SILVER = Pair.of((Object) LOTRMod.blockOreStorage, (Object) 3);
		Pair GOLD = Pair.of((Object) Blocks.gold_block, (Object) 0);
		protectionBlocks.put(BRONZE, 8);
		protectionBlocks.put(SILVER, 16);
		protectionBlocks.put(GOLD, 32);
		lastWarningTimes = new HashMap<>();
	}

	public static class FilterForPlayer implements IFilter {
		private EntityPlayer thePlayer;
		private Permission thePerm;

		public FilterForPlayer(EntityPlayer p, Permission perm) {
			thePlayer = p;
			thePerm = perm;
		}

		@Override
		public ProtectType protects(LOTREntityBanner banner) {
			ProtectType hook;
			if (thePlayer.capabilities.isCreativeMode) {
				return ProtectType.NONE;
			}
			if (banner.isStructureProtection()) {
				return ProtectType.STRUCTURE;
			}
			if (banner.isPlayerSpecificProtection()) {
				if (!banner.isPlayerWhitelisted(thePlayer, thePerm)) {
					return ProtectType.PLAYER_SPECIFIC;
				}
				return ProtectType.NONE;
			}
			if (!banner.isPlayerAllowedByFaction(thePlayer, thePerm)) {
				return ProtectType.FACTION;
			}
			return ProtectType.NONE;
		}

		@Override
		public void warnProtection(IChatComponent message) {
			if (thePlayer instanceof FakePlayer) {
				return;
			}
			if (thePlayer instanceof EntityPlayerMP && !thePlayer.worldObj.isRemote) {
				EntityPlayerMP entityplayermp = (EntityPlayerMP) thePlayer;
				entityplayermp.sendContainerToPlayer(thePlayer.inventoryContainer);
				if (!LOTRBannerProtection.hasWarningCooldown(entityplayermp)) {
					entityplayermp.addChatMessage(message);
					LOTRBannerProtection.setWarningCooldown(entityplayermp);
				}
			}
		}
	}

	public interface IFilter {
		ProtectType protects(LOTREntityBanner var1);

		void warnProtection(IChatComponent var1);
	}
	
	public static boolean isProtectedByBanner(World world, Entity entity, IFilter protectFilter, boolean sendMessage) {
        int i = MathHelper.floor_double(entity.posX);
        int j = MathHelper.floor_double(entity.boundingBox.minY);
        int k = MathHelper.floor_double(entity.posZ);
        return LOTRBannerProtection.isProtectedByBanner(world, i, j, k, protectFilter, sendMessage);
    }

    public static boolean isProtectedByBanner(World world, int i, int j, int k, IFilter protectFilter, boolean sendMessage) {
        return LOTRBannerProtection.isProtectedByBanner(world, i, j, k, protectFilter, sendMessage, 0.0);
    }

    public static boolean isProtectedByBanner(World world, int i, int j, int k, IFilter protectFilter, boolean sendMessage, double searchExtra) {
        if(!LOTRConfig.allowBannerProtection) {
            return false;
        }
        String protectorName = null;
        AxisAlignedBB originCube = AxisAlignedBB.getBoundingBox(i, j, k, i + 1, j + 1, k + 1).expand(searchExtra, searchExtra, searchExtra);
        AxisAlignedBB searchCube = originCube.expand(64.0, 64.0, 64.0);
        List banners = world.getEntitiesWithinAABB(LOTREntityBanner.class, searchCube);
        if(!banners.isEmpty()) {
            for(Object banner2 : banners) {
                ProtectType result;
                LOTREntityBanner banner = (LOTREntityBanner) banner2;
                AxisAlignedBB protectionCube = banner.createProtectionCube();
                if(!banner.isProtectingTerritory() || !protectionCube.intersectsWith(searchCube) || !protectionCube.intersectsWith(originCube) || (result = protectFilter.protects(banner)) == ProtectType.NONE) continue;
                if(result == ProtectType.FACTION) {
                    protectorName = banner.getBannerType().faction.factionName();
                    break;
                }
                if(result == ProtectType.PLAYER_SPECIFIC) {
                    GameProfile placingPlayer = banner.getPlacingPlayer();
                    if(placingPlayer != null) {
                        if(StringUtils.isBlank(placingPlayer.getName())) {
                            MinecraftServer.getServer().func_147130_as().fillProfileProperties(placingPlayer, true);
                        }
                        protectorName = placingPlayer.getName();
                        break;
                    }
                    protectorName = "?";
                    break;
                }
                if(result != ProtectType.STRUCTURE) continue;
                protectorName = StatCollector.translateToLocal("chat.lotr.protectedStructure");
                break;
            }
        }
        if(protectorName != null) {
            if(sendMessage) {
                protectFilter.warnProtection(new ChatComponentTranslation("chat.lotr.protectedLand", protectorName));
            }
            return true;
        }
        return false;
    }

	public enum Permission {
		FULL, DOORS, TABLES, CONTAINERS, FOOD, BEDS, SWITCHES;

		public final int bitFlag = 1 << ordinal();
		public final String codeName = name();

		public static Permission forName(String s) {
			for (Permission p : Permission.values()) {
				if (!p.codeName.equals(s)) {
					continue;
				}
				return p;
			}
			return null;
		}
	}

	public enum ProtectType {
		NONE, FACTION, PLAYER_SPECIFIC, STRUCTURE;

	}

}