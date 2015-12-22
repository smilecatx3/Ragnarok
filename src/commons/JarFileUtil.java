package commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 程式打包成jar檔之後，若要讀取jar裡面的資料，就需要使用這個API。<br/>
 * 不能直接用File的API，因為jar裡面的檔案不屬於file system類型的檔案。
 *
 */
public class JarFileUtil {
	
	/**
	 * 取得dirName裡的所有檔案名稱 (不包括資料夾名稱)
	 * @param classObject TypeName.class (可以直接使用getClass()當作引數傳入)
	 * @param dirName
	 * @return dirName裡的所有檔案名稱list (不包括資料夾名稱)
	 */
	public static List<String> listFiles (Class<?> classObject, String dirName) {
		List<String> fileNameList = new ArrayList<>();
		CodeSource codeSource = classObject.getProtectionDomain().getCodeSource();
		if (codeSource == null) {
			System.err.println("Can't get code source of the class: " + classObject.getName());
			return null;
		}
		try {
			ZipInputStream zip = new ZipInputStream(codeSource.getLocation().openStream());	// 讀取jar檔
			while (true) {
				ZipEntry zipEntry = zip.getNextEntry();	// 取得jar裡面的檔案
				if (zipEntry == null)
					break;
				String fileName = zipEntry.getName();
				// 列出所有開頭是dirName的檔案名稱
				// 使用!fileName.endsWith("/")排除資料夾名稱
				if (fileName.startsWith(dirName) && !fileName.endsWith("/"))
					fileNameList.add("/"+fileName);
			}
			zip.close();
			if (fileNameList.size()!=0)
				return fileNameList;
			else {
				System.err.printf("Can't list files. %s, dirName: %s%n", classObject, dirName);
				System.err.printf("Check if the class is in a jar file, and dirName is correct.%n");
				System.err.println("Note that dirName should NOT lead with /%n");
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 匯出成jar檔後，要讀取jar裡面的檔案時需要使用此method
	 * @param classObject TypeName.class (可以直接使用getClass()當作引數傳入)
	 * @param fileName 檔案名稱 (包括jar檔內的完整路徑)
	 * @return 檔案內容
	 */
	public static String readFile(Class<?> classObject, String fileName) {
		StringBuilder data = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(classObject.getResourceAsStream(fileName)));
			while (reader.ready())
				data.append(reader.readLine()).append("\n");
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data.toString();
	}
	
	/**
	 * 匯出成jar檔後，要讀取jar裡面的資料夾內的所有檔案時需要使用此method。
	 * @param classObject TypeName.class (可以直接使用getClass()當作引數傳入)
	 * @param dirName 資料夾名稱
	 * @return 檔案內容list
	 */
	public static List<String> readFiles(Class<?> classObject, String dirName) {
		List<String> dataList = new ArrayList<>();
		List<String> filePaths = listFiles(classObject, dirName);
		if (filePaths == null) {
			System.err.println("At JarFileUtil.readFiles: Fail to read files.");
			return null;
		}
		for (Iterator<String> iterator = filePaths.iterator(); iterator.hasNext(); ) 
			dataList.add(readFile(classObject, iterator.next()));
		return dataList;
	}
}
