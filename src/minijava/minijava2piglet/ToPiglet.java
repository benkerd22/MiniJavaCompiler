package minijava.minijava2piglet;

import minijava.symbol.*;

public class ToPiglet {
    private static String ext(String s) { // change extension ".java" to ".spg"
        return s.replace(".java", ".spg");
    }

    public static void compile(String filename) {
        System.out.println("Compiling " + filename);

        Code.init(ext(filename));
        MJava.buildCode();
        Code.finish();

        System.out.println("Done");

        //MJava.show();
    }
}