package kanga.kanga2MIPS;

import tools.*;

class Code extends CodeWriter {
    private static String lbuf = ""; // label buffer

    public static void emit(String code) {
        emit(code, String.format("%-6s", lbuf), "\n");
        lbuf = "";
    }

    private static String Ins(String ins) {
        return String.format("%-8s", ins);
    }

    static void label(String label) {
        if (lbuf != "")
            emit("");
        lbuf = label + ":";
    }

    static void mov(String d, String s) {
        if (d != s)
            emit(Ins("move") + d + ", " + s);
    }

    static void li(String d, int i) {
        emit(Ins("li") + d + ", " + i);
    }

    static void la(String d, String addr) {
        emit(Ins("la") + d + ", " + addr);
    }

    static void add(String d, String s1, String s2) {
        emit(Ins("add") + d + ", " + s1 + ", " + s2);
    }

    static void addi(String d, String s, int i16) {
        emit(Ins("addi") + d + ", " + s + ", " + i16);
    }

    static void sub(String d, String s1, String s2) {
        emit(Ins("sub") + d + ", " + s1 + ", " + s2);
    }

    static void mul(String d, String s1, String s2) {
        emit(Ins("mul") + d + ", " + s1 + ", " + s2);
    }

    static void slt(String d, String s1, String s2) {
        emit(Ins("slt") + d + ", " + s1 + ", " + s2);
    }

    static void slti(String d, String s, int i16) {
        emit(Ins("slti") + d + ", " + s + ", " + i16);
    }

    static void lw(String d, String base, int i16) {
        emit(Ins("lw") + d + ", " + i16 + "(" + base + ")");
    }

    static void sw(String base, int i16, String s) {
        emit(Ins("sw") + s + ", " + i16 + "(" + base + ")");
    }

    static void b(String label) {
        //emit("b " + label);
        emit(Ins("j") + label);
    }

    static void bne(String t, int i16, String label) {
        emit(Ins("bne") + t + ", " + i16 + ", " + label);
    }

    static void j(String func) {
        emit(Ins("j") + func);
    }

    static void jr(String t) {
        emit(Ins("jr") + t);
    }

    static void jal(String func) {
        emit(Ins("jal") + func);
    }

    static void jalr(String t) {
        emit(Ins("jalr") + t);
    }

    static void syscall() {
        emit("syscall");
    }
}