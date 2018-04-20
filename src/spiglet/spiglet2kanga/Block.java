package spiglet.spiglet2kanga;

import java.util.*;
import spiglet.syntaxtree.*;
import spiglet.visitor.*;

class Block { // Basic Block in CFG
    Set<Integer> in, out, use, def;
    private Set<Block> preds; // predecessors

    List<Stmt> stmts;

    boolean finishRIG = false;

    Block() {
        in = new HashSet<Integer>();
        out = new HashSet<Integer>();
        use = new HashSet<Integer>();
        def = new HashSet<Integer>();
        preds = new HashSet<Block>();
        stmts = new LinkedList<Stmt>();
    }

    private int getVal(Temp n) {
        return Integer.parseInt(n.f1.f0.toString());
    }

    private Temp deadTemp() {
        return new Temp(new IntegerLiteral(new NodeToken("-1")));
    }

    void accept(Stmt n) {
        stmts.add(n);
    }

    int Size() {
        return stmts.size();
    }

    void addPred(Block b) {
        preds.add(b);
    }

    void removePred(Block b) {
        preds.remove(b);
    }

    Set<Block> Preds() {
        return preds;
    }

    void buildUseDef() {
        class SetBuilder extends DepthFirstVisitor {
            public void visit(HLoadStmt n) {
                use.remove(getVal(n.f1));
                def.add(getVal(n.f1));

                n.f2.accept(this);
            }

            public void visit(MoveStmt n) {
                use.remove(getVal(n.f1));
                def.add(getVal(n.f1));

                n.f2.accept(this);
            }

            public void visit(Temp n) {
                use.add(getVal(n));
            }
        }

        SetBuilder sb = new SetBuilder();
        for (ListIterator<Stmt> i = stmts.listIterator(stmts.size()); i.hasPrevious();) {
            Stmt st = i.previous();
            st.accept(sb);
        }
    }

    void addEdge(Graph g, Set<Integer> s) {
        for (int i : s)
            for (int j : s)
                g.addEdge(i, j);
    }

    void buildRIG(Graph g) { // find edges
        if (finishRIG)
            return;

        Set<Integer> live = new HashSet<Integer>(out);
        class SetBuilder extends GJVoidDepthFirst<Graph> {
            public void visit(HLoadStmt n, Graph g) {
                int t = getVal(n.f1);
                if (!live.contains(t))
                    n.f1 = deadTemp();
                else
                    live.remove(getVal(n.f1));

                n.f2.accept(this, g);
            }

            public void visit(MoveStmt n, Graph g) {
                int t = getVal(n.f1);
                if (!live.contains(t))
                    n.f1 = deadTemp();
                else
                    live.remove(getVal(n.f1));

                n.f2.accept(this, g);
            }

            public void visit(Call n, Graph g) {
                g.protect(live);
                
                n.f1.accept(this, g);
                n.f3.accept(this, g);
            }

            public void visit(Temp n, Graph g) {
                live.add(getVal(n));
            }
        }

        SetBuilder sb = new SetBuilder();
        for (ListIterator<Stmt> i = stmts.listIterator(stmts.size()); i.hasPrevious();) {
            addEdge(g, live);

            Stmt st = i.previous();
            st.accept(sb, g);
        }

        addEdge(g, in);

        finishRIG = true;
    }
}