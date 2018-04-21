package spiglet.spiglet2kanga;

import java.util.*;
import spiglet.syntaxtree.*;
import spiglet.visitor.*;

class Block { // Basic Block in CFG
    private String label;
    private Set<Integer> in, out, use, def;
    private Set<Block> preds, succs; // predecessors, successors
    private List<Stmt> stmts; // statements
    private int vstmts; // valid statements (after dead code elimination)

    Block() {
        label = "";
        in = new HashSet<Integer>();
        out = new HashSet<Integer>();
        use = new HashSet<Integer>();
        def = new HashSet<Integer>();
        preds = new HashSet<Block>();
        succs = new HashSet<Block>();
        stmts = new LinkedList<Stmt>();
        vstmts = 0;
    }

    // ***** Block Build *****
    void accept(String label_) {
        label = label_;
    }

    void accept(Stmt n) {
        stmts.add(n);
    }

    void addPred(Block b) {
        preds.add(b);
        b.succs.add(this);
    }

    void removePred(Block b) {
        preds.remove(b);
        b.succs.remove(this);
    }

    static void checkDeadBlock(List<Block> blocks) {
        List<Block> t = new LinkedList<Block>(blocks);
        for (Block b : t)
            if (b.preds.size() == 0) { // b is a dead block
                for (Block succ : b.succs)
                    succ.preds.remove(b);
                blocks.remove(b);
            }
    }

    static void checkLabel(List<Block> blocks, Graph g) { // kill and merge useless labels
        Block last = null;
        for (Block b : blocks) {
            if (last != null && last.vstmts == 0 && last.label != "" && b.label != "") {
                g.mergeLabel(last.label, b.label); // last.label = b.label
                last.label = "";
            }

            boolean useless = true;
            for (Block pred : b.preds) {
                if (pred != last) {
                    useless = false;
                    break;
                }
            }

            if (useless)
                g.killLabel(b.label);

            last = b;
        }
    }

    // ***** Live Analysis *****

    private int getVal(Temp n) {
        return Integer.parseInt(n.f1.f0.toString());
    }

    private void addEdge(Graph g, Set<Integer> s) {
        if (g == null)
            return;

        for (int i : s)
            for (int j : s)
                g.addEdge(i, j);
    }

    private boolean scan(Graph g, boolean init) { // init : the first time to build use, def?
        final int[] vn = new int[1]; // valid Stmt count
        final Set<Integer> live = new HashSet<Integer>(out);
        use.clear();
        def.clear();
        vn[0] = stmts.size();

        class BlockScanner extends GJVoidDepthFirst<Graph> {
            private Temp deadTemp() {
                return new Temp(new IntegerLiteral(new NodeToken("-1")));
            }

            public void visit(NoOpStmt n, Graph g) {
                vn[0]--;
            }

            public void visit(HLoadStmt n, Graph g) {
                int t = getVal(n.f1);
                if (!live.contains(t) && !init) {
                    n.f1 = deadTemp();
                    vn[0]--;
                } else {
                    live.remove(t);
                    use.remove(t);
                    def.add(t);

                    n.f2.accept(this, g);
                }
            }

            public void visit(MoveStmt n, Graph g) {
                int t = getVal(n.f1);
                if (!live.contains(t) && !init) {
                    n.f1 = deadTemp();

                    if (n.f2.f0.choice instanceof Call)
                        n.f2.accept(this, g);
                    else
                        vn[0]--;
                } else {
                    live.remove(t);
                    use.remove(t);
                    def.add(t);

                    n.f2.accept(this, g);
                }
            }

            public void visit(Call n, Graph g) {
                if (g != null)
                    g.protect(live);

                n.f1.accept(this, g);
                n.f3.accept(this, g);
            }

            public void visit(Temp n, Graph g) {
                live.add(getVal(n));
                use.add(getVal(n));
            }
        }

        BlockScanner bs = new BlockScanner();
        for (ListIterator<Stmt> i = stmts.listIterator(stmts.size()); i.hasPrevious();) {
            addEdge(g, live);

            Stmt st = i.previous();
            st.accept(bs, g);
        }
        vstmts = vn[0];

        addEdge(g, live);

        return live.equals(in);
    }

    private static void FixedPoint(Set<Block> blocks) {
        Set<Block> changed = new HashSet<Block>(blocks);
        while (!changed.isEmpty()) {
            Block b = changed.iterator().next();
            changed.remove(b);

            b.out.clear();
            for (Block succ : b.succs)
                b.out.addAll(succ.in);

            Set<Integer> newin = new HashSet<Integer>(b.out);
            newin.removeAll(b.def);
            newin.addAll(b.use);

            if (b.in.equals(newin))
                continue;

            b.in.clear();
            b.in.addAll(newin);

            for (Block pred : b.preds)
                changed.add(pred);
        }
    }

    static void liveAnalysis(Set<Block> blocks, Graph g) {
        for (Block b : blocks)
            b.scan(null, true);

        while (true) {
            FixedPoint(blocks);

            boolean f = true;
            for (Block b : blocks)
                f = f && b.scan(null, false);

            if (f)
                break;
        }

        for (Block b : blocks)
            b.scan(g, false);
    }

    // ***** Code Build *****

    void argnInit(int argn, Graph g) { // only start block will invoke this
        for (int args : out) {
            if (args >= argn)
                System.out.println("SOURCE FILE ERROR");

            int reg = g.getReg(args, Code.v1, false);
            if (reg == -1)
                continue;

            if (args < 4)
                Code.mov(reg, Code.a0 + args);
            else
                Code.lreg(reg, args - 4); // TODO: SPILLED --> SPILLED ?

            if (reg == Code.v1)
                Code.sreg(g.getSpill(args), reg);
        }
    }

    void buildCode(Graph g) {
        Code.emit(g.getLabel(label), "", "");

        CodeGenerator cg = new CodeGenerator();
        for (Stmt s : stmts)
            s.accept(cg, g);
    }
}