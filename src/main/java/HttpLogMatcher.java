import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HttpLogMatcher {

    private static final Pattern SECTION_PATTERN = Pattern.compile(".*\"GET (\\S+) .*");

    static Optional<String> getSection(String maybeLine) {
        return Optional.ofNullable(maybeLine)
                .map(SECTION_PATTERN::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1))
                .map(url -> url.split("/"))
                .filter(urlParts -> urlParts.length > 1)
                .map(urlParts -> {
                    if (urlParts.length == 2) {
                        return "/";
                    }
                    return "/" + urlParts[1];
                });
    }
}
