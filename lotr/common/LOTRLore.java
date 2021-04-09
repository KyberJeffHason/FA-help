package lotr.common;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Charsets;
import cpw.mods.fml.common.ModContainer;
import lotr.common.entity.npc.LOTRNames;
import lotr.common.util.LOTRLog;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.MathHelper;

public class LOTRLore {
    private static final String newline = "\n";
    private static final String codeMetadata = "#";
    private static final String codeTitle = "title:";
    private static final String codeAuthor = "author:";
    private static final String codeCategory = "types:";
    private static final String codeCategorySeparator = ",";
    private static final String codeReward = "reward";
    public final String loreName;
    public final String loreTitle;
    public final String loreAuthor;
    public final String loreText;
    public final List<LoreCategory> loreCategories;
    public final boolean isRewardable;

    public static LOTRLore getMultiRandomLore(List<LoreCategory> categories, Random random, boolean rewardsOnly) {
        ArrayList<LOTRLore> allLore = new ArrayList<LOTRLore>();
        for(LoreCategory c : categories) {
            for(LOTRLore lore : c.loreList) {
                if(allLore.contains(lore) || rewardsOnly && !lore.isRewardable) continue;
                allLore.add(lore);
            }
        }
        if(!allLore.isEmpty()) {
            return allLore.get(random.nextInt(allLore.size()));
        }
        return null;
    }

