package minijava.minijava2piglet;

import minijava.symbol.*;

public class ToPiglet {
    public static int maxCallParas;

    public static void compile(String src, String dst) {
        System.out.println("Compiling " + src);

        maxCallParas = 20; // Blame the EVIL Piglet Interpreter...
        Code.init(dst);
        MJava.buildCode();
        Code.finish();
        //MJava.show();

        System.out.println("Done");
    }
}