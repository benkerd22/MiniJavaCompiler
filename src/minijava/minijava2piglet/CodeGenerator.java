package minijava.minijava2piglet;

import java.util.*;
import minijava.syntaxtree.*;
import minijava.visitor.*;
import minijava.symbol.*;

class Label {
    private static int n = 0; // how many Labels are there

    public static void init() {
        //n = 0;
    }

    public static String getnew() {
        n++;
        return "L" + n;
    }
}

class Reg {
    private static int n = 0;

    public static void init() {
        n = 0;
    }

    public static int getnew() {
        n++;
        return n;
    }
}

public class CodeGenerator extends GJDepthFirst<JVar, Scope> {
    public CodeGenerator() {
        Reg.init();
    }

    public JVar visit(VarDeclaration n, Scope scope) {
        scope.declare(MJava.getType(n.f0.f0.choice), n.f1);
        scope.getVar(n.f1).bind(Reg.getnew());

        return null;
    }

    public JVar visit(FormalParameter n, Scope scope) {
        scope.declare(MJava.getType(n.f0.f0.choice), n.f1);

        // the parameters of a func always bind from 1,2..
        // remember that TEMP 0 is for "this"
        scope.getVar(n.f1).bind(Reg.getnew());

        return null;
    }

    public JVar visit(Block n, Scope scope) {
        Scope new_scope = new Scope(scope);
        n.f1.accept(this, new_scope);

        return null;
    }

    public JVar visit(AssignmentStatement n, Scope scope) {
        JVar left = n.f0.accept(this, scope);
        JVar right = n.f2.accept(this, scope);

        // there is no volatile right, guaranteed by "Expression"
        // so we focus on volatile left
        if (left.isVola()) {
            // we always set the "." bias to TEMP 0
            left.setBase(0);

            int treg = Reg.getnew();
            Code.mov(treg, right.Reg());
            Code.store(left.baseReg(), left.Bias(), treg);
        } else
            Code.mov(left.Reg(), right.Reg());

        return null;
    }

    public JVar visit(ArrayAssignmentStatement n, Scope scope) {
        JVar a = n.f0.accept(this, scope);
        JType ele = ((JArray) a.Type()).ElementType(); // element type

        if (a.isVola()) {
            // we always set the "." bias to TEMP 0; cas' only "this." exists here
            a.setBase(0);

            int treg = Reg.getnew();
            Code.load(treg, a.baseReg(), a.Bias());
            a.bind(treg);
        }

        JVar exp = n.f2.accept(this, scope);
        JVar right = n.f5.accept(this, scope);

        String ok = Label.getnew();
        int preg = Reg.getnew(); // address pointer
        int lpreg = Reg.getnew(); // address pointer (.length)
        int lreg = Reg.getnew(); // length
        int ereg = Reg.getnew();

        Code.minus(lpreg, a.Reg(), "4");
        Code.load(lreg, lpreg, 0);

        Code.times(preg, exp.Reg(), Integer.toString(ele.Size()));
        Code.lt(ereg, preg, "TEMP " + lreg);
        Code.lt(ereg, ereg, "1");
        Code.jump(ok, ereg);
        Code.error(); // out of index

        Code.label(ok);
        Code.plus(preg, a.Reg(), "TEMP " + preg);
        Code.store(preg, 0, right.Reg());

        return null;
    }

    public JVar visit(IfStatement n, Scope scope) {
        JVar exp;
        //String t = Label.getnew();
        String f = Label.getnew(); // false
        String exit = Label.getnew();

        exp = n.f2.accept(this, scope);
        Code.jump(f, exp.Reg());

        n.f4.accept(this, scope);
        Code.jump(exit, -1);

        Code.label(f);
        n.f6.accept(this, scope);

        Code.label(exit);

        return null;
    }

    public JVar visit(WhileStatement n, Scope scope) {
        JVar exp;
        String exit = Label.getnew();
        String loop = Label.getnew();

        exp = n.f2.accept(this, scope);
        Code.jump(exit, exp.Reg());

        Code.label(loop);
        n.f4.accept(this, scope);

        exp = n.f2.accept(this, scope);
        Code.jump(exit, exp.Reg());
        Code.jump(loop, -1);

        Code.label(exit);

        return null;
    }

    public JVar visit(PrintStatement n, Scope scope) {
        JVar exp = n.f2.accept(this, scope);

        Code.print(exp.Reg());

        return null;
    }

