package ragnarok;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The class creates itemInfo.lua file.
 *
 * @author Xavier Lin
 */
public class ItemInfoCreator {

    public static void main(String[] args) throws Exception {
        ItemInfoCreator creator = new ItemInfoCreator();
        creator.create("data/converted/itemInfo.lua");
    }



    private class ItemInfo {
        String displayName;
        String resourceName = "사과";
        List<String> description = new ArrayList<>();
        int slotCount = 0;
        int classNum = 0;
        ItemInfo(String displayName) { this.displayName = displayName; }
    }
    private Map<Integer, ItemInfo> itemInfoTable = new TreeMap<>(); // <ID, ItemInfo>

    /**
     * Uses the default data to create an ItemInfoCreator instance.
     * @throws IOException
     */
    public ItemInfoCreator() throws IOException {
        this(
            "data/itemInfo/idnum2itemdisplaynametable.txt",
            "data/itemInfo/idnum2itemresnametable.txt",
            "data/itemInfo/idnum2itemdesctable(ds_ver).txt",
            "data/itemInfo/itemslotcounttable.txt",
            "data/itemInfo/item_db.txt"
        );
    }

    /**
     * Creates and initializes an ItemInfoCreator instance given the required data.
     * @param itemDisplayNameTable idnum2itemdisplaynametable.txt
     * @param itemResourceNameTable idnum2itemresnametable.txt
     * @param itemDescriptionTable idnum2itemdesctable.txt
     * @param itemSlotCountTable itemslotcounttable.txt (for 'slotCount' attribute)
     * @param itemDB item_db.txt (for 'ClassNum' attribute)
     * @throws IOException
     */
    public ItemInfoCreator(
            String itemDisplayNameTable,
            String itemResourceNameTable,
            String itemDescriptionTable,
            String itemSlotCountTable,
            String itemDB) throws IOException {

        System.out.println("Initializing ...");

        initTable(itemDisplayNameTable);

        ExecutorService[] executorServices = new ExecutorService[4];
        for (int i=0; i<executorServices.length; i++)
            executorServices[i] = Executors.newSingleThreadExecutor();

        try {
            Future[] futures = new Future[4];
            futures[0] = executorServices[0].submit(() -> { loadResourceTable(itemResourceNameTable); return null; });
            futures[1] = executorServices[1].submit(() -> { loadDescriptionTable(itemDescriptionTable); return null; });
            futures[2] = executorServices[2].submit(() -> { loadSlotCountTable(itemSlotCountTable); return null; });
            futures[3] = executorServices[3].submit(() -> { loadItemDB(itemDB); return null; });

            for (Future future : futures)
                future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        for (ExecutorService service : executorServices)
            service.shutdownNow();
    }

    /**
     * Creates itemInfo.lua file
     * @param outputFileName The desired output file name (.lua)
     */
    public void create(String outputFileName) {
        if (!outputFileName.endsWith(".lua"))
            outputFileName += ".lua";
        System.out.print(String.format("Creating '%s' ... ", outputFileName));

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName), "MS950"))) {
            writer.write("tbl = {\r\n");
            for (Map.Entry<Integer, ItemInfo> entry : itemInfoTable.entrySet()) {
                ItemInfo itemInfo = entry.getValue();

                StringBuilder description = new StringBuilder();
                for (String line : itemInfo.description)
                    description.append(String.format("\t\t\t\"%s\",%n", line));
                if (description.length() > 0)
                    description.deleteCharAt(description.lastIndexOf(","));

                writer.write(String.format("\t[%d] = {%n", entry.getKey()));

                writer.write(String.format("\t\tunidentifiedDisplayName = \"%s(未鑑定)\",%n", itemInfo.displayName));
                writer.write(String.format("\t\tunidentifiedResourceName = \"%s\",%n", itemInfo.resourceName));
                writer.write(String.format("\t\tunidentifiedDescriptionName = {%n%s\t\t},%n", description));

                writer.write(String.format("\t\tidentifiedDisplayName = \"%s\",%n", itemInfo.displayName));
                writer.write(String.format("\t\tidentifiedResourceName = \"%s\",%n", itemInfo.resourceName));
                writer.write(String.format("\t\tidentifiedDescriptionName = {%n%s\t\t},%n", description));

                writer.write(String.format("\t\tslotCount = %d,%n", itemInfo.slotCount));
                writer.write(String.format("\t\tClassNum = %d%n\t},%n%n", itemInfo.classNum));
            }
            writer.write("}\r\n");
            writer.write(FileUtils.readFileToString(new File("data/itemInfo/itemInfo.lua.sample")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Finished");
    }
    
    private void initTable(String itemDisplayNameTable) throws IOException {
        System.out.println(String.format("- Loading '%s' ...", itemDisplayNameTable));

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(itemDisplayNameTable), "MS950"))) {
            Pattern pattern = Pattern.compile("(\\d+)#(.+)#");
            while (reader.ready()) {
                Matcher matcher = pattern.matcher(reader.readLine());
                if (matcher.matches())
                    itemInfoTable.put(Integer.parseInt(matcher.group(1)), new ItemInfo(Utils.fixEncoding(matcher.group(2))));
            }
        }
    }

    private void loadResourceTable(String itemResourceNameTable) throws IOException {
        System.out.println(String.format("- Loading '%s' ...", itemResourceNameTable));

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(itemResourceNameTable), "MS950"))) {
            Pattern pattern = Pattern.compile("(\\d+)#(.+)#");
            while (reader.ready()) {
                Matcher matcher = pattern.matcher(reader.readLine());
                if (matcher.matches()) {
                    int itemID = Integer.parseInt(matcher.group(1));
                    if (itemInfoTable.containsKey(itemID))
                        itemInfoTable.get(itemID).resourceName = matcher.group(2);
                }
            }
        }
    }

    private void loadDescriptionTable(String itemDescriptionTable) throws IOException {
        System.out.println(String.format("- Loading '%s' ...", itemDescriptionTable));

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(itemDescriptionTable), "MS950"))) {
            Pattern pattern = Pattern.compile("(\\d+)#");
            while (reader.ready()) {
                Matcher matcher = pattern.matcher(reader.readLine());
                if (matcher.matches()) {
                    ItemInfo info = itemInfoTable.get(Integer.parseInt(matcher.group(1)));
                    if (info == null)
                        continue;
                    String line;
                    while (!(line = reader.readLine()).equals("#"))
                        info.description.add(Utils.fixEncoding(line).replace("\"", "\\\""));
                }
            }
        }
    }

    private void loadSlotCountTable(String itemSlotCountTable) throws IOException {
        System.out.println(String.format("- Loading '%s' ...", itemSlotCountTable));

        try (BufferedReader reader = new BufferedReader(new FileReader(itemSlotCountTable))) {
            Pattern pattern = Pattern.compile("(\\d+)#(\\d+)#");
            while (reader.ready()) {
                Matcher matcher = pattern.matcher(reader.readLine());
                if (matcher.matches()) {
                    ItemInfo info = itemInfoTable.get(Integer.parseInt(matcher.group(1)));
                    if (info != null)
                        info.slotCount = Integer.parseInt(matcher.group(2));
                }
            }
        }
    }

    private void loadItemDB(String itemDB) throws IOException {
        System.out.println(String.format("- Loading '%s' ...", itemDB));

        try (BufferedReader reader = new BufferedReader(new FileReader(itemDB))) {
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.matches("\\d+,.+")) {
                    String[] tokens = line.split(",");
                    ItemInfo info = itemInfoTable.get(Integer.parseInt(tokens[0]));
                    if ( (info != null) && (tokens[18].length() > 0) )
                        info.classNum = Integer.parseInt(tokens[18]); // View column
                }
            }
        }
    }
}
