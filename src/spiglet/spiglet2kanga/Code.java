package spiglet.spiglet2kanga;

import tools.*;

class Code extends CodeWriter {
    static String[] REG = { "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9", "s0", "s1", "s2", "s3", "s4",
            "s5", "s6", "s7", "v0", "v1", "a0", "a1", "a2", "a3" };
    private static String buf = "";
    public static int t0 = 0, t9 = 9, s0 = 10, s7 = 17, v0 = 18, v1 = 19, a0 = 20;

    public static void noop() {
        emit("", "", ""); // ignore NOOP
    }

    public static void error() {
        emit("ERROR");
    }

    public static void jump(String label, int reg) {
        if (label == "")
            return;

        if (reg < 0) {
            emit("JUMP " + label);
        } else {
            emit("CJUMP " + REG[reg] + " " + label);
        }
    }

    public static void store(int dreg, String bias, int sreg) {
        emit("HSTORE " + REG[dreg] + " " + bias + " " + REG[sreg]);
    }

    public static void load(int dreg, int sreg, String bias) {
        emit("HLOAD " + REG[dreg] + " " + REG[sreg] + " " + bias);
    }

    public static void mov(int dreg, int sreg) {
        mov_(REG[sreg]);
        mov(dreg);
    }

    public static void mov(int dreg) {
        if (REG[dreg] != buf)
            emit("MOVE " + REG[dreg] + " " + buf);

        buf = "";
    }

    public static void mov_(String exp) {
        buf = exp;
    }

    public static void print(String num) {
        emit("PRINT " + num);
    }

    public static void lreg(int tmp, int spill) {
        emit("ALOAD " + REG[tmp] + " SPILLEDARG " + spill);
    }

    public static void sreg(int spill, int tmp) {
        emit("ASTORE SPILLEDARG " + spill + " " + REG[tmp]);
    }

    public static void pass(int reg, int index) {
        if (index < 4) {
            mov(index + a0, reg);
        } else {
            emit("PASSARG " + (index - 3) + " " + REG[reg]);
        }
    }

    public static void call(String exp) {
        emit("CALL " + exp);
        mov_("v0");
    }

    public static void malloc(String exp) {
        mov_("HALLOCATE " + exp);
    }

    public static void op(String op, int sreg1, String exp) {
        mov_(op + " " + REG[sreg1] + " " + exp);
    }
}