import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpLogMatcherTest {
    @org.junit.Test
    public void canGetSection() throws Exception {
        String line = "199.72.81.55 - - [01/Jul/1995:00:00:01 -0400] \"GET /history/apollo/ HTTP/1.0\" 200 6245";
        String expected = "/history";

        Optional<String> maybeActual = HttpLogMatcher.getSection(line);
        assertTrue(maybeActual.isPresent());

        String actual = maybeActual.get();
        assertEquals(expected, actual);
    }
}