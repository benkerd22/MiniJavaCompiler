package spiglet.spiglet2kanga;

import java.util.*;
import spiglet.syntaxtree.*;
import spiglet.visitor.*;

class CodeGenerator extends GJVoidDepthFirst<Graph> {
    public void visit(NoOpStmt n, Graph g) {
        Code.noop();
    }

    public void visit(ErrorStmt n, Graph g) {
        Code.error();
    }

    public void visit(CJumpStmt n, Graph g) {
        Code.jump(n.f2.f0.toString(), g.getReg(n.f1));
    }

    public void visit(JumpStmt n, Graph g) {
        Code.jump(n.f1.f0.toString(), -1);
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
            Code.sreg(g.spill.get(g.getTemp(n.f1)), Code.v1);
    }

    public void visit(MoveStmt n, Graph g) {
        int dreg = g.getReg(n.f1, Code.v1, false);

        n.f2.accept(this, g);

        if (dreg == -1)
            return;

        Code.mov(dreg);

        if (dreg == Code.v1)
            Code.sreg(g.spill.get(g.getTemp(n.f1)), Code.v1);
    }

    public void visit(PrintStmt n, Graph g) {
        Code.print(g.getExp(n.f1));
    }

    public void visit(StmtExp n, Graph g) {
        n.f1.accept(this, g);
        Code.mov_(g.getExp(n.f3));
        Code.mov(Code.v0);
    }

    public void visit(Call n, Graph g) {
        class ArgsListHelper extends GJVoidDepthFirst<List<Integer>> {
            public void visit(Temp n, List<Integer> list) {
                list.add(g.getReg(n));
            }
        }

        List<Integer> list = new ArrayList<Integer>();
        n.f3.accept(new ArgsListHelper(), list);

        for (int i = 0; i < list.size(); i++)
            Code.pass(list.get(i), i);

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

    public void visit(Label n, Graph g) {
        // only from StmtList -> Label | Procedure -> Label can reach here
        Code.emit(n.f0.toString(), "", "");
    }
}