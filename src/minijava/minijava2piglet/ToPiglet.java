package minijava.minijava2piglet;

import minijava.symbol.*;

public class ToPiglet {
    public static boolean checkOutOfIndex;
    public static int maxCallParas;

    public static void compile(String src, String dst) {
        System.out.println("Compiling " + src);

        checkOutOfIndex = false;    // Blame the EVIL Kanga Interpreter...
        maxCallParas = 20;  // Blame the EVIL Piglet Interpreter...
        Code.init(dst);
        MJava.buildCode();
        Code.finish();

        System.out.println("Done");

        //MJava.show();
    }
}