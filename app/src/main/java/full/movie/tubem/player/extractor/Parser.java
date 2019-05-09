package full.movie.tubem.player.extractor;

import full.movie.tubem.player.extractor.exceptions.ParsingException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public static class RegexException extends ParsingException {
        public RegexException(String message) {
            super(message);
        }
    }

    private Parser() {
    }

    public static String matchGroup1(String pattern, String input) throws RegexException {
        return matchGroup(pattern, input, 1);
    }

    public static String matchGroup(String pattern, String input, int group) throws RegexException {
        Matcher mat = Pattern.compile(pattern).matcher(input);
        if (mat.find()) {
            return mat.group(group);
        }
        if (input.length() > 1024) {
            throw new RegexException("failed to find pattern \"" + pattern);
        }
        throw new RegexException("failed to find pattern \"" + pattern + " inside of " + input + "\"");
    }

    public static Map<String, String> compatParseMap(String input) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        for (String arg : input.split("&")) {
            String[] splitArg = arg.split("=");
            if (splitArg.length > 1) {
                map.put(splitArg[0], URLDecoder.decode(splitArg[1], "UTF-8"));
            } else {
                map.put(splitArg[0], "");
            }
        }
        return map;
    }
}
