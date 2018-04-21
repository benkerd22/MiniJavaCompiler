package spiglet.spiglet2kanga;

import java.util.*;
import spiglet.syntaxtree.*;
import spiglet.visitor.*;

class CodeGenerator extends GJVoidDepthFirst<Graph> { // this generator only build code WITHIN a func
    public void visit(NoOpStmt n, Graph g) {
        Code.noop();
    }

    public void visit(ErrorStmt n, Graph g) {
        Code.error();
    }

    public void visit(CJumpStmt n, Graph g) {
        Code.jump(g.getLabel(n.f2.f0.toString()), g.getReg(n.f1));
    }

    public void visit(JumpStmt n, Graph g) {
        Code.jump(g.getLabel(n.f1.f0.toString()), -1);
    }

    public void visit(HStoreStmt n, Graph g) {
        int dreg = g.getReg(n.f1, Code.v1, true);
        int sreg = g.getReg(n.f3, Code.v0, true);

        Code.store(dreg, n.f2.f0.toString(), sreg);
    }

    public void visit(HLoadStmt n, Graph g) {
        int dreg = g.getReg(n.f1, Code.v1, false);
        if (dreg == -1)
            return;

        int sreg = g.getReg(n.f2);

        Code.load(dreg, sreg, n.f3.f0.toString());

        if (dreg == Code.v1)
            Code.sreg(g.getSpill(n.f1), Code.v1);
    }

    public void visit(MoveStmt n, Graph g) {
        int dreg = g.getReg(n.f1, Code.v1, false);
        if (dreg == -1) {
            if (n.f2.f0.choice instanceof Call)
                n.f2.accept(this, g);
            return;
        }

        n.f2.accept(this, g);

        Code.mov(dreg);

        if (dreg == Code.v1)
            Code.sreg(g.getSpill(n.f1), Code.v1);
    }

    public void visit(PrintStmt n, Graph g) {
        Code.print(g.getExp(n.f1));
    }

    public void visit(Call n, Graph g) {
        final int[] argn = new int[1];
        argn[0] = 0;

        class ArgsListHelper extends DepthFirstVisitor {
            public void visit(Temp n) {
                int reg = g.getReg(n);

                if (argn[0] < 4)
                    Code.mov(Code.a0 + argn[0], reg);
                else
                    Code.pass(argn[0] - 3, reg);

                argn[0]++;
            }
        }

        n.f3.accept(new ArgsListHelper());

        Code.call(g.getExp(n.f1));
    }

    public void visit(HAllocate n, Graph g) {
        Code.malloc(g.getExp(n.f1));
    }

    public void visit(BinOp n, Graph g) {
        Code.op(n.f0.f0.choice.toString(), g.getReg(n.f1), g.getExp(n.f2));
    }

    public void visit(SimpleExp n, Graph g) {
        // only from MoveStmt -> Exp -> SimpleExp can reach here
        // must be a mov
        Code.mov_(g.getExp(n));
    }
}