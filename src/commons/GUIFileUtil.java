package commons;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;

public class GUIFileUtil {
	private static JFileChooser fileChooser = new JFileChooser();

	public static String openFile() {
		fileChooser.setMultiSelectionEnabled(false);
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			return FileUtil.readFile(fileChooser.getSelectedFile().getAbsolutePath());
		return null;
	}
	
	public static List<String> openFiles() {
		fileChooser.setMultiSelectionEnabled(true);
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();
			String[] fileNames = new String[files.length];
			for (int i=0; i<fileNames.length; i++)
				fileNames[i] = files[i].getAbsolutePath();
			return FileUtil.readFiles(fileNames);
		}
		return null;
	}
	
	public static void saveFile(String content) {
		fileChooser.setMultiSelectionEnabled(false);
		if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
			FileUtil.saveFile(content, fileChooser.getSelectedFile().getAbsolutePath());
	}
}