    public JVar visit(Expression n, Scope scope) {
        return n.f0.accept(this, scope);
    }

    public JVar visit(AndExpression n, Scope scope) {
        JVar a = n.f0.accept(this, scope);
        JVar b = n.f2.accept(this, scope);

        int rreg = Reg.getnew(); // result

        Code.lt(rreg, a.Reg(), "1");
        Code.lt(rreg, rreg, "TEMP " + b.Reg());

        return new JVar(n, MJava.Boolean()).bind(rreg);
    }

    public JVar visit(CompareExpression n, Scope scope) {
        JVar a = n.f0.accept(this, scope);
        JVar b = n.f2.accept(this, scope);

        int rreg = Reg.getnew(); // result

        Code.lt(rreg, a.Reg(), "TEMP " + b.Reg());

        return new JVar(n, MJava.Boolean()).bind(rreg);
    }

    public JVar visit(PlusExpression n, Scope scope) {
        JVar a = n.f0.accept(this, scope);
        JVar b = n.f2.accept(this, scope);

        int rreg = Reg.getnew(); // result

        Code.plus(rreg, a.Reg(), "TEMP " + b.Reg());

        return new JVar(n, MJava.Int()).bind(rreg);
    }

    public JVar visit(MinusExpression n, Scope scope) {
        JVar a = n.f0.accept(this, scope);
        JVar b = n.f2.accept(this, scope);

        int rreg = Reg.getnew(); // result

        Code.minus(rreg, a.Reg(), "TEMP " + b.Reg());

        return new JVar(n, MJava.Int()).bind(rreg);
    }

    public JVar visit(TimesExpression n, Scope scope) {
        JVar a = n.f0.accept(this, scope);
        JVar b = n.f2.accept(this, scope);

        int rreg = Reg.getnew(); // result

        Code.times(rreg, a.Reg(), "TEMP " + b.Reg());

        return new JVar(n, MJava.Int()).bind(rreg);
    }

    public JVar visit(ArrayLookup n, Scope scope) {
        JVar a = n.f0.accept(this, scope);
        JType ele = ((JArray) a.Type()).ElementType(); // element type
        
        JVar exp = n.f2.accept(this, scope);

        String ok = Label.getnew();
        int preg = Reg.getnew(); // address pointer
        int lpreg = Reg.getnew(); // address pointer (.length)
        int lreg = Reg.getnew(); // length
        int ereg = Reg.getnew();
        int vreg = Reg.getnew(); // value

        Code.minus(lpreg, a.Reg(), "4");
        Code.load(lreg, lpreg, 0);

        Code.times(preg, exp.Reg(), Integer.toString(ele.Size()));
        Code.lt(ereg, preg, "TEMP " + lreg);
        Code.lt(ereg, ereg, "1");
        Code.jump(ok, ereg);
        Code.error(); // out of index
        
        Code.label(ok);
        Code.plus(preg, a.Reg(), "TEMP " + preg);
        Code.load(vreg, preg, 0);

        return new JVar(n, ele).bind(vreg);
    }

    public JVar visit(ArrayLength n, Scope scope) {
        JVar a = n.f0.accept(this, scope);

        int lpreg = Reg.getnew(); // address pointer (.length)
        int lreg = Reg.getnew(); // length

        Code.minus(lpreg, a.Reg(), "4");
        Code.load(lreg, lpreg, 0);

        return new JVar(n, MJava.Int()).bind(lreg);
    }

    public JVar visit(MessageSend n, Scope scope) {
        class Entry {
            CodeGenerator cg;
            Scope scope;
            ArrayList<Integer> list;
        }

        class ExpressionListHelper extends GJVoidDepthFirst<Entry> {
            public void visit(Expression n, Entry e) {
                JVar exp = n.accept(e.cg, e.scope);
                e.list.add(exp.Reg());
            }
        }

        JVar a = n.f0.accept(this, scope);
        JClass c = (JClass) a.Type();

        JMethod m = c.queryMethod(n.f2);
        int ms = m.Owner().queryMethodStatus(n.f2); // query status in the method's REAL owner
        String funcExp;

        if (ms == 0) {
            // static call
            funcExp = "f" + m.Index() + "_" + m.Owner().Name() + "_" + m.Name();
        } else {
            // polymorphism, query func in the vTable
            int preg = Reg.getnew(); // VPTR
            int freg = Reg.getnew(); // func address
            int mb = m.Owner().querymBiases(n.f2); // VPTR bias

            Code.load(preg, a.Reg(), 0);
            Code.load(freg, preg, mb);
            funcExp = "TEMP " + freg;
        }

        Entry e = new Entry();
        e.cg = this;
        e.scope = scope;
        e.list = new ArrayList<Integer>();
        e.list.add(a.Reg()); // put "this" in TEMP 0

        n.f4.accept(new ExpressionListHelper(), e);

        int rreg = Reg.getnew();

        if (e.list.size() <= 20) {
            Code.call(rreg, funcExp, e.list.toArray(new Integer[e.list.size()]));
        } else {
            int areg = Reg.getnew(); // array to store parameters
            Code.malloc(areg, Integer.toString(e.list.size() * 4));

            int i = 0;
            for (int reg : e.list) {
                Code.store(areg, i, reg);
                i += 4;
            }

            Code.call(rreg, funcExp, areg);
        }

        return new JVar(n, m.Ret()).bind(rreg);
    }

