package kanga.kanga2MIPS;

import java.util.*;
import kanga.visitor.*;
import kanga.syntaxtree.*;

class CodeGenerator extends DepthFirstVisitor {
    private static String ra = "$ra", sp = "$sp", fp = "$fp";
    private static String v0 = "$v0", v1 = "$v1", t9 = "$t9", a0 = "$a0";

    // ***** Wrappers *****

    private static String R(Reg n) {
        return "$" + n.f0.choice.toString();
    }

    private static class L {
        static Map<String, String> m = new HashMap<String, String>();
        static int cnt = 0;

        static void userfunc(Label n) {
            String l = n.f0.toString();
            m.put(l, "_" + l);
        }

        static String get(Label n) {
            String l = n.f0.toString();
            if (!m.containsKey(l)) {
                m.put(l, "L" + cnt);
                cnt++;
            }

            return m.get(l);
        }
    }

    private class RenameHelper extends DepthFirstVisitor {
        public void visit(Procedure n) {
            L.userfunc(n.f0);
        }
    }

    private static int I(IntegerLiteral n) {
        return Integer.parseInt(n.f0.toString());
    }

    private static boolean isInt16(int i) {
        return Short.MIN_VALUE <= i && i <= Short.MAX_VALUE;
    }

    private void beginF(String id, int slots) {
        Code.emit(".text");
        Code.emit(".globl\t" + id);
        Code.label(id);
        Code.emit("");

        Code.sw(sp, -4, ra);
        Code.sw(sp, -8, fp);
        Code.mov(fp, sp);
        Code.addi(sp, sp, -slots * 4);
    }

    private void endF(int slots) {
        Code.addi(sp, sp, slots * 4);
        Code.lw(ra, fp, -4);
        Code.lw(fp, fp, -8);
        Code.jr(ra);
        Code.emit("");
    }

    private void push(String t) {
        Code.addi(sp, sp, -4);
        Code.sw(sp, 0, t);
    }

    private void pop(String t) {
        Code.lw(t, sp, 0);
        Code.addi(sp, sp, 4);
    }

    private void lw(String d, String base, int i) {
        if (!isInt16(i)) {
            String tmp = getTmp(d, base);
            push(tmp);
            Code.li(tmp, i);
            Code.add(tmp, base, tmp);
            Code.lw(d, tmp, 0);
            pop(tmp);
        } else
            Code.lw(d, base, i);
    }

    private void sw(String base, int i, String s) {
        if (!isInt16(i)) {
            String tmp = getTmp(base, s);
            push(tmp);
            Code.li(tmp, i);
            Code.add(tmp, base, tmp);
            Code.sw(tmp, 0, s);
            pop(tmp);
        } else
            Code.sw(base, i, s);
    }

    private void movExp(String t, SimpleExp n) {
        Node exp = n.f0.choice;
        if (exp instanceof Reg)
            Code.mov(t, R((Reg) exp));
        else if (exp instanceof IntegerLiteral)
            Code.li(t, I((IntegerLiteral) exp));
        else if (exp instanceof Label)
            Code.la(t, L.get((Label) exp));
    }

    private void sys_println() {
        Code.emit(".text");
        Code.emit(".globl\tprintln");
        Code.label("println");
        Code.emit("");

        if (!ToMIPS.isNative)
            push(v0); // the caller pass the $a0, no need to store
        Code.li(v0, 1);
        Code.syscall();
        Code.la(a0, "endl");
        Code.li(v0, 4);
        Code.syscall();
        if (!ToMIPS.isNative)
            pop(v0);

        Code.jr(ra);
        Code.emit("");

        Code.emit(".data");
        Code.emit(".align\t0");
        Code.label("endl");
        Code.emit(".asciiz \"\\n\"");
        Code.emit("");
    }

    private void sys_err() {
        Code.emit(".text");
        Code.emit(".globl\terr");
        Code.label("err");
        Code.emit("");

        // v0 and a0 will not be active afterwards. no need to store
        Code.li(v0, 4);
        Code.la(a0, "err_msg");
        Code.syscall();
        Code.li(v0, 10);
        Code.syscall();
        Code.emit("");

        Code.emit(".data");
        Code.emit(".align\t0");
        Code.label("err_msg");
        Code.emit(".asciiz \"ERROR\\n\"");
        Code.emit("");
    }

    private String getTmp(String used1, String used2) { // get a tmp Reg that differs u1 and u2
        Set<String> s = new TreeSet<String>();
        s.add(v0);
        s.add(v1);
        s.add(t9);
        s.remove(used1);
        s.remove(used2);
        return s.iterator().next();
    }

    // ***** Visitor *****

    public void visit(Goal n) {
        n.f12.accept(new RenameHelper());

        int slots = Integer.parseInt(n.f5.f0.toString()) + 2; // 2 slots for $ra, $fp
        beginF("main", slots);

        n.f10.accept(this);

        endF(slots);

        n.f12.accept(this);

        sys_println();
        sys_err();
    }

