package spiglet.spiglet2kanga;

import java.io.*;
import spiglet.*;

class SPP {
    private static boolean b = true; // the first time using SpigletParser?

    public static void accept(final File f) {
        try {
            InputStream inf = new FileInputStream(f);
            if (b) {
                new SpigletParser(inf);
                b = false;
            } else
                SpigletParser.ReInit(inf);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}

public class ToKanga {
    public static boolean isNative = false;

    public static void compile(String src, String dst) {
        File f = new File(src);
        if (!f.isFile()) {
            System.out.println(src + " is not a folder nor a file");
            return;
        }

        SPP.accept(f);

        System.out.println("Compiling " + src);
        try {
            Code.init(dst);
            SpigletParser.Goal().accept(new GraphBuilder(), null);
            Code.finish();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Done");
    }
}