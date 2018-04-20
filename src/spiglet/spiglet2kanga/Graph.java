package spiglet.spiglet2kanga;

import java.util.*;
import spiglet.syntaxtree.*;
import spiglet.visitor.*;

class Graph { // CFG (Control Flow Graph), or a set of basic Blocks in a function
    private Set<Block> blocks;
    private Map<Integer, Block> entry; // Label entry ==> Block
    private Map<Block, Integer> jlist; // Block ==> jump to label

    private Block last, now;

    private int argn; // argn of this func
    private int maxCallArgn; // max argn of any call in this func
    private Map<Integer, Set<Integer>> RIG; // Register Interference Graph. row id ==> elements in a row
    private Set<Integer> protect; // vars that MUST be assigned with s* registers

    private Map<Integer, Integer> alloc; // TEMP i ==> real register
    private Map<Integer, Integer> spill; // TEMP i ==> spill ID
    private boolean[] usedReg;
    private int usedS;
    // Reg IDs
    //   0  ~ 9  : t*
    //   10 ~ 17 : s*
    //   18 ~ 19 : v*
    //   20 ~ 23 : a*

    Graph(int argn_) {
        blocks = new HashSet<Block>();
        entry = new HashMap<Integer, Block>();
        jlist = new HashMap<Block, Integer>();
        last = null;
        now = new Block();
        blocks.add(now);

        argn = argn_;
        maxCallArgn = 0;
        RIG = new HashMap<Integer, Set<Integer>>();
        protect = new HashSet<Integer>();
        alloc = new HashMap<Integer, Integer>();
        spill = new HashMap<Integer, Integer>();
        usedReg = new boolean[24];
        usedS = 0;
    }

    private int getVal(Label n) {
        return Integer.parseInt(n.f0.toString().substring(1));
    }

    // ***** Block Build *****

    private void newBlock() { // new Block always store in now
        last = now;
        now = new Block();
        blocks.add(now);
        now.addPred(last);
    }

    private void checkCall(Stmt n) {
        final int[] count = { 0 };
        class ArgsListHelper extends DepthFirstVisitor {
            public void visit(Temp n) {
                count[0]++;
            }
        }

        if (n.f0.choice instanceof MoveStmt) {
            MoveStmt m = (MoveStmt) n.f0.choice;
            if (m.f2.f0.choice instanceof Call) {
                Call c = (Call) m.f2.f0.choice;
                c.f3.accept(new ArgsListHelper());
                if (count[0] > maxCallArgn)
                    maxCallArgn = count[0];
            }
        }
    }

    void accept(Label n) {
        newBlock();

        entry.put(getVal(n), now);
    }

    void accept(Stmt n) {
        checkCall(n);

        now.accept(n);

        Node st = n.f0.choice;
        int jump = 0;
        if (st instanceof JumpStmt) {
            jump = getVal(((JumpStmt) st).f1);
        } else if (st instanceof CJumpStmt) {
            jump = getVal(((CJumpStmt) st).f2);
        } else if (st instanceof ErrorStmt) {
            jump = -1; // jump to exit
        }

        if (jump != 0) {
            jlist.put(now, jump);
            newBlock();
            if (st instanceof JumpStmt || st instanceof ErrorStmt)
                now.removePred(last);
        }
    }

    void stop(SimpleExp n) {
        newBlock();
        entry.put(-1, now);

        if (n != null) // if has a return expression
            now.accept(new Stmt(new NodeChoice(new PrintStmt(n)))); // a fake PrintStmt for consistency
    }

    private void buildBlockPreds() {
        for (Map.Entry<Block, Integer> e : jlist.entrySet()) {
            Block pred = e.getKey();
            int label = e.getValue();
            Block succ = entry.get(label);
            succ.addPred(pred);
        }
    }

    // ***** Register Allocation *****

    void addEdge(int i, int j) {
        if (i == j) {
            if (!RIG.containsKey(i)) {
                RIG.put(i, new HashSet<Integer>());
            }

            return;
        }

        if (!RIG.containsKey(i)) {
            RIG.put(i, new HashSet<Integer>());
        }
        if (!RIG.containsKey(j)) {
            RIG.put(j, new HashSet<Integer>());
        }

        RIG.get(i).add(j);
        RIG.get(j).add(i);
    }

    void protect(Set<Integer> s) {
        protect.addAll(s);
    }

    private void spill(int id) {
        if (alloc.containsKey(id))
            alloc.remove(id);
        spill.put(id, spill.size());
    }

    private void preColorRIG() {
        // allocate protected vars first
        Map<Integer, Integer> d = new TreeMap<Integer, Integer>(Collections.reverseOrder()); // degree

        for (int i : protect) {
            int deg = 0;
            for (int j : RIG.get(i))
                if (protect.contains(j))
                    deg++;
            d.put(i, deg);
        }

        Stack<Integer> s = new Stack<Integer>();
        int maxReg = 8; // s0 ~ s8
        while (!d.isEmpty()) {
            int i = -1;
            for (Map.Entry<Integer, Integer> e : d.entrySet()) {
                if (e.getValue() < maxReg) {
                    i = e.getKey();
                    s.push(i);
                    break;
                }
            }

            if (i < 0) {
                //spill
                int m = maxReg;
                for (Map.Entry<Integer, Integer> e : d.entrySet()) {
                    if (e.getValue() >= m) {
                        m = e.getValue();
                        i = e.getKey();
                    }
                }

                spill(i);
            }

            for (int j : RIG.get(i))
                if (protect.contains(j) && d.containsKey(j))
                    d.put(j, d.get(j) - 1);
            d.remove(i);
        }

        Set<Integer> free = new TreeSet<Integer>();
        while (!s.isEmpty()) {
            int i = s.pop();

            free.clear();
            for (int k = Code.s0; k <= Code.s7; k++)
                free.add(k);

            for (int j : RIG.get(i))
                if (protect.contains(j) && alloc.containsKey(j))
                    free.remove(alloc.get(j));

            int reg = free.iterator().next();
            alloc.put(i, reg);
        }
    }

