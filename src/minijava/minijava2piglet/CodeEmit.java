package minijava.minijava2piglet;

import java.io.*;

public class CodeEmit {
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

    public static void send(String code) {
        out.print(code);
    }

    public static void sendln(String code) {
        out.println(code);
    }
}