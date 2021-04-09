package lotr.common.command;

import java.util.*;
import org.apache.commons.lang3.tuple.Pair;
import lotr.common.world.structure2.scan.LOTRStructureScan;
import net.minecraft.block.Block;
import net.minecraft.command.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class LOTRCommandStrScan extends CommandBase {
    private boolean scanning = false;
    private Map<Block, String> blockAliases = new HashMap<Block, String>();
    private Map<Pair<Block, Integer>, String> blockMetaAliases = new HashMap<Pair<Block, Integer>, String>();
    private int originX;
    private int originY;
    private int originZ;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;

    @Override
    public String getCommandName() {
        return "strscan";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "development command";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if(args.length >= 1) {
            String option = args[0];
            if(option.equals("begin")) {
                if(!this.scanning) {
                    this.scanning = true;
                    this.blockAliases.clear();
                    this.blockMetaAliases.clear();
                    CommandBase.func_152373_a(sender, this, "Begun scanning", new Object[0]);
                    return;
                }
                throw new WrongUsageException("Already begun scanning", new Object[0]);
            }
            if(option.equals("assoc") && args.length >= 3 && this.scanning) {
                String blockID = args[1];
                Block block = Block.getBlockFromName(blockID);
                if(block != null) {
                    String alias = args[2];
                    if(!this.blockAliases.containsValue(alias)) {
                        this.blockAliases.put(block, alias);
                        CommandBase.func_152373_a(sender, this, "Associated block %s to alias %s", blockID, alias);
                        return;
                    }
                    throw new WrongUsageException("Alias %s already used", alias);
                }
                throw new WrongUsageException("Block %s does not exist", blockID);
            }
            if(option.equals("assoc_meta") && args.length >= 4 && this.scanning) {
                String blockID = args[1];
                Block block = Block.getBlockFromName(blockID);
                if(block != null) {
                    int meta = CommandBase.parseInt(sender, args[2]);
                    if(meta >= 0 && meta <= 15) {
                        String alias = args[3];
                        if(!this.blockMetaAliases.containsValue(alias)) {
                            this.blockMetaAliases.put(Pair.of(block, meta), alias);
                            CommandBase.func_152373_a(sender, this, "Associated block %s and metadata %s to alias %s", blockID, meta, alias);
                            return;
                        }
                        throw new WrongUsageException("Alias %s already used", alias);
                    }
                    throw new WrongUsageException("Invalid metadata value %s", meta);
                }
                throw new WrongUsageException("Block %s does not exist", blockID);
            }
            if(option.equals("origin") && args.length >= 4 && this.scanning) {
                ChunkCoordinates coords = sender.getPlayerCoordinates();
                int i = coords.posX;
                int j = coords.posY;
                int k = coords.posZ;
                i = MathHelper.floor_double(CommandBase.func_110666_a(sender, i, args[1]));
                j = MathHelper.floor_double(CommandBase.func_110666_a(sender, j, args[2]));
                k = MathHelper.floor_double(CommandBase.func_110666_a(sender, k, args[3]));
                this.maxX = this.originX = i;
                this.minX = this.originX;
                this.maxY = this.originY = j;
                this.minY = this.originY;
                this.maxZ = this.originZ = k;
                this.minZ = this.originZ;
                CommandBase.func_152373_a(sender, this, "Set scan origin to %s %s %s", this.originX, this.originY, this.originZ);
                return;
            }
            if(option.equals("expand") && args.length >= 4 && this.scanning) {
                ChunkCoordinates coords = sender.getPlayerCoordinates();
                int i = coords.posX;
                int j = coords.posY;
                int k = coords.posZ;
                i = MathHelper.floor_double(CommandBase.func_110666_a(sender, i, args[1]));
                j = MathHelper.floor_double(CommandBase.func_110666_a(sender, j, args[2]));
                k = MathHelper.floor_double(CommandBase.func_110666_a(sender, k, args[3]));
                this.minX = Math.min(i, this.minX);
                this.minY = Math.min(j, this.minY);
                this.minZ = Math.min(k, this.minZ);
                this.maxX = Math.max(i, this.maxX);
                this.maxY = Math.max(j, this.maxY);
                this.maxZ = Math.max(k, this.maxZ);
                CommandBase.func_152373_a(sender, this, "Expanded scan region to include %s %s %s", i, j, k);
                return;
            }
            if(option.equals("scan") && args.length >= 2 && this.scanning) {
                String scanName = args[1];
                LOTRStructureScan scan = new LOTRStructureScan(scanName);
                World world = sender.getEntityWorld();
                for(int j = this.minY; j <= this.maxY; ++j) {
                    for(int k = this.minZ; k <= this.maxZ; ++k) {
                        for(int i = this.minX; i <= this.maxX; ++i) {
                            String alias;
                            int i1 = i - this.originX;
                            int j1 = j - this.originY;
                            int k1 = k - this.originZ;
                            Block block = world.getBlock(i, j, k);
                            int meta = world.getBlockMetadata(i, j, k);
                            boolean fillBelow = false;
                            if(block == Blocks.air || block == Blocks.bedrock) continue;
                            if(world.getBlock(i, j - 1, k) == Blocks.bedrock) {
                                fillBelow = true;
                            }
                            LOTRStructureScan.ScanStepBase step = null;
                            if(this.blockMetaAliases.containsKey(Pair.of(block, meta))) {
                                alias = this.blockMetaAliases.get(Pair.of(block, meta));
                                step = new LOTRStructureScan.ScanStepBlockMetaAlias(i1, j1, k1, alias);
                                scan.includeBlockMetaAlias(alias);
                            }
                            else if(this.blockAliases.containsKey(block)) {
                                alias = this.blockAliases.get(block);
                                step = new LOTRStructureScan.ScanStepBlockAlias(i1, j1, k1, alias, meta);
                                scan.includeBlockAlias(alias);
                            }
                            else {
                                step = new LOTRStructureScan.ScanStep(i1, j1, k1, block, meta);
                            }
                            step.fillDown = fillBelow;
                            scan.addScanStep(step);
                        }
                    }
                }
                if(LOTRStructureScan.writeScanToFile(scan)) {
                    this.scanning = false;
                    CommandBase.func_152373_a(sender, this, "Scanned structure as %s", scanName);
                    return;
                }
                throw new WrongUsageException("Error scanning structure as %s", scanName);
            }
        }
        throw new WrongUsageException(this.getCommandUsage(sender), new Object[0]);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int i) {
        return false;
    }
}