    private void colorRIG() {
        Map<Integer, Integer> d = new TreeMap<Integer, Integer>(Collections.reverseOrder()); // degree

        for (Map.Entry<Integer, Set<Integer>> e : RIG.entrySet()) {
            d.put(e.getKey(), e.getValue().size());
        }

        Stack<Integer> s = new Stack<Integer>();
        int maxReg = 18;
        while (!d.isEmpty()) {
            int i = -1;
            for (Map.Entry<Integer, Integer> e : d.entrySet()) {
                if (e.getValue() < maxReg) {
                    i = e.getKey();
                    s.push(i);
                    break;
                }
            }

            if (i < 0) {
                //spill
                int m = maxReg;
                for (Map.Entry<Integer, Integer> e : d.entrySet()) {
                    if (e.getValue() >= m) {
                        m = e.getValue();
                        i = e.getKey();
                    }
                }

                spill(i);
            }

            for (int j : RIG.get(i))
                if (d.containsKey(j))
                    d.put(j, d.get(j) - 1);
            d.remove(i);
        }

        Set<Integer> free = new TreeSet<Integer>();
        while (!s.isEmpty()) {
            int i = s.pop();

            if (alloc.containsKey(i))
                continue;

            free.clear();
            for (int k = Code.t0; k <= Code.s7; k++)
                free.add(k);

            for (int j : RIG.get(i))
                if (alloc.containsKey(j)) {
                    free.remove(alloc.get(j));
                }

            int reg = free.iterator().next();
            alloc.put(i, reg);
        }
    }

    private void checkReg() {
        Arrays.fill(usedReg, false);
        for (Map.Entry<Integer, Integer> e : alloc.entrySet())
            usedReg[e.getValue()] = true;

        for (int i = Code.s0; i <= Code.s7; i++)
            if (usedReg[i])
                usedS++;
    }

    private void buildSpill() {
        int base = Math.max(argn - 4, 0) + usedS;
        for (Map.Entry<Integer, Integer> e : spill.entrySet())
            spill.put(e.getKey(), e.getValue() + base);
    }

    void regAlloc() {
        buildBlockPreds();

        Block.liveAnalysis(blocks, this);

        preColorRIG();
        colorRIG();

        checkReg();
        buildSpill();
    }

    // ***** Code Build *****

    int getTemp(Temp n) {
        return Integer.parseInt(n.f1.f0.toString());
    }

    int getReg(Temp n) {
        return getReg(n, Code.v1, true);
    }

    int getReg(Temp n, int tmp, boolean autoLoad) {
        int t = getTemp(n);
        return getReg(t, tmp, autoLoad);
    }

    int getReg(int t, int tmp, boolean autoLoad) {
        // if autoLoad: spilled var --(load)--> reg tmp
        if (spill.containsKey(t)) {
            if (autoLoad)
                Code.lreg(tmp, spill.get(t));
            return tmp;
        } else if (alloc.containsKey(t))
            return alloc.get(t);
        else
            return -1; // dead temp
    }

    int getSpill(Temp n) {
        int t = getTemp(n);
        if (spill.containsKey(t))
            return spill.get(t);
        else
            return -1;
    }

    String getExp(SimpleExp n) {
        return getExp(n.f0.choice, Code.v1);
    }

    String getExp(Node n, int tmp) {
        if (n instanceof Temp) {
            int reg = getReg((Temp) n, tmp, true);
            if (reg != -1)
                return Code.REG[reg];
            else
                return "";
        }
        else if (n instanceof IntegerLiteral)
            return ((IntegerLiteral) n).f0.toString();
        else if (n instanceof Label)
            return ((Label) n).f0.toString();
        else
            System.out.println("ERROR");

        return "";
    }

    void begin(Label funcID) {
        String code;
        if (funcID != null)
            code = funcID.f0.toString();
        else
            code = "MAIN";

        code += " [" + argn + "][" + (Math.max(argn - 4, 0) + usedS + spill.size()) + "][" + maxCallArgn + "]\n";

        Code.emit(code, "", "");

        int j = 0;
        for (int i = Code.s0; i <= Code.s7; i++) {
            if (usedReg[i]) {
                Code.sreg(j, i);
                j++;
            }
        }

        for (int i = 0; i < argn; i++) {
            int reg = getReg(i, Code.v1, false);

            if (reg == -1)
                continue;

            if (i < 4)
                Code.mov(reg, i + Code.a0);
            else
                Code.lreg(reg, i - 4);
            // TODO: SPILLED --> SPILLED ?

            if (reg == Code.v1) {
                Code.sreg(spill.get(i), reg);
            }
        }
    }

    void end() {
        int j = usedS - 1;
        for (int i = Code.s7; i >= Code.s0; i--) {
            if (usedReg[i]) {
                Code.lreg(i, j);
                j--;
            }
        }

        Code.emit("END\n", "", "\n");
    }

    // ***** Attribute *****

    void show() {
        for (Map.Entry<Integer, Integer> e : alloc.entrySet()) {
            System.out.println("\tTEMP " + e.getKey() + " ==> " + e.getValue());
        }
        for (Map.Entry<Integer, Integer> e : spill.entrySet()) {
            System.out.println("\tTEMP " + e.getKey() + " SPILL " + e.getValue());
        }
        System.out.println("\n");
    }
}