    public void visit(Procedure n) {
        int slots = Integer.parseInt(n.f5.f0.toString()) + 2; // 2 slots for $ra, $fp
        beginF(L.get(n.f0), slots); // add a _ to prevent conflicting with built-in functions ie. print, malloc

        n.f10.accept(this);

        endF(slots);
    }

    public void visit(NoOpStmt n) {
        return; // ignore noop
    }

    public void visit(ErrorStmt n) {
        Code.j("err");
    }

    public void visit(CJumpStmt n) {
        Code.bne(R(n.f1), 1, L.get(n.f2));
    }

    public void visit(JumpStmt n) {
        Code.b(L.get(n.f1));
    }

    public void visit(HStoreStmt n) {
        sw(R(n.f1), I(n.f2), R(n.f3));
    }

    public void visit(HLoadStmt n) {
        lw(R(n.f1), R(n.f2), I(n.f3));
    }

    public void visit(MoveStmt n) {
        Node src = n.f2.f0.choice;

        if (src instanceof SimpleExp) {
            movExp(R(n.f1), (SimpleExp) src);
        } else if (src instanceof BinOp) {
            BinOp m = (BinOp) src;
            String op = m.f0.f0.choice.toString();
            Node exp = m.f2.f0.choice;

            if (exp instanceof Reg) {
                switch (op) {
                case "LT":
                    Code.slt(R(n.f1), R(m.f1), R((Reg) exp));
                    break;
                case "PLUS":
                    Code.add(R(n.f1), R(m.f1), R((Reg) exp));
                    break;
                case "MINUS":
                    Code.sub(R(n.f1), R(m.f1), R((Reg) exp));
                    break;
                case "TIMES":
                    Code.mul(R(n.f1), R(m.f1), R((Reg) exp));
                }
            } else {
                if (exp instanceof IntegerLiteral) {
                    int i = I((IntegerLiteral) exp);
                    if (op == "MINUS")
                        i = -i;
                    if (isInt16(i)) {
                        switch (op) {
                        case "LT":
                            Code.slti(R(n.f1), R(m.f1), i);
                            return;
                        case "PLUS":
                        case "MINUS":
                            Code.addi(R(n.f1), R(m.f1), i);
                            return;
                        case "TIMES":
                            // No muli instruction. Need to load i into a Reg (as if i is an Int32)
                            // Flows afterwards!
                        }
                    }
                }

                // Reaching here means:
                //      exp is a Label
                // or   exp is a Int, and is too big (not Int16)
                // or   exp is a Int, and op is "TIMES"

                String tmp = getTmp(R(n.f1), R(m.f1));
                push(tmp);
                movExp(tmp, m.f2);
                switch (op) {
                case "LT":
                    Code.slt(R(n.f1), R(m.f1), tmp);
                    break;
                case "PLUS":
                    Code.add(R(n.f1), R(m.f1), tmp);
                    break;
                case "MINUS":
                    Code.sub(R(n.f1), R(m.f1), tmp);
                    break;
                case "TIMES":
                    Code.mul(R(n.f1), R(m.f1), tmp);
                }
                pop(tmp);
            }
            // End of BinOp
        } else if (src instanceof HAllocate) {
            if (!ToMIPS.isNative) {
                push(a0);
                push(v0);
            }

            movExp(a0, ((HAllocate) src).f1);
            Code.li(v0, 9);
            Code.syscall();
            Code.mov(R(n.f1), v0);

            if (!ToMIPS.isNative) {
                pop(v0);
                pop(a0);
            }
        }
    }

    public void visit(PrintStmt n) {
        if (!ToMIPS.isNative) // not sure whether $a0 is active afterwards
            push(a0);

        movExp(a0, n.f1);
        Code.jal("println");

        if (!ToMIPS.isNative)
            pop(a0);
    }

    public void visit(ALoadStmt n) {
        lw(R(n.f1), fp, (-3 - I(n.f2.f1)) * 4);
    }

    public void visit(AStoreStmt n) {
        sw(fp, (-3 - I(n.f1.f1)) * 4, R(n.f2));
    }

    public void visit(PassArgStmt n) {
        // what to do if i isn't 16bit ...
        // in this case we can't use the $sp
        // haven't found a beautiful solution yet ...
        // >_<
        Code.sw(sp, (-2 - I(n.f1)) * 4, R(n.f2)); // it is a **BUG** when I(n.f1) is too big
    }

    public void visit(CallStmt n) {
        Node exp = n.f1.f0.choice;

        if (exp instanceof Reg)
            Code.jalr(R((Reg) exp));
        else if (exp instanceof Label)
            Code.jal(L.get((Label) exp));
        else if (exp instanceof IntegerLiteral)
            Code.jal(String.valueOf(I((IntegerLiteral) exp)));
    }

    public void visit(Label n) {
        Code.label(L.get(n));
    }
}