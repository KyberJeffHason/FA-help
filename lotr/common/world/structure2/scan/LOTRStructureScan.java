package lotr.common.world.structure2.scan;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.BOMInputStream;
import com.google.common.base.Charsets;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ModContainer;
import lotr.common.LOTRMod;
import lotr.common.util.LOTRLog;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.DimensionManager;

public class LOTRStructureScan {
    private static final String strscanFormat = ".strscan";
    private static Map<String, LOTRStructureScan> allLoadedScans = new HashMap<String, LOTRStructureScan>();
    public final String scanName;
    public final List<ScanStepBase> scanSteps = new ArrayList<ScanStepBase>();
    public final List<String> blockAliases = new ArrayList<String>();
    public final List<String> blockMetaAliases = new ArrayList<String>();

    public static void loadAllScans() {
        allLoadedScans.clear();
        HashMap<String, BufferedReader> scanNamesAndReaders = new HashMap<String, BufferedReader>();
        ZipFile zip = null;
        try {
            ModContainer mc = LOTRMod.getModContainer();
            if(mc.getSource().isFile()) {
                zip = new ZipFile(mc.getSource());
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while(entries.hasMoreElements()) {
                    String path;
                    ZipEntry entry = entries.nextElement();
                    String s = entry.getName();
                    if(!s.startsWith(path = "assets/lotr/strscan/") || !s.endsWith(strscanFormat)) continue;
                    s = s.substring(path.length());
                    int i = s.indexOf(strscanFormat);
                    try {
                        s = s.substring(0, i);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new BOMInputStream(zip.getInputStream(entry)), Charsets.UTF_8.name()));
                        scanNamesAndReaders.put(s, reader);
                    }
                    catch(Exception e) {
                        FMLLog.severe("Failed to load LOTR structure scan " + s + "from zip file", new Object[0]);
                        e.printStackTrace();
                    }
                }
            }
            else {
                File scanDir = new File(LOTRMod.class.getResource("/assets/lotr/strscan").toURI());
                Collection<File> subfiles = FileUtils.listFiles(scanDir, null, true);
                for(File subfile : subfiles) {
                    String s = subfile.getPath();
                    s = s.substring(scanDir.getPath().length() + 1);
                    int i = (s = s.replace(File.separator, "/")).indexOf(strscanFormat);
                    if(i < 0) {
                        FMLLog.severe("Failed to load LOTR structure scan " + s + " from MCP folder - not in " + strscanFormat + " format", new Object[0]);
                        continue;
                    }
                    try {
                        s = s.substring(0, i);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(subfile)), Charsets.UTF_8.name()));
                        scanNamesAndReaders.put(s, reader);
                    }
                    catch(Exception e) {
                        FMLLog.severe("Failed to load LOTR structure scan " + s + " from MCP folder", new Object[0]);
                        e.printStackTrace();
                    }
                }
            }
        }
        catch(Exception e) {
            FMLLog.severe("Failed to load LOTR structure scans", new Object[0]);
            e.printStackTrace();
        }
        for(String strName : scanNamesAndReaders.keySet()) {
            BufferedReader reader = scanNamesAndReaders.get(strName);
            int curLine = 0;
            try {
                String nextLine;
                ArrayList<String> lines = new ArrayList<String>();
                while((nextLine = reader.readLine()) != null) {
                    lines.add(nextLine);
                }
                reader.close();
                if(lines.isEmpty()) {
                    FMLLog.severe("LOTR structure scans " + strName + " is empty!", new Object[0]);
                    continue;
                }
                LOTRStructureScan scan = new LOTRStructureScan(strName);
                HashSet<String> blockAliases = new HashSet<String>();
                HashSet<String> blockMetaAliases = new HashSet<String>();
                for(String line : lines) {
                    String s1;
                    String alias;
                    ++curLine;
                    if(line.length() == 0) continue;
                    if(line.startsWith("#")) {
                        s1 = line.substring(1, line.length() - 1);
                        blockAliases.add(s1);
                        continue;
                    }
                    if(line.startsWith("~")) {
                        s1 = line.substring(1, line.length() - 1);
                        blockMetaAliases.add(s1);
                        continue;
                    }
                    int i = 0;
                    int j = line.indexOf(".");
                    String s12 = line.substring(i, j);
                    int x = Integer.parseInt(s12);
                    ScanStepBase step = null;
                    boolean fillDown = false;
                    boolean findLowest = false;
                    i = j + 1;
                    j = line.indexOf(".", i);
                    s12 = line.substring(i, j);
                    if(s12.endsWith("v")) {
                        fillDown = true;
                        s12 = s12.substring(0, s12.length() - 1);
                    }
                    else if(s12.endsWith("_")) {
                        findLowest = true;
                        s12 = s12.substring(0, s12.length() - 1);
                    }
                    int y = Integer.parseInt(s12);
                    i = j + 1;
                    j = line.indexOf(".", i);
                    s12 = line.substring(i, j);
                    int z = Integer.parseInt(s12);
                    i = j + 1;
                    char c = line.charAt(i);
                    if(c == '\"') {
                        j = line.indexOf("\"", i + 1);
                        s12 = line.substring(i, j + 1);
                        String blockID = s12 = s12.substring(1, s12.length() - 1);
                        Block block = Block.getBlockFromName(blockID);
                        if(block == null) {
                            FMLLog.severe("LOTRStrScan: Block " + blockID + " does not exist!", new Object[0]);
                            block = Blocks.stone;
                        }
                        i = j + 2;
                        j = line.length();
                        s12 = line.substring(i, j);
                        int meta = Integer.parseInt(s12);
                        step = new ScanStep(x, y, z, block, meta);
                    }
                    else if(c == '#') {
                        j = line.indexOf("#", i + 1);
                        s12 = line.substring(i, j + 1);
                        alias = s12 = s12.substring(1, s12.length() - 1);
                        i = j + 2;
                        j = line.length();
                        s12 = line.substring(i, j);
                        int meta = Integer.parseInt(s12);
                        step = new ScanStepBlockAlias(x, y, z, alias, meta);
                    }
                    else if(c == '~') {
                        j = line.indexOf("~", i + 1);
                        s12 = line.substring(i, j + 1);
                        alias = s12 = s12.substring(1, s12.length() - 1);
                        step = new ScanStepBlockMetaAlias(x, y, z, alias);
                    }
                    else if(c == '/') {
                        j = line.indexOf("/", i + 1);
                        s12 = line.substring(i, j + 1);
                        String code = s12 = s12.substring(1, s12.length() - 1);
                        if(code.equals("SKULL")) {
                            step = new ScanStepSkull(x, y, z);
                        }
                    }
                    if(step != null) {
                        step.fillDown = fillDown;
                        step.findLowest = findLowest;
                        scan.addScanStep(step);
                        continue;
                    }
                    throw new IllegalArgumentException("Invalid scan instruction on line " + curLine);
                }
                allLoadedScans.put(scan.scanName, scan);
            }
            catch(Exception e) {
                FMLLog.severe("Failed to load LOTR structure scan " + strName + ": error on line " + curLine, new Object[0]);
                e.printStackTrace();
            }
        }
        if(zip != null) {
            try {
                zip.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean writeScanToFile(LOTRStructureScan scan) {
        File dir = new File(DimensionManager.getCurrentSaveRootDirectory(), "lotr_str_scans");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File scanFile = new File(dir, scan.scanName + strscanFormat);
        try {
            if(!scanFile.exists()) {
                scanFile.createNewFile();
            }
            PrintStream writer = new PrintStream(new FileOutputStream(scanFile));
            if(!scan.blockAliases.isEmpty() || !scan.blockMetaAliases.isEmpty()) {
                for(String alias : scan.blockAliases) {
                    writer.println("#" + alias + "#");
                }
                for(String alias : scan.blockMetaAliases) {
                    writer.println("~" + alias + "~");
                }
                writer.println();
            }
            for(ScanStepBase e : scan.scanSteps) {
                writer.print(e.x);
                writer.print(".");
                writer.print(e.y);
                if(e.fillDown) {
                    writer.print("v");
                }
                writer.print(".");
                writer.print(e.z);
                writer.print(".");
                if(e instanceof ScanStep) {
                    ScanStep step = (ScanStep) e;
                    writer.print("\"");
                    String blockName = Block.blockRegistry.getNameForObject(step.block);
                    if(blockName.startsWith("minecraft:")) {
                        blockName = blockName.substring("minecraft:".length());
                    }
                    writer.print(blockName);
                    writer.print("\"");
                    writer.print(".");
                    writer.print(step.meta);
                    writer.println();
                    continue;
                }
                if(e instanceof ScanStepBlockAlias) {
                    ScanStepBlockAlias step = (ScanStepBlockAlias) e;
                    writer.print("#");
                    writer.print(step.alias);
                    writer.print("#");
                    writer.print(".");
                    writer.print(step.meta);
                    writer.println();
                    continue;
                }
                if(!(e instanceof ScanStepBlockMetaAlias)) continue;
                ScanStepBlockMetaAlias step = (ScanStepBlockMetaAlias) e;
                writer.print("~");
                writer.print(step.alias);
                writer.print("~");
                writer.println();
            }
            writer.close();
            return true;
        }
        catch(Exception e) {
            LOTRLog.logger.error("Error saving strscan file " + scan.scanName);
            e.printStackTrace();
            return false;
        }
    }

    public static LOTRStructureScan getScanByName(String name) {
        return allLoadedScans.get(name);
    }

    public LOTRStructureScan(String name) {
        this.scanName = name;
    }

    public void addScanStep(ScanStepBase e) {
        this.scanSteps.add(e);
    }

    public void includeBlockAlias(String alias) {
        for(String s : this.blockAliases) {
            if(!s.equals(alias)) continue;
            return;
        }
        this.blockAliases.add(alias);
    }

    public void includeBlockMetaAlias(String alias) {
        for(String s : this.blockMetaAliases) {
            if(!s.equals(alias)) continue;
            return;
        }
        this.blockMetaAliases.add(alias);
    }

    public static class ScanStepSkull extends ScanStepBase {
        public ScanStepSkull(int _x, int _y, int _z) {
            super(_x, _y, _z);
        }

        @Override
        public boolean hasAlias() {
            return false;
        }

        @Override
        public String getAlias() {
            return null;
        }

        @Override
        public Block getBlock(Block aliasBlock) {
            return Blocks.skull;
        }

        @Override
        public int getMeta(int aliasMeta) {
            return 1;
        }
    }

    public static class ScanStepBlockMetaAlias extends ScanStepBase {
        public final String alias;

        public ScanStepBlockMetaAlias(int _x, int _y, int _z, String _alias) {
            super(_x, _y, _z);
            this.alias = _alias;
        }

        @Override
        public boolean hasAlias() {
            return true;
        }

        @Override
        public String getAlias() {
            return this.alias;
        }

        @Override
        public Block getBlock(Block aliasBlock) {
            return aliasBlock;
        }

        @Override
        public int getMeta(int aliasMeta) {
            return aliasMeta;
        }
    }

    public static class ScanStepBlockAlias extends ScanStepBase {
        public final String alias;
        public final int meta;

        public ScanStepBlockAlias(int _x, int _y, int _z, String _alias, int _meta) {
            super(_x, _y, _z);
            this.alias = _alias;
            this.meta = _meta;
        }

        @Override
        public boolean hasAlias() {
            return true;
        }

        @Override
        public String getAlias() {
            return this.alias;
        }

        @Override
        public Block getBlock(Block aliasBlock) {
            return aliasBlock;
        }

        @Override
        public int getMeta(int aliasMeta) {
            return this.meta;
        }
    }

    public static class ScanStep extends ScanStepBase {
        public final Block block;
        public final int meta;

        public ScanStep(int _x, int _y, int _z, Block _block, int _meta) {
            super(_x, _y, _z);
            this.block = _block;
            this.meta = _meta;
        }

        @Override
        public boolean hasAlias() {
            return false;
        }

        @Override
        public String getAlias() {
            return null;
        }

        @Override
        public Block getBlock(Block aliasBlock) {
            return this.block;
        }

        @Override
        public int getMeta(int aliasMeta) {
            return this.meta;
        }
    }

    public static abstract class ScanStepBase {
        public final int x;
        public final int y;
        public final int z;
        public boolean fillDown = false;
        public boolean findLowest = false;

        public ScanStepBase(int _x, int _y, int _z) {
            this.x = _x;
            this.y = _y;
            this.z = _z;
        }

        public abstract boolean hasAlias();

        public abstract String getAlias();

        public abstract Block getBlock(Block var1);

        public abstract int getMeta(int var1);
    }

}
