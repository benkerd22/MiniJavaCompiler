package spiglet.spiglet2kanga;

import spiglet.syntaxtree.*;
import spiglet.visitor.*;

class GraphBuilder extends GJVoidDepthFirst<Graph> {
    public void visit(Goal n, Graph g) {
        //System.out.println("MAIN");

        g = new Graph(0);
        n.f1.accept(this, g);
        g.stop(null);
        g.work();

        g.begin(null);
        n.f1.accept(new CodeGenerator(), g);
        g.end();

        n.f3.accept(this, null);
    }

    public void visit(Procedure n, Graph g) {
        //System.out.println("FUNC " + n.f0.f0.toString());

        int argn = Integer.parseInt(n.f2.f0.toString());
        g = new Graph(argn);
        n.f4.accept(this, g);
        g.work();

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