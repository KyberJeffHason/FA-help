package lotr.common.block;

import java.util.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.tileentity.LOTRTileEntityDwarvenDoor;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class LOTRBlockGateDwarvenIthildin extends LOTRBlockGateDwarven {
    @Override
    public boolean hasTileEntity(int meta) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new LOTRTileEntityDwarvenDoor();
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconregister) {
        super.registerBlockIcons(iconregister);
        for(DoorSize s : DoorSize.values()) {
            for(int i = 0; i < s.width; ++i) {
                for(int j = 0; j < s.height; ++j) {
                    IIcon icon;
                    s.icons[i][j] = icon = iconregister.registerIcon(this.getTextureName() + "_glow_" + s.doorName + "_" + i + "_" + j);
                }
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entity, ItemStack itemstack) {
        super.onBlockPlacedBy(world, i, j, k, entity, itemstack);
        LOTRTileEntityDwarvenDoor door = (LOTRTileEntityDwarvenDoor) world.getTileEntity(i, j, k);
        if(door != null) {
            int meta = world.getBlockMetadata(i, j, k);
            int dir = LOTRBlockGate.getGateDirection(meta);
            door.setDoorSizeAndPos(null, 0, 0);
            door.setDoorBasePos(i, j, k);
            if(dir != 0 && dir != 1) {
                int xzFactorX = dir == 2 ? 1 : (xzFactorX = dir == 3 ? -1 : 0);
                int xzFactorZ = dir == 4 ? -1 : (dir == 5 ? 1 : 0);
                block0: for(DoorSize doorSize : DoorSize.orderedSizes) {
                    int width = doorSize.width;
                    int height = doorSize.height;
                    int rangeXZ = width - 1;
                    int rangeY = height - 1;
                    for(int y = -rangeY; y <= 0; ++y) {
                        for(int xz = -rangeXZ; xz <= 0; ++xz) {
                            int i2;
                            int k2;
                            int j2;
                            int y1;
                            TileEntity te;
                            LOTRTileEntityDwarvenDoor otherDoor;
                            int xz1;
                            int j1 = j + y;
                            int i1 = i + xz * xzFactorX;
                            int k1 = k + xz * xzFactorZ;
                            boolean connected = true;
                            boolean canReplaceSize = true;
                            block3: for(y1 = 0; y1 <= rangeY; ++y1) {
                                for(xz1 = 0; xz1 <= rangeXZ; ++xz1) {
                                    DoorSize otherSize;
                                    j2 = j1 + y1;
                                    i2 = i1 + xz1 * xzFactorX;
                                    k2 = k1 + xz1 * xzFactorZ;
                                    if(i2 == i && j2 == j && k2 == k) continue;
                                    if(!this.areDwarfDoorsMatching(world, i, j, k, i2, j2, k2)) {
                                        connected = false;
                                        break block3;
                                    }
                                    te = world.getTileEntity(i2, j2, k2);
                                    if(!(te instanceof LOTRTileEntityDwarvenDoor) || DoorSize.compareLarger.compare(otherSize = (otherDoor = (LOTRTileEntityDwarvenDoor) te).getDoorSize(), doorSize) == 1) continue;
                                    canReplaceSize = false;
                                    break block3;
                                }
                            }
                            if(!connected || !canReplaceSize) continue;
                            door.setDoorSizeAndPos(doorSize, -xz, -y);
                            door.setDoorBasePos(i, j, k);
                            for(y1 = 0; y1 <= rangeY; ++y1) {
                                for(xz1 = 0; xz1 <= rangeXZ; ++xz1) {
                                    j2 = j1 + y1;
                                    i2 = i1 + xz1 * xzFactorX;
                                    k2 = k1 + xz1 * xzFactorZ;
                                    if(i2 == i && j2 == j && k2 == k || !((te = world.getTileEntity(i2, j2, k2)) instanceof LOTRTileEntityDwarvenDoor)) continue;
                                    otherDoor = (LOTRTileEntityDwarvenDoor) te;
                                    otherDoor.setDoorSizeAndPos(doorSize, xz1, y1);
                                    otherDoor.setDoorBasePos(i, j, k);
                                }
                            }
                            break block0;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void breakBlock(World world, int i, int j, int k, Block block, int meta) {
        DoorSize doorSize;
        int dir;
        LOTRTileEntityDwarvenDoor door = (LOTRTileEntityDwarvenDoor) world.getTileEntity(i, j, k);
        if(door != null && (doorSize = door.getDoorSize()) != null && (dir = LOTRBlockGate.getGateDirection(meta)) != 0 && dir != 1) {
            int xzFactorX = dir == 2 ? 1 : (xzFactorX = dir == 3 ? -1 : 0);
            int xzFactorZ = dir == 4 ? -1 : (dir == 5 ? 1 : 0);
            int width = doorSize.width;
            int height = doorSize.height;
            int rangeXZ = width - 1;
            int rangeY = height - 1;
            for(int y = -rangeY; y <= rangeY; ++y) {
                for(int xz = -rangeXZ; xz <= rangeXZ; ++xz) {
                    TileEntity te;
                    LOTRTileEntityDwarvenDoor otherDoor;
                    int j1 = j + y;
                    int i1 = i + xz * xzFactorX;
                    int k1 = k + xz * xzFactorZ;
                    if(i1 == i && j1 == j && k1 == k || !((te = world.getTileEntity(i1, j1, k1)) instanceof LOTRTileEntityDwarvenDoor) || !(otherDoor = (LOTRTileEntityDwarvenDoor) te).isSameDoor(door)) continue;
                    otherDoor.setDoorSizeAndPos(null, 0, 0);
                }
            }
        }
        super.breakBlock(world, i, j, k, block, meta);
    }

    public IIcon getGlowIcon(IBlockAccess world, int i, int j, int k, int side) {
        TileEntity te;
        int meta = world.getBlockMetadata(i, j, k);
        int dir = LOTRBlockGate.getGateDirection(meta);
        boolean open = LOTRBlockGate.isGateOpen(world, i, j, k);
        if(!open && dir == Facing.oppositeSide[side] && (te = world.getTileEntity(i, j, k)) instanceof LOTRTileEntityDwarvenDoor) {
            LOTRTileEntityDwarvenDoor door = (LOTRTileEntityDwarvenDoor) te;
            return door.getDoorSize().icons[door.getDoorPosX()][door.getDoorPosY()];
        }
        return null;
    }

    @Override
    protected boolean directionsMatch(int dir1, int dir2) {
        return dir1 == dir2;
    }

    private boolean areDwarfDoorsMatching(IBlockAccess world, int i, int j, int k, int i1, int j1, int k1) {
        Block block = world.getBlock(i, j, k);
        int meta = world.getBlockMetadata(i, j, k);
        Block otherBlock = world.getBlock(i1, j1, k1);
        int otherMeta = world.getBlockMetadata(i1, j1, k1);
        int dir = LOTRBlockGate.getGateDirection(meta);
        int otherDir = LOTRBlockGate.getGateDirection(otherMeta);
        return block == this && block == otherBlock && ((LOTRBlockGate) block).directionsMatch(dir, otherDir) && ((LOTRBlockGate) otherBlock).directionsMatch(dir, otherDir);
    }

    public static enum DoorSize {
        _1x1("1x1", 1, 1), _1x2("1x2", 1, 2), _2x2("2x2", 2, 2), _2x3("2x3", 2, 3), _3x4("3x4", 3, 4);

        public final String doorName;
        public final int width;
        public final int height;
        public final int area;
        public final IIcon[][] icons;
        public static Comparator<DoorSize> compareLarger;
        public static List<DoorSize> orderedSizes;

        private DoorSize(String s, int i, int j) {
            this.doorName = s;
            this.width = i;
            this.height = j;
            this.area = this.width * this.height;
            this.icons = new IIcon[this.width][this.height];
        }

        public static DoorSize forName(String s) {
            for(DoorSize size : DoorSize.values()) {
                if(!size.doorName.equals(s)) continue;
                return size;
            }
            return null;
        }

        static {
            compareLarger = new Comparator<DoorSize>() {

                @Override
                public int compare(DoorSize s1, DoorSize s2) {
                    if(s1 == s2) {
                        return 0;
                    }
                    if(s1.area != s2.area) {
                        return -Integer.valueOf(s1.area).compareTo(s2.area);
                    }
                    if(s1.height != s2.height) {
                        return -Integer.valueOf(s1.height).compareTo(s2.height);
                    }
                    return -Integer.valueOf(s1.width).compareTo(s2.width);
                }
            };
            orderedSizes = new ArrayList<DoorSize>();
            for(DoorSize s : DoorSize.values()) {
                orderedSizes.add(s);
            }
            Collections.sort(orderedSizes, compareLarger);
        }

    }

}
