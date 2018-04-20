package tools;

import java.io.*;

public class CodeWriter {
    private static PrintWriter out;

    public static void init(String filename) {
        try {
            out = new PrintWriter(filename, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void finish() {
        out.close();
    }

    public static void emit(String code) {
        emit(code, "\t", "\n");
    }

    public static void emit(String code, String prefix, String suffix) {
        out.print(prefix + code + suffix);
    }
}