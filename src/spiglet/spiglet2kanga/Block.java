package spiglet.spiglet2kanga;

import java.util.*;
import spiglet.syntaxtree.*;
import spiglet.visitor.*;

class Block { // Basic Block in CFG
    private Set<Integer> in, out, use, def;
    private Set<Block> preds; // predecessors
    private List<Stmt> stmts; // statements

    Block() {
        in = new HashSet<Integer>();
        out = new HashSet<Integer>();
        use = new HashSet<Integer>();
        def = new HashSet<Integer>();
        preds = new HashSet<Block>();
        stmts = new LinkedList<Stmt>();
    }

    // ***** Block Build *****

    void accept(Stmt n) {
        stmts.add(n);
    }

    void addPred(Block b) {
        preds.add(b);
    }

    void removePred(Block b) {
        preds.remove(b);
    }

    // ***** Live Analysis *****

    private int getVal(Temp n) {
        return Integer.parseInt(n.f1.f0.toString());
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

    void buildUseDef(Temp n) {
        // special build for "RETURN TEMP i" in exit block
        use.add(getVal(n));
    }

    static void buildInOut(Set<Block> changed) {
        while (!changed.isEmpty()) {
            Block b = changed.iterator().next();
            changed.remove(b);

            Set<Integer> tmp = new HashSet<Integer>(b.out);
            tmp.removeAll(b.def);
            tmp.addAll(b.use);
            b.in.addAll(tmp);

            for (Block pred : b.preds) {
                if (pred.out.containsAll(b.in))
                    continue;
                pred.out.addAll(b.in);
                changed.add(pred);
            }
        }
    }

    private void addEdge(Graph g, Set<Integer> s) {
        for (int i : s)
            for (int j : s)
                g.addEdge(i, j);
    }

    void buildRIG(Graph g) { // find RIG edges
        Set<Integer> live = new HashSet<Integer>(out);
        class SetBuilder extends GJVoidDepthFirst<Graph> {
            private Temp deadTemp() {
                return new Temp(new IntegerLiteral(new NodeToken("-1")));
            }

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
    }
}