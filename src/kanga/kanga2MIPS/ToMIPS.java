package kanga.kanga2MIPS;

import java.io.*;
import kanga.*;

class KP {
    private static boolean b = true; // the first time using KangaParser?

    public static void accept(final File f) {
        try {
            InputStream inf = new FileInputStream(f);
            if (b) {
                new KangaParser(inf);
                b = false;
            } else
                KangaParser.ReInit(inf);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}

public class ToMIPS {
    public static boolean isNative = false;

    public static void compile(String src, String dst) {
        File f = new File(src);
        if (!f.isFile()) {
            System.out.println(src + " is not a folder nor a file");
            return;
        }

        KP.accept(f);

        System.out.println("Compiling " + src);
        try {
            Code.init(dst);
            KangaParser.Goal().accept(new CodeGenerator());
            Code.finish();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Done");
    }
}