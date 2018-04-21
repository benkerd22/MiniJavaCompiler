package spiglet.spiglet2kanga;

import spiglet.syntaxtree.*;
import spiglet.visitor.*;

class GraphBuilder extends GJVoidDepthFirst<Graph> {
    public void visit(Goal n, Graph g) {
        g = new Graph(0);
        n.f1.accept(this, g);
        g.ret(null);
        g.regAlloc();

        g.buildCode(null);

        n.f3.accept(this, null);
    }

    public void visit(Procedure n, Graph g) {
        int argn = Integer.parseInt(n.f2.f0.toString());

        g = new Graph(argn);
        n.f4.accept(this, g);
        g.regAlloc();

        g.buildCode(n.f0);
    }

    public void visit(Stmt n, Graph g) {
        g.stmt(n);
    }

    public void visit(StmtExp n, Graph g) {
        n.f1.accept(this, g);
        g.ret(n.f3);
    }

    public void visit(Label n, Graph g) {
        g.stmt(n);
    }
}