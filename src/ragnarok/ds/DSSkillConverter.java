package ragnarok.ds;

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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ragnarok.Utils;


/**
 * The class converts the official skill descriptions to DS's ones.
 * All rights of the skill descriptions belong to <a href="http://fharr.com/">http://fharr.com/</a>.
 *
 * @author Xavier Lin
 */
public class DSSkillConverter {

    public static void main(String[] args) throws Exception {
        DSSkillConverter converter = new DSSkillConverter(
                "data/skillinfoz/ds_skill.html",
                "data/skillinfoz/skillnametable.txt",
                "data/skillinfoz/skilldescript.lua");
        converter.convert("data/converted/skilldescript.lua");
    }





    private Map<String, List<String>> dsSkillTable = new LinkedHashMap<>(); // <Name, DS description>
    private Map<String, String> nameTable = new LinkedHashMap<>(); // <Skill_id, Name>
    private Map<String, String> officialSkillTable = new LinkedHashMap<>(); // <Skill_id, Official description>

    /**
     * Creates and initializes a DSSkillConverter instance.
     * @param dsSkillFile The file content of <a href="http://rs.fharr.com/static/skilllist2_dsfix.js.pagespeed.jm.Hz7EWXnsFI.js">http://rs.fharr.com/static/skilllist2_dsfix.js.pagespeed.jm.Hz7EWXnsFI.js</a>
     * @param skillNameTable skillnametable.txt retrieved from data.grf
     * @param skillDescript skilldescript.lua retrieved from data.grf
     */
    public DSSkillConverter(
            String dsSkillFile,
            String skillNameTable,
            String skillDescript) {

        ExecutorService[] executorServices = new ExecutorService[3];
        for (int i=0; i<executorServices.length; i++)
            executorServices[i] = Executors.newSingleThreadExecutor();

        try {
            Future[] futures = new Future[3];
            futures[0] = executorServices[0].submit(() -> { parseDSData(dsSkillFile); return null; });
            futures[1] = executorServices[1].submit(() -> { parseSkillNameTable(skillNameTable); return null; });
            futures[2] = executorServices[1].submit(() -> { parseSkillDescript(skillDescript); return null; });

            for (Future future : futures)
                future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        for (ExecutorService executorService : executorServices)
            executorService.shutdownNow();
    }

    /**
     * Converts the official skill descriptions to the DS's ones.
     * @param outputFileName The output file name (The converted .lua file)
     * @throws Exception
     */
    public void convert(String outputFileName) throws Exception {
        System.out.print(String.format("Creating '%s' ... ", outputFileName));
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName), "MS950"))){
            writer.write(String.format("SKILL_DESCRIPT =%n{%n"));
            for (Map.Entry<String, String> entry : officialSkillTable.entrySet()) {
                String id = entry.getKey();
                String name = nameTable.get(id);
                writer.write(String.format("\t[SKID.%s] =%n\t{%n", id));
                if (dsSkillTable.containsKey(name)) {
                    for (String line : dsSkillTable.get(name))
                        writer.write(String.format("\t\t\"%s\",%n", Utils.fixEncoding(line)));
                } else {
                    writer.write(Utils.fixEncoding(entry.getValue()));
                }
                writer.write(String.format("\t},%n%n"));
            }
            writer.write("}");
            writer.flush();
        }
        System.out.println("finished");
    }

    /**
     * Parses the data retrieved from fharr website. Note that there should be a '\n' character at the end of each skill.
     * @param fileName The file name of DS's skill description
     * @throws IOException
     */
    private void parseDSData(String fileName) throws IOException {
        System.out.println(String.format("Parsing '%s' ...", fileName));

        String data = FileUtils.readFileToString(new File(fileName));
        String[] skills = data.split("\n"); // Split each skill (given that each skill is separated by a '\n' character)

        Pattern pattern1 = Pattern.compile(".+\"(([^\\s]+)\\((\\w*\\.*'*-*\\s*)+\\)(.+))\"\\];"); // Parse name and description
        Pattern pattern2 = Pattern.compile(".*<span style=('|\\\\\")color:#(\\p{Alnum}{6}|\\p{Alnum}{3})('|;\\\\\")>.+"); // Parse color code
        for (String skill : skills) {
            Matcher matcher = pattern1.matcher(skill);
            if (matcher.matches()) {
                String name = matcher.group(2);
                String description = matcher.group(1);
                // Parse description
                while ((description.contains("<span"))) {
                    matcher = pattern2.matcher(description);
                    if (matcher.matches()) {
                        String color = matcher.group(2);
                        if (color.length() == 3) // Handle the special cases
                            color = String.format("%c%c%c%c%c%c", color.charAt(0), color.charAt(0), color.charAt(1), color.charAt(1), color.charAt(2), color.charAt(2));
                        description = description.replaceFirst("<span style=('|\\\\\")color:#(\\p{Alnum}{6}|\\p{Alnum}{3})('|;\\\\\")>", "^"+color);
                    }
                }
                description = description.replace("</span>", "^000000");

                // Ignore sp info
                int index = description.lastIndexOf("<hr>Sp");
                if (index != -1)
                    description = description.substring(0, index);

                // Add to the table
                List<String> lines = new ArrayList<>();
                Collections.addAll(lines, description.split("<br/>"));
                dsSkillTable.put(name, lines);
            }
        }
    }

    /**
     * Parses skillnametable.txt
     * @param skillNameTable Path to skillnametable.txt
     * @throws IOException
     */
    private void parseSkillNameTable(String skillNameTable) throws IOException {
        System.out.println(String.format("Parsing '%s' ...", skillNameTable));

        try (BufferedReader reader = new BufferedReader(new FileReader(skillNameTable))){
            Pattern pattern = Pattern.compile("(.+)#(.+)#.*"); // Note that this pattern cannot handle BOM-character
            while (reader.ready()) {
                String line = reader.readLine();
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches())
                    nameTable.put(matcher.group(1), matcher.group(2));
            }
        }
    }

    /**
     * Parses skilldescript.lua
     * @param skillDescript Path to skilldescript.lua
     * @throws IOException
     */
    private void parseSkillDescript(String skillDescript) throws IOException {
        System.out.println(String.format("Parsing '%s' ...", skillDescript));

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(skillDescript), "MS950"))){
            Pattern pattern = Pattern.compile(".*\\[.+\\.(.+)\\]\\s*=.*");
            while (reader.ready()) {
                String line = reader.readLine();
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String id = matcher.group(1);
                    reader.readLine(); // Consume "{"
                    StringBuilder description = new StringBuilder();
                    while (!(line = reader.readLine()).contains("},"))
                        description.append(line).append("\r\n");
                    officialSkillTable.put(id, description.toString());
                }
            }
        }
    }

}
