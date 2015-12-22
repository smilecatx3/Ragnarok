package ragnarok;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * The class replaces the English monster name to Chinese one.
 *
 * @author Xavier Lin
 */
public class MobTranslator {

    public static void main(String[] args) {
        MobTranslator translator = new MobTranslator();
        translator.translate("data/mob/mob_db.txt", "data/mob/mob_db_tc.txt", "D:/mob_db.txt");
    }



    private Map<String, String> nameTable = new HashMap<>(); // <ID, Chinese Name>

    /**
     * Replaces the monster name to Chinese.
     * @param mob_db The English mob_db.txt
     * @param mob_db_tc The Chinese mob_db.txt
     * @param outputFileName The output file name
     */
    public void translate(
            String mob_db,
            String mob_db_tc,
            String outputFileName) {

        try {
            parseNameTable(mob_db_tc);

            try (BufferedReader reader = new BufferedReader(new FileReader(mob_db));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName), "Big5"))) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line.matches("\\d+,.+")) {
                        String[] cols = line.split(",");
                        if (nameTable.containsKey(cols[0])) {
                            StringBuilder builder = new StringBuilder();
                            builder.append(cols[0]).append(",").append(cols[1]).append(",").append(nameTable.get(cols[0]));
                            for (int i=3; i<cols.length; i++)
                                builder.append(",").append(cols[i]);
                            line = builder.toString();
                        }
                    }
                    writer.write(line);
                    writer.newLine();
                }
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseNameTable(String mob_db_tc) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mob_db_tc), "Big5"))) {
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.matches("\\d+,.+")) {
                    String[] cols = line.split(",");
                    nameTable.put(cols[0], cols[2]);
                }
            }
        }
    }

}
