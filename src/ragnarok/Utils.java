package ragnarok;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Xavier Lin
 */
public class Utils {

    /**
     * Fix Chinese Big5 encoding problems.
     * @param string The original string
     * @return The fixed string
     */
    public static String fixEncoding(String string) {
        Set<Character> charSet = new HashSet<>();
        Collections.addAll(charSet, '許', '功', '蓋', '髏', '淚', '餐', '枯');

        StringBuilder fixedString = new StringBuilder(string);
        for (int i=0; i<fixedString.length(); i++)
            if (charSet.contains(fixedString.charAt(i)))
                if ( (i == fixedString.length()-1) || (fixedString.charAt(i+1) != '\\') )
                    fixedString.insert(i+1, '\\');

        return fixedString.toString();
    }
}
