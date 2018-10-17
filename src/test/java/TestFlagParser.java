import com.salconpetroleum.parser.OptionParser;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Phua Ging Sheng (phua.gingsheng@gmail.com)
 * @version 1.0
 */
public class TestFlagParser {

    public static void main(String args[]) {
        String[] argv = "a -I/lib1 b -I=/lib2 -I /lib3 c -help -file/dev/null --filetype=txt -filetype cpp d".split(" ");
        System.out.println(argv.length);
        System.out.println(Arrays.toString(argv));
    }

    @Test
    public void TestParser() {
        OptionParser parser = new OptionParser();

        parser.addStringOption("I");
        parser.addStringOption("f file");
        parser.addStringOption("t filetype");

        parser.addBoolOption("h help");
        parser.addBoolOption("v version");

        String[] argv = "a -I/lib1 b -I=/lib2 -I /lib3 c -help -file/dev/null --filetype=txt -filetype cpp d".split(" ");

        String[] rest = parser.parse(argv);

        assertEquals(4, rest.length);  // a b c d
        assertTrue(Arrays.asList(rest).indexOf("a") != -1);
        assertTrue(Arrays.asList(rest).indexOf("b") != -1);
        assertTrue(Arrays.asList(rest).indexOf("c") != -1);
        assertTrue(Arrays.asList(rest).indexOf("d") != -1);

        // Any flag that represents the same option, should return the same value

        assertTrue(parser.isSet("file"));
        assertTrue(parser.isSet("f"));

        assertTrue(parser.isSet("help"));
        assertTrue(parser.isSet("filetype"));

        assertTrue(parser.isSet("I"));
        assertFalse(parser.isSet("version"));

        //[I, f, file, t, filetype, h, help, v, version]
        String[] iOptions = parser.getAll("I");
        assertEquals(3, iOptions.length);

        // They all have to be here, but can come in any order
        assertTrue(Arrays.asList(iOptions).indexOf("/lib1") != -1);
        assertTrue(Arrays.asList(iOptions).indexOf("/lib2") != -1);
        assertTrue(Arrays.asList(iOptions).indexOf("/lib3") != -1);

        //[help, file/dev/null, filetype=cpp, filetype=txt]
        assertEquals("/dev/null", parser.get("file"));

        String[] typeOptions = parser.getAll("filetype");
        assertEquals(2, typeOptions.length);

        assertTrue(Arrays.asList(typeOptions).indexOf("txt") != -1);
        assertTrue(Arrays.asList(typeOptions).indexOf("cpp") != -1);

        // Unset all flags, ready to parse new input
        parser.reset();

        assertFalse(parser.isSet("file"));
        assertFalse(parser.isSet("f"));

        assertFalse(parser.isSet("help"));
        assertFalse(parser.isSet("filetype"));

        assertFalse(parser.isSet("I"));
        assertFalse(parser.isSet("version"));

        // Allow juxtaposition of boolean flags
        rest = parser.parse("a b c -hv d".split(" "));

        assertEquals(4, rest.length);

        assertTrue(parser.isSet("help"));
        assertTrue(parser.isSet("version"));

    }
}
