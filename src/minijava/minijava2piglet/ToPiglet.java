package minijava.minijava2piglet;

import minijava.symbol.*;

public class ToPiglet {
    public static void compile(String filename) {
        Code.init(filename);
        MJava.buildCode();
        Code.finish();

        System.out.println("DONE");
    }
}