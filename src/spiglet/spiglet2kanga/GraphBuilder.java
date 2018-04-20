package spiglet.spiglet2kanga;

import spiglet.syntaxtree.*;
import spiglet.visitor.*;

class GraphBuilder extends GJVoidDepthFirst<Graph> {
    public void visit(Goal n, Graph g) {
        g = new Graph(0);
        n.f1.accept(this, g);
        g.stop(null);
        g.regAlloc();

        g.begin(null);
        n.f1.accept(new CodeGenerator(), g);
        g.end();

        n.f3.accept(this, null);
    }

    public void visit(Procedure n, Graph g) {
        int argn = Integer.parseInt(n.f2.f0.toString());

        g = new Graph(argn);
        n.f4.accept(this, g);
        g.regAlloc();

        g.begin(n.f0);
        n.f4.accept(new CodeGenerator(), g);
        g.end();
    }

    public void visit(Stmt n, Graph g) {
        g.accept(n);
    }

    public void visit(StmtExp n, Graph g) {
        n.f1.accept(this, g);
        g.stop(n.f3);
    }

    public void visit(Label n, Graph g) {
        g.accept(n);
    }
}