    public static void loadAllLore() {
        HashMap<String, BufferedReader> loreReaders = new HashMap<String, BufferedReader>();
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
                    if(!s.startsWith(path = "assets/lotr/lore/") || !s.endsWith(".txt")) continue;
                    s = s.substring(path.length());
                    int i = s.indexOf(".txt");
                    try {
                        s = s.substring(0, i);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new BOMInputStream(zip.getInputStream(entry)), Charsets.UTF_8.name()));
                        loreReaders.put(s, reader);
                    }
                    catch(Exception e) {
                        LOTRLog.logger.error("Failed to load LOTR lore " + s + "from zip file");
                        e.printStackTrace();
                    }
                }
            }
            else {
                File nameBankDir = new File(LOTRMod.class.getResource("/assets/lotr/lore").toURI());
                for(File file : nameBankDir.listFiles()) {
                    String s = file.getName();
                    int i = s.indexOf(".txt");
                    if(i < 0) {
                        LOTRLog.logger.error("Failed to load LOTR lore " + s + " from MCP folder; name bank files must be in .txt format");
                        continue;
                    }
                    try {
                        s = s.substring(0, i);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(file)), Charsets.UTF_8.name()));
                        loreReaders.put(s, reader);
                    }
                    catch(Exception e) {
                        LOTRLog.logger.error("Failed to load LOTR lore " + s + " from MCP folder");
                        e.printStackTrace();
                    }
                }
            }
        }
        catch(Exception e) {
            LOTRLog.logger.error("Failed to load LOTR lore");
            e.printStackTrace();
        }
        for(Map.Entry entry : loreReaders.entrySet()) {
            String loreName = (String) entry.getKey();
            BufferedReader reader = (BufferedReader) entry.getValue();
            try {
                Object categoryString;
                String line;
                String title = "";
                String author = "";
                ArrayList<LoreCategory> categories = new ArrayList<LoreCategory>();
                String text = "";
                boolean reward = false;
                while((line = reader.readLine()) != null) {
                    if(line.startsWith(codeMetadata)) {
                        String metadata = line.substring(codeMetadata.length());
                        if(metadata.startsWith(codeTitle)) {
                            title = metadata.substring(codeTitle.length());
                            continue;
                        }
                        if(metadata.startsWith(codeAuthor)) {
                            author = metadata.substring(codeAuthor.length());
                            continue;
                        }
                        if(metadata.startsWith(codeCategory)) {
                            categoryString = metadata.substring(codeCategory.length());
                            while(((String) categoryString).length() > 0) {
                                Object categoryName = null;
                                int indexOf = ((String) categoryString).indexOf(codeCategorySeparator);
                                if(indexOf >= 0) {
                                    categoryName = ((String) categoryString).substring(0, indexOf);
                                    categoryString = ((String) categoryString).substring(indexOf + 1);
                                }
                                else {
                                    categoryName = categoryString;
                                    categoryString = "";
                                }
                                if(categoryName == null) continue;
                                if(((String) categoryName).equals("all")) {
                                    for(LoreCategory category : LoreCategory.values()) {
                                        if(categories.contains(category)) continue;
                                        categories.add(category);
                                    }
                                    continue;
                                }
                                LoreCategory category = LoreCategory.forName((String) categoryName);
                                if(category != null) {
                                    if(categories.contains(category)) continue;
                                    categories.add(category);
                                    continue;
                                }
                                LOTRLog.logger.warn("LOTRLore: Loading lore " + loreName + ", no category exists for name " + (String) categoryName);
                            }
                            continue;
                        }
                        if(!metadata.startsWith(codeReward)) continue;
                        reward = true;
                        continue;
                    }
                    text = text + line;
                    text = text + newline;
                }
                reader.close();
                LOTRLore lore = new LOTRLore(loreName, title, author, text, categories, reward);
                Iterator<LoreCategory> categoryString1 = categories.iterator();
                while(categoryString1.hasNext()) {
                    LoreCategory category = (categoryString1.next());
                    category.addLore(lore);
                }
            }
            catch(Exception e) {
                LOTRLog.logger.error("Failed to load LOTR lore: " + loreName);
                e.printStackTrace();
            }
        }
        for(LoreCategory category : LoreCategory.values()) {
            int num = category.loreList.size();
            int numReward = 0;
            for(LOTRLore lore : category.loreList) {
                if(!lore.isRewardable) continue;
                ++numReward;
            }
            LOTRLog.logger.info("LOTRLore: Category " + category.categoryName + " has loaded " + num + " lore texts, of which " + numReward + " rewardable");
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

    public LOTRLore(String name, String title, String auth, String text, List<LoreCategory> categories, boolean reward) {
        this.loreName = name;
        this.loreTitle = title;
        this.loreAuthor = auth;
        this.loreText = text;
        this.loreCategories = categories;
        this.isRewardable = reward;
    }

    private static List<String> organisePages(String loreText) {
        ArrayList<String> loreTextPages = new ArrayList<String>();
        String remainingText = loreText;
        ArrayList<String> splitTxtWords = new ArrayList<String>();
        while(remainingText.length() > 0) {
            String[] splitWords;
            String part;
            if(remainingText.startsWith(newline)) {
                part = newline;
                if(!splitTxtWords.isEmpty()) {
                    splitTxtWords.add(part);
                }
                remainingText = remainingText.substring(part.length());
                continue;
            }
            part = "";
            int indexOf = remainingText.indexOf(newline);
            part = indexOf >= 0 ? remainingText.substring(0, indexOf) : remainingText;
            for(String word : splitWords = StringUtils.split(part, " ")) {
                splitTxtWords.add(word);
            }
            remainingText = remainingText.substring(part.length());
        }
        int pageLengthMax = 256;
        int maxLines = 13;
        int avgLineLength = 17;
        int page = 0;
        while(!splitTxtWords.isEmpty()) {
            int i;
            String pageText = "";
            int numLines = 0;
            String currentLine = "";
            int usedWords = 0;
            for(i = 0; i < splitTxtWords.size(); ++i) {
                String word = splitTxtWords.get(i);
                if(pageText.length() + word.length() > 256) break;
                if(word.equals(newline)) {
                    if(currentLine.length() > 0) {
                        pageText = pageText + currentLine;
                        currentLine = "";
                        if(++numLines >= 13) break;
                    }
                    ++usedWords;
                    if(pageText.length() <= 0) continue;
                    pageText = pageText + word;
                    if(++numLines < 13) continue;
                    break;
                }
                currentLine = currentLine + word;
                ++usedWords;
                if(i < splitTxtWords.size() - 1) {
                    currentLine = currentLine + " ";
                }
                if(currentLine.length() < 17) continue;
                pageText = pageText + currentLine;
                currentLine = "";
                if(++numLines >= 13) break;
            }
            if(currentLine.length() > 0) {
                pageText = pageText + currentLine;
                currentLine = "";
                ++numLines;
            }
            for(i = 0; i < usedWords; ++i) {
                splitTxtWords.remove(0);
            }
            loreTextPages.add(pageText);
            ++page;
        }
        return loreTextPages;
    }

    private String formatRandom(String text, Random random) {
        int lastIndexStart = -1;
        do {
            String formatted;
            String unformatted;
            block16: {
                String s1;
                int indexStart = text.indexOf("{", lastIndexStart + 1);
                int indexEnd = text.indexOf("}");
                lastIndexStart = indexStart;
                if(indexStart < 0 || indexEnd <= indexStart) break;
                unformatted = text.substring(indexStart, indexEnd + 1);
                formatted = unformatted.substring(1, unformatted.length() - 1);
                if(formatted.startsWith("num:")) {
                    try {
                        s1 = formatted.substring("num:".length());
                        int i1 = s1.indexOf(codeCategorySeparator);
                        String s2 = s1.substring(0, i1);
                        String s3 = s1.substring(i1 + codeCategorySeparator.length());
                        int min = Integer.parseInt(s2);
                        int max = Integer.parseInt(s3);
                        int number = MathHelper.getRandomIntegerInRange(random, min, max);
                        formatted = String.valueOf(number);
                    }
                    catch(Exception e) {
                        LOTRLog.logger.error("LOTRLore: Error formatting number " + unformatted + " in text: " + this.loreName);
                        e.printStackTrace();
                    }
                }
                else if(formatted.startsWith("name:")) {
                    try {
                        String name;
                        String namebank = s1 = formatted.substring("name:".length());
                        if(!LOTRNames.nameBankExists(namebank)) {
                            LOTRLog.logger.error("LOTRLore: No namebank exists for " + namebank + "!");
                            break block16;
                        }
                        formatted = name = LOTRNames.getRandomName(namebank, random);
                    }
                    catch(Exception e) {
                        LOTRLog.logger.error("LOTRLore: Error formatting name " + unformatted + " in text: " + this.loreName);
                        e.printStackTrace();
                    }
                }
                else if(formatted.startsWith("choose:")) {
                    try {
                        String remaining = formatted.substring("choose:".length());
                        ArrayList<String> words = new ArrayList<String>();
                        while(remaining.length() > 0) {
                            String word;
                            int indexOf = remaining.indexOf("/");
                            if(indexOf >= 0) {
                                word = remaining.substring(0, indexOf);
                                remaining = remaining.substring(indexOf + "/".length());
                            }
                            else {
                                word = remaining;
                                remaining = "";
                            }
                            words.add(word);
                        }
                        formatted = words.get(random.nextInt(words.size()));
                    }
                    catch(Exception e) {
                        LOTRLog.logger.error("LOTRLore: Error formatting choice " + unformatted + " in text: " + this.loreName);
                        e.printStackTrace();
                    }
                }
            }
            text = Pattern.compile(unformatted, 16).matcher(text).replaceFirst(Matcher.quoteReplacement(formatted));
        }
        while(true);
        return text;
    }

    public ItemStack createLoreBook(Random random) {
        ItemStack itemstack = new ItemStack(Items.written_book);
        NBTTagCompound data = new NBTTagCompound();
        itemstack.setTagCompound(data);
        String title = this.formatRandom(this.loreTitle, random);
        String author = this.formatRandom(this.loreAuthor, random);
        String text = this.formatRandom(this.loreText, random);
        List<String> textPages = LOTRLore.organisePages(text);
        data.setString("title", title);
        data.setString("author", author);
        NBTTagList pages = new NBTTagList();
        for(String pageText : textPages) {
            pages.appendTag(new NBTTagString(pageText));
        }
        data.setTag("pages", pages);
        return itemstack;
    }

    public static enum LoreCategory {
        RUINS("ruins"),
        SHIRE("shire"),
        BLUE_MOUNTAINS("blue_mountains"),
        LINDON("lindon"),
        ERIADOR("eriador"),
        RIVENDELL("rivendell"),
        EREGION("eregion"),
        DUNLAND("dunland"),
        GUNDABAD("gundabad"),
        ANGMAR("angmar"),
        WOODLAND_REALM("woodland_realm"),
        DOL_GULDUR("dol_guldur"),
        DALE("dale"),
        DURIN("durins_folk"),
        LOTHLORIEN("lothlorien"),
        ROHAN("rohan"),
        ISENGARD("isengard"),
        GONDOR("gondor"),
        MORDOR("mordor"),
        DORWINION("dorwinion"),
        RHUN("rhun"),
        HARNEDOR("harnedor"),
        SOUTHRON("southron"),
        UMBAR("umbar"),
        NOMAD("nomad"),
        GULF("gulf"),
        FAR_HARAD("far_harad"),
        FAR_HARAD_JUNGLE("far_harad_jungle"),
        HALF_TROLL("half_troll");

        public static final String allCode = "all";
        public final String categoryName;
        private List<LOTRLore> loreList = new ArrayList<LOTRLore>();

        private LoreCategory(String s) {
            this.categoryName = s;
        }

        private void addLore(LOTRLore lore) {
            this.loreList.add(lore);
        }

        private LOTRLore getRandomLore(Random random) {
            return this.loreList.get(random.nextInt(this.loreList.size()));
        }

        public static LoreCategory forName(String s) {
            for(LoreCategory r : LoreCategory.values()) {
                if(!s.equalsIgnoreCase(r.categoryName)) continue;
                return r;
            }
            return null;
        }
    }

}
