package ragnarok.ds;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The class is to retrieve item description data from <a href="http://fharr.com/">http://fharr.com/</a>.
 * All rights of the retrieved data belong to this site.
 *
 * @author Xavier Lin
 */
public class DSItemDataRetriever {

    public static void main(String[] args) {
        Set<Integer> itemIDs = new TreeSet<>();

        // Prepare input
        try (BufferedReader reader = new BufferedReader(new FileReader("data/itemInfo/idnum2itemdisplaynametable.txt"))) {
            Pattern pattern = Pattern.compile("(\\d+)#.+#");
            while (reader.ready()) {
                Matcher matcher = pattern.matcher(reader.readLine());
                if (matcher.matches())
                    itemIDs.add(Integer.parseInt(matcher.group(1)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        itemIDs.add(501);

        DSItemDataRetriever retriever = new DSItemDataRetriever();
        retriever.retrieve(itemIDs, "data/idnum2itemdesctable(ds_ver).txt");
    }


    /**
     * Retrieves item description.
     * @param itemIDs The desired items to retrieve
     * @param outputFileName The output file name
     */
    public void retrieve(Set<Integer> itemIDs, String outputFileName) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName), "MS950"))) {
            int count = 0;
            for (Integer itemID : itemIDs) {
                try {
                    System.out.print(String.format("Retrieve %d ... ", itemID));
                    String itemDescription = download(itemID);
                    if (itemDescription.length() > 0)
                        writer.write(String.format("%d#%n%s%n#%n", itemID, itemDescription));
                    System.out.println(String.format("done (%d/%d)", ++count, itemIDs.size()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Downloads item description page.
     * @param itemID The item's id
     * @return The item description
     * @throws IOException
     */
    private String download(int itemID) throws IOException {
        StringBuilder data = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(String.format("http://rd.fharr.com/item-%d.html", itemID)).openStream()))) {
            String line;
            while ( ((line = reader.readLine()) != null) && !line.contains("404.php") )
                if (line.contains("FT_Desc Begin"))
                    while (!(line = reader.readLine()).contains("FT_Desc End"))
                        data.append(line);
        }
        return parse(data.toString());
    }

    /**
     * Parses the item's description page and retrieves the desired part.
     * @param data The item's description page content
     * @return The parsed data
     * @throws UnsupportedEncodingException
     */
    private String parse(String data) throws UnsupportedEncodingException {
        Pattern pattern = Pattern.compile(".+left;\">(.+)</p>.+");
        Matcher matcher = pattern.matcher(data);
        if (matcher.matches()) {
            data = matcher.group(1);
            pattern = Pattern.compile(".*<span style=\"color:#(\\p{Alnum}{6})\">.*");
            while ((data.contains("<span"))) {
                matcher = pattern.matcher(data);
                if (matcher.matches())
                    data = data.replaceFirst("<span style=\"color:#(\\p{Alnum}{6})\">", "^"+matcher.group(1));
            }
            return data.replace("<p>", "").replace("</p>", "\r\n").replace("</span>", "^000000");
        } else {
            return "";
        }
    }

}
