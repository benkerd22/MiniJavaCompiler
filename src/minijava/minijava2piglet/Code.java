package minijava.minijava2piglet;

import tools.*;

public class Code extends CodeWriter {
    public static void label(String label) {
        emit(label + "\tNOOP", "", "\n");
    }

    public static String T(int reg) {
        return "TEMP " + reg;
    }

    public static void error() {
        emit("ERROR");
    }

    // All the "String exp" bellow stands for "SimpleExp"

    public static void mov(int dreg, String exp) {
        emit("MOVE " + T(dreg) + " " + exp);
    }

    public static void mov(int dreg, int sreg) {
        mov(dreg, "" + T(sreg));
    }

    public static void lt(int dreg, int sreg, String exp) {
        mov(dreg, "LT " + T(sreg) + " " + exp);
    }

    public static void plus(int dreg, int sreg, String exp) {
        mov(dreg, "PLUS " + T(sreg) + " " + exp);
    }

    public static void minus(int dreg, int sreg, String exp) {
        mov(dreg, "MINUS " + T(sreg) + " " + exp);
    }

    public static void times(int dreg, int sreg, String exp) {
        mov(dreg, "TIMES " + T(sreg) + " " + exp);
    }

    public static void load(int dreg, int breg, int bias) { // dst reg, base reg, bias
        emit("HLOAD " + T(dreg) + " " + T(breg) + " " + bias);
    }

    public static void store(int breg, int bias, int sreg) { // base reg, bias, src reg
        emit("HSTORE " + T(breg) + " " + bias + " " + T(sreg));
    }

    public static void jump(String label, int ereg) {
        if (ereg < 0)
            emit("JUMP " + label);
        else
            emit("CJUMP " + T(ereg) + " " + label);
    }

    public static void print(int sreg) {
        emit("PRINT " + T(sreg));
    }

    public static void malloc(int dreg, String exp) {
        mov(dreg, "HALLOCATE " + exp);
    }

    public static void call(int rreg, String exp, Integer... pregs) {
        String args = "(";
        for (int preg : pregs) {
            args += " " + T(preg);
        }
        args += " )";

        mov(rreg, "CALL " + exp + args);
    }

    public static void callocFunc() {
        String loop = Label.getnew();

        emit("calloc [2]\nBEGIN", "", "\n");

        mov(2, "0");
        mov(3, 0);
        mov(4, "0");

        label(loop);
        store(3, 0, 2);
        plus(3, 3, "4");
        plus(4, 4, "4");
        lt(5, 4, T(1));
        lt(5, 5, "1");
        jump(loop, 5);

        emit("RETURN\n\t0\nEND\n", "", "\n");
    }
}