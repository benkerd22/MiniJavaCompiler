package minijava.minijava2piglet;

import java.io.*;

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

    public static void load(int dreg, int breg, int bias) { // dst reg, base reg, bias
        emitln("HLOAD TEMP " + dreg + " TEMP " + breg + " " + bias);
    }

    public static void store(int breg, int bias, int sreg) { // base reg, bias, src reg
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
        String loop = Label.getnew();

        emit("calloc [2]\nBEGIN\n", "");

        mov(2, "0");
        mov(3, 0);

        label(loop);
        store(3, 0, 2);
        plus(3, 3, "4");
        lt(4, 3, "TEMP 1");
        lt(4, 4, "1");
        jump(loop, 4);

        emit("RETURN\n\t0\nEND\n\n", "");
    }
}