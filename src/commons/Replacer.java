package commons;

import java.util.regex.Pattern;

/**
 * 用來取代字串內容的工具
 *
 */
public class Replacer {
	
	/**
	 * @param source 原始字串
	 * @param target 要被取代的字串
	 * @param replacement 要替換成的字串
	 * @return 取代完的字串
	 */
	public static String replace(String source, String target, String replacement) {
		StringBuilder result = new StringBuilder();
		String[] lines_source = source.replace("\r", "").split("\n");
		String[] lines_target = target.replace("\r", "").split("\n");
		int index_target = 0;
		int lineNum_matched = 0;
		
		// 設定每行的target為regex pattern
		for (int i=0; i<lines_target.length; i++)
			lines_target[i] = "\\s*" + Pattern.quote(lines_target[i].trim()) + "\\s*";
		// 開始搜尋source
		for (int lineNum=0; lineNum<lines_source.length; lineNum++) {
			String currentLine = lines_source[lineNum];
			// 目前的行沒有match到target pattern
			if (!currentLine.matches(lines_target[index_target])) {
				// index_target大於0代表之前有match過，但現在沒有match到，則代表沒有完整match。
				// 把從第一次match到的行、到目前行數上一行的內容append到result。
				if (index_target > 0) {
					for (int i=lineNum_matched; i<lineNum; i++)
						result.append(lines_source[i]).append("\r\n");
					index_target = 0;
				}
				result.append(currentLine).append("\r\n");
			// 目前的行match到target pattern
			} else {
				// 紀錄第一次matched到的行數
				if (index_target == 0)
					lineNum_matched = lineNum;
				index_target++;
				// 已經完全match到，則把replacement字串append到result。並重設index_target，繼續往後搜尋。
				if (index_target == lines_target.length) {
					result.append(replacement).append("\r\n");
					index_target = 0;
				}
			}
		}
		return result.toString();
	}
}