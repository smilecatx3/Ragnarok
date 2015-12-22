package commons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 支援以下功能的檔案工具程式: <br>
 * <ol>
 * <li>讀取單一檔案: String readFile(File file)</li>
 * <li>讀取多個檔案: List&lt;String&gt;readFiles(File[] files)</li>
 * <li>存檔: void saveFile(String content, File file)</li>
 * </ol>
 */
public class FileUtil {
	
	/**
	 * 讀取單一檔案
	 * @param file
	 * @return 檔案內容
	 */
	public static String readFile(String filePath) {
		StringBuilder data = new StringBuilder();
		try {
			filePath = URLDecoder.decode(filePath, "UTF-8");
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			while (reader.ready())
				data.append(reader.readLine()).append("\n");
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data.toString();
	}
	
	/**
	 * 讀取多個檔案
	 * @param file
	 * @return 檔案內容list
	 */
	public static List<String> readFiles(String[] fileNames) {
		List<String> datas = new ArrayList<>(fileNames.length);
		for (String fileName : fileNames)
			datas.add(readFile(fileName));
		return datas;
	}
	
	/**
	 * 存檔
	 * @param content 要寫入的內容
	 * @param file 要存的檔案
	 */
	public static void saveFile(String content, String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
