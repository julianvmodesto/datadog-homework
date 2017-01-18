import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Table test for section matching
 */
@RunWith(Parameterized.class)
public class HttpLogMatcherTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"199.72.81.55 - - [01/Jul/1995:00:00:01 -0400] \"GET /history/apollo/ HTTP/1.0\" 200 6245", "/history"},
                {"pma02.rt66.com - - [01/Jul/1995:00:30:32 -0400] \"GET /index.html HTTP/1.0\" 200 786", "/"},
                {"139.121.119.19 - - [01/Jul/1995:00:30:17 -0400] \"GET /images/KSC-logosmall.gif HTTP/1.0\" 200 1204", "/images"}
        });
    }

    private String line;
    private String expectedSection;

    public HttpLogMatcherTest(String line, String expectedSection) {
        this.line = line;
        this.expectedSection = expectedSection;
    }

    @Test
    public void canGetSectionFromLine() {
        Optional<String> maybeActual = HttpLogMatcher.getSection(line);
        assertTrue(maybeActual.isPresent());

        String actual = maybeActual.get();
        assertEquals(expectedSection, actual);
    }
}