    public JVar visit(PrimaryExpression n, Scope scope) {
        // in this node, variable is TO BE USED
        JVar v = n.f0.accept(this, scope);
        if (v.isVola()) {
            int treg = Reg.getnew();

            // there is no "." in minijava, only "this.*" exists
            // and "this" always store in TEMP 0
            // so we always set the "." bias to TEMP 0
            v.setBase(0);
            Code.load(treg, v.baseReg(), v.Bias());

            return new JVar(n, v.Type()).bind(treg);
        } else
            return v;
    }

    public JVar visit(IntegerLiteral n, Scope scope) {
        int treg = Reg.getnew();
        Code.mov(treg, n.f0.toString());
        return new JVar(n, MJava.Int()).bind(treg);
    }

    public JVar visit(TrueLiteral n, Scope scope) {
        int treg = Reg.getnew();
        Code.mov(treg, "1");
        return new JVar(n, MJava.Boolean()).bind(treg);
    }

    public JVar visit(FalseLiteral n, Scope scope) {
        int treg = Reg.getnew();
        Code.mov(treg, "0");
        return new JVar(n, MJava.Boolean()).bind(treg);
    }

    public JVar visit(Identifier n, Scope scope) {
        // if we reach here, the <Identifier> n MUST be treated as a VARIABLE rather than class name, method name etc.
        // only do the "get".
        // cas' we still don't know whether this variable is to be stored or used
        return scope.getVar(n);
    }

    public JVar visit(ThisExpression n, Scope scope) {
        return new JVar(n, scope.Owner()).bind(0);
    }

    public JVar visit(ArrayAllocationExpression n, Scope scope) {
        JVar exp = n.f3.accept(this, scope);
        int sreg = Reg.getnew(); // size reg
        int areg = Reg.getnew(); // size reg for allocate
        int preg = Reg.getnew(); // array pointer reg

        Code.times(sreg, exp.Reg(), Integer.toString(MJava.Int().Size())); // element is int
        Code.plus(areg, sreg, "4"); // 4 bytes space for .length
        Code.malloc(preg, "TEMP " + areg);
        Code.store(preg, 0, sreg); // a[-1] <== size
        Code.plus(preg, preg, "4"); // &a[-1] -> &a[0]
        Code.call(Reg.getnew(), "calloc", preg, sreg);

        return new JVar(n, MJava.ArrayInt()).bind(preg);
    }

    public JVar visit(AllocationExpression n, Scope scope) {
        JClass c = (JClass) MJava.getType(n.f1);
        int sreg = Reg.getnew(); // size reg
        int preg = Reg.getnew(); // class pointer reg
        int vreg = Reg.getnew(); // VPTR

        Code.mov(sreg, Integer.toString(c.instanceSize() + 4)); // 4 bytes for VPTR
        Code.malloc(preg, "TEMP " + sreg);
        Code.call(Reg.getnew(), "calloc", preg, sreg); //calloc
        Code.call(vreg, "new_" + c.Name()); // get VPTR
        Code.store(preg, 0, vreg); // store VPTR

        return new JVar(n, c).bind(preg);
    }

    public JVar visit(NotExpression n, Scope scope) {
        JVar v = n.f1.accept(this, scope);
        int treg = Reg.getnew();

        Code.lt(treg, v.Reg(), "1");

        return new JVar(n, MJava.Boolean()).bind(treg);
    }

    public JVar visit(BracketExpression n, Scope scope) {
        return n.f1.accept(this, scope);
    }
}