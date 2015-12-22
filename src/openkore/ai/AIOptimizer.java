package openkore.ai;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import commons.FileUtil;
import commons.JarFileUtil;
import commons.Replacer;


/**
 * 快速優化OpenKore的AI行為。請參考<a href="ro2.snop.idv.tw/">續－仙研網</a>的文章：
 * <a href="http://ro2.snop.idv.tw/forum.php?mod=viewthread&tid=1460">thread 1</a>、
 * <a href="http://ro2.snop.idv.tw/forum.php?mod=viewthread&tid=63">thread 2</a>
 *
 * @author Xavier Lin
 */
public class AIOptimizer {

    public static void main(String args[]) {
        // 設定look-and-feel為目前系統的外觀
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {	}
        AIOptimizer optimizer = new AIOptimizer();
        optimizer.openSrcFiles();
        for (String targetFileName : optimizer.targetFileNames)
            optimizer.replace(targetFileName);
        JOptionPane.showMessageDialog(null, "已全部修改完成，記得修改config.txt。");
    }



    // pattern的資料結構
    private class Pattern {
        String target;
        String replacement;
        Pattern(String target, String replacement) {
            this.target = target;
            this.replacement = replacement;
        }
    }

	private final static String targetFileName = "data/openkore/targetFileNames.txt";	// 要修改的檔案名稱清單
	private final static String patternDirPath = "data/oprenkore/patterns/";			// Pattern的資料夾名稱
	private final static Map<String, List<Pattern>> patternFilesMap = new HashMap<>();	// 紀錄每個要修改的檔案，要用到哪些pattern。<檔案名稱，pattern list>
	private String[] targetFileNames;													// targetFileName裡面的每一行
	private Map<String, File> srcFilesMap = new HashMap<>();							// 紀錄要修改的檔案位置 <檔案名稱，檔案位置>

	// constructor設定pattern list資料
	public AIOptimizer() {
		setTargetFileNames();
		for (String dirName : targetFileNames)
			setPatterns(dirName);
	}
	
	private void openSrcFiles() {
		String srcDirPath = "";
		// 設定JFileChooser屬性
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("選擇src資料夾");
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "src directory";
			}
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});
		// 選擇src資料夾位置
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			String dirName = fileChooser.getSelectedFile().toString();
			if ( !dirName.contains("src") || (dirName.length()-dirName.lastIndexOf("src")!=3) ) {
				JOptionPane.showMessageDialog(null, "只能選擇src資料夾!!");
				System.exit(0);
			}
			srcDirPath = dirName + File.separator;
		} else {
			System.exit(0);
		}
		// 設定要修改的檔案位置
		srcFilesMap.put(targetFileNames[0], new File(srcDirPath+targetFileNames[0]));
		srcFilesMap.put(targetFileNames[1], new File(srcDirPath+"AI"+File.separator+targetFileNames[1]));
		srcFilesMap.put(targetFileNames[2], new File(srcDirPath+"AI"+File.separator+targetFileNames[2]));
		srcFilesMap.put(targetFileNames[3], new File(srcDirPath+"Task"+File.separator+targetFileNames[3]));
		srcFilesMap.put(targetFileNames[4], new File(srcDirPath+"Network"+File.separator+targetFileNames[4]));
		srcFilesMap.put(targetFileNames[5], new File(srcDirPath+"Network"+File.separator+"Receive"+File.separator+targetFileNames[5]));
	}
	
	private void setTargetFileNames() {
		String content = JarFileUtil.readFile(getClass(), targetFileName);
		targetFileNames = content.trim().replace("\r", "").split("\n");
	}
	
	private void setPatterns(String dirName) {
		List<Pattern> patterns = new ArrayList<>();
		List<String> patternFiles = JarFileUtil.readFiles(getClass(), patternDirPath+dirName);	// 讀取jar檔裡pattern資料夾下的所有檔案
		for (Iterator<String> iterator = patternFiles.iterator(); iterator.hasNext(); ) {
			String content = iterator.next();
			String target = content.substring(content.indexOf("# BEFORE #")+"# BEFORE #".length(), content.indexOf("# AFTER #")).trim();
			String replacement = content.substring(content.indexOf("# AFTER #")+"# AFTER #".length(), content.length()).trim();
			patterns.add(new Pattern(target, replacement));
		}
		patternFilesMap.put(dirName, patterns);	// 每個dirName都有自己的pattern list
	}
	
	public void replace(String targetFileName) {
		String content = FileUtil.readFile(srcFilesMap.get(targetFileName).getAbsolutePath());
		// 每個target file都有相對應的pattern，儲存在patternFilesMap裡面。
		for (Iterator<Pattern> iterator = patternFilesMap.get(targetFileName).iterator(); iterator.hasNext(); ) {
			Pattern pattern = iterator.next();
			content = Replacer.replace(content, pattern.target, pattern.replacement);
		}
		FileUtil.saveFile(content, srcFilesMap.get(targetFileName).getAbsolutePath());
	}
}

