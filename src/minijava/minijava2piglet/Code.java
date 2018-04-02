package minijava.minijava2piglet;

import java.io.*;
import java.util.*;

public class Code {
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

    public static void emit(String code, String prefix) {
        out.print(prefix + code);
    }

    public static void emit(String code) {
        emit(code, "\t");
    }

    public static void emitln(String code) {
        emit(code + "\n");
    }

    public static void label(String label) {
        emit(label + "\tNOOP\n", "");
    }

    public static void error() {
        emitln("ERROR");
    }

    // All the "String exp" bellow stands for "SimpleExp"

    public static void mov(int dreg, String exp) {
        emitln("MOVE TEMP " + dreg + " " + exp);
    }

    public static void mov(int dreg, int sreg) {
        mov(dreg, "TEMP " + sreg);
    }

    public static void lt(int dreg, int sreg, String exp) {
        mov(dreg, "LT TEMP " + sreg + " " + exp);
    }

    public static void plus(int dreg, int sreg, String exp) {
        mov(dreg, "PLUS TEMP " + sreg + " " + exp);
    }

    public static void minus(int dreg, int sreg, String exp) {
        mov(dreg, "MINUS TEMP " + sreg + " " + exp);
    }

    public static void times(int dreg, int sreg, String exp) {
        mov(dreg, "TIMES TEMP " + sreg + " " + exp);
    }

    public static void load(int dreg, int breg, int bias) {   // dst reg, base reg, bias
        emitln("HLOAD TEMP " + dreg + " TEMP " + breg + " " + bias);
    }

    public static void store(int breg, int bias, int sreg) {  // base reg, bias, src reg
        emitln("HSTORE TEMP " + breg + " " + bias + " TEMP " + sreg);
    }

    public static void jump(String label, int ereg) {
        if (ereg < 0)
            emitln("JUMP " + label);
        else
            emitln("CJUMP TEMP " + ereg + " " + label);
    }

    public static void print(int sreg) {
        emitln("PRINT TEMP " + sreg);
    }

    public static void malloc(int dreg, String exp) {
        mov(dreg, "HALLOCATE " + exp);
    }

    public static void call(int rreg, String exp, Integer... pregs) {
        String args = "(";
        for (int preg : pregs) {
            args += " TEMP " + preg;
        }
        args += " )";

        mov(rreg, "CALL " + exp + args);
    }

    public static void callocFunc() {
        String l1 = Label.getnew();
        String func = 
        "calloc [2]\n" +
        "BEGIN\n" +
        "\tMOVE TEMP 2 0\n" +
        "\tMOVE TEMP 3 0\n" +
        "\tMOVE TEMP 4 TEMP 0\n" +
        l1 + "\tNOOP\n" +
        "\tHSTORE TEMP 4 0 TEMP 2\n" +
        "\tMOVE TEMP 4 PLUS TEMP 4 4\n" +
        "\tMOVE TEMP 5 LT TEMP 4 TEMP 1\n" +
        "\tMOVE TEMP 5 LT TEMP 5 1\n" +
        "\tCJUMP TEMP 5 " + l1 + "\n" +
        "RETURN\n" +
        "\t0\n" +
        "END\n\n";

        emit(func, "");
    }
}