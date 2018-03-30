package minijava.typecheck;

import java.util.*;
import minijava.syntaxtree.*;
import minijava.visitor.*;

class ScopeBuilder extends GJDepthFirst<JVar, Scope> {
	public JVar visit(VarDeclaration n, Scope scope) {
		scope.declare(Java.get(n.f0.f0.choice), n.f1);

		return null;
	}

	public JVar visit(FormalParameter n, Scope scope) {
		scope.declare(Java.get(n.f0.f0.choice), n.f1);
		scope.getVar(n.f1).assign(); // assume arguments is initialized

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

		left.Type().Assignable(right.Type(), true, n);
		if (left.Type() == Java.ArrayInt()) {
			left.assign(right.Value());
		} else
			left.assign();

		return null;
	}

	public JVar visit(ArrayAssignmentStatement n, Scope scope) {
		JVar a = n.f0.accept(this, scope);
		if (!(a.Type() instanceof JArray)) {
			ErrorHandler.send("Type " + a.Type().Name() + " is not an array", n);
			return new JVar(n, Java.Undefined());
		}

		JVar exp = n.f2.accept(this, scope);
		Java.Int().Assignable(exp.Type(), true, n);

		JType leftType = ((JArray) a.Type()).ElementType();
		JVar right = n.f5.accept(this, scope);
		leftType.Assignable(right.Type(), true, n);

		return a.assign();
	}

	public JVar visit(IfStatement n, Scope scope) {
		JVar exp = n.f2.accept(this, scope);
		Java.Boolean().Assignable(exp.Type(), true, n);

		n.f4.accept(this, scope);
		n.f6.accept(this, scope);

		if (exp.Value() == JBoolean.True()) {
			ErrorHandler.warn("Dead code", n.f6);
		} else if (exp.Value() == JBoolean.False()) {
			ErrorHandler.warn("Dead code", n.f4);
		}

		return null;
	}

	public JVar visit(WhileStatement n, Scope scope) {
		JVar exp = n.f2.accept(this, scope);
		Java.Boolean().Assignable(exp.Type(), true, n);

		n.f4.accept(this, scope);

		if (exp.Value() == JBoolean.False()) {
			ErrorHandler.warn("Dead code", n.f4);
		} else if (exp.Value() == JBoolean.True()) {
			ErrorHandler.warn("Dead loop", n.f4);
		}

		return null;
	}

	public JVar visit(PrintStatement n, Scope scope) {
		JVar exp = n.f2.accept(this, scope);
		Java.Int().Assignable(exp.Type(), true, n);

		return null;
	}

	public JVar visit(Expression n, Scope scope) {
		return n.f0.accept(this, scope);
	}

	public JVar visit(AndExpression n, Scope scope) {
		JVar a = n.f0.accept(this, scope);
		JVar b = n.f2.accept(this, scope);

		Java.Boolean().Assignable(a.Type(), true, n);
		Java.Boolean().Assignable(b.Type(), true, n);

		long val = 0;
		if (a.Value() == JBoolean.False()) {
			ErrorHandler.warn("Dead code", n.f2);
			val = JBoolean.False();
		} else if (b.Value() == JBoolean.False()) {
			ErrorHandler.warn("Dead code", n.f0);
			val = JBoolean.False();
		}
		return new JVar(n, Java.Boolean()).assign(val);
	}

	public JVar visit(CompareExpression n, Scope scope) {
		JVar a = n.f0.accept(this, scope);
		JVar b = n.f2.accept(this, scope);

		Java.Int().Assignable(a.Type(), true, n);
		Java.Int().Assignable(b.Type(), true, n);

		return new JVar(n, Java.Boolean()).assign();
	}

	public JVar visit(PlusExpression n, Scope scope) {
		JVar a = n.f0.accept(this, scope);
		JVar b = n.f2.accept(this, scope);

		Java.Int().Assignable(a.Type(), true, n);
		Java.Int().Assignable(b.Type(), true, n);

		return new JVar(n, Java.Int()).assign();
	}

	public JVar visit(MinusExpression n, Scope scope) {
		JVar a = n.f0.accept(this, scope);
		JVar b = n.f2.accept(this, scope);

		Java.Int().Assignable(a.Type(), true, n);
		Java.Int().Assignable(b.Type(), true, n);

		return new JVar(n, Java.Int()).assign();
	}

	public JVar visit(TimesExpression n, Scope scope) {
		JVar a = n.f0.accept(this, scope);
		JVar b = n.f2.accept(this, scope);

		Java.Int().Assignable(a.Type(), true, n);
		Java.Int().Assignable(b.Type(), true, n);

		return new JVar(n, Java.Int()).assign();
	}

	public JVar visit(ArrayLookup n, Scope scope) {
		JVar a = n.f0.accept(this, scope);
		JVar exp = n.f2.accept(this, scope);

		if (!(a.Type() instanceof JArray)) {
			ErrorHandler.send("Type " + a.Type().Name() + " is not an array", n);
			return new JVar(n, new JUndefined()).assign();
		}

		Java.Int().Assignable(exp.Type(), true, n);

		if (a.Value() != 0 && exp.Value() != 0) {
			if (exp.Value() < 0 || exp.Value() >= a.Value()) {
				ErrorHandler.send("Array index out of range: " + exp.Value(), n);
			}
		}

		return new JVar(n, ((JArray) a.Type()).ElementType()).assign();
	}

	public JVar visit(ArrayLength n, Scope scope) {
		JVar exp = n.f0.accept(this, scope);
		if (!(exp.Type() instanceof JArray)) {
			ErrorHandler.send("Type " + exp.Type().Name() + " has no member length", n);
		}

		return new JVar(n, Java.Int()).assign();
	}

	public JVar visit(MessageSend n, Scope scope) {
		class Entry {
			ScopeBuilder sb;
			Scope scope;
			ArrayList<JType> list;
		}

		class ExpressionListHelper extends GJVoidDepthFirst<Entry> {
			public void visit(Expression n, Entry e) {
				JVar exp = n.accept(e.sb, e.scope);
				e.list.add(exp.Type());
			}
		}

		JVar a = n.f0.accept(this, scope);
		if (!(a.Type() instanceof JClass)) {
			ErrorHandler.send("Cannot use . over type " + a.Type().Name(), n);
			return new JVar(n, Java.Undefined()).assign();
		}

		String sid = n.f2.f0.toString();
		JMethod m = ((JClass) a.Type()).queryMethod(sid);
		if (m == null) {
			ErrorHandler.send("Class " + ((JClass) a.Type()).Name() + " has no method " + sid, n);
			return new JVar(n, Java.Undefined()).assign();
		}

		Entry e = new Entry();
		e.sb = this;
		e.scope = scope;
		e.list = new ArrayList<JType>();
		n.f4.accept(new ExpressionListHelper(), e);

		int check = 0;
		if (e.list.size() == m.List().size()) {
			for (int i = 0; i < m.List().size(); i++) {
				if (e.list.get(i) != m.List().get(i)) {
					check = i;
					break;
				}
			}
		} else
			check = e.list.size() - 1;

		if (check != 0) {
			ErrorHandler.send("Method " + m.Name() + " in Class " + ((JClass) a.Type()).Name()
					+ " is not applicateable for the paralist: " + check + "th", n);
		}

		return new JVar(n, m.Ret()).assign();
	}

	public JVar visit(PrimaryExpression n, Scope scope) {
		JVar exp = n.f0.accept(this, scope);
		if (!exp.isAssigned()) {
			ErrorHandler.send("variable " + exp.Id() + " might not have been initialized", n);
		}

		return exp;
	}

	public JVar visit(IntegerLiteral n, Scope scope) {
		try {
			int val = Integer.parseInt(n.f0.toString());
			return new JVar(n, Java.Int()).assign(val);
		} catch (NumberFormatException e) {
			ErrorHandler.send("the literal " + n.f0.toString() + " of type int is out of range", n);
			return new JVar(n, Java.Int()).assign();
		}

	}

	public JVar visit(TrueLiteral n, Scope scope) {
		return new JVar(n, Java.Boolean()).assign(JBoolean.True());
	}

	public JVar visit(FalseLiteral n, Scope scope) {
		return new JVar(n, Java.Boolean()).assign(JBoolean.False());
	}

	public JVar visit(Identifier n, Scope scope) {
		JVar a = scope.getVar(n);
		if (a.Type() instanceof JUndefined) {
			ErrorHandler.send("Undefined variable " + a.Id(), n);
		}

		return a;
	}

	public JVar visit(ThisExpression n, Scope scope) {
		return new JVar(n, scope.Owner()).assign();
	}

	public JVar visit(ArrayAllocationExpression n, Scope scope) {
		JVar exp = n.f3.accept(this, scope);

		Java.Int().Assignable(exp.Type(), true, n);

		return new JVar(n, Java.ArrayInt()).assign(exp.Value());
	}

	public JVar visit(AllocationExpression n, Scope scope) {
		return new JVar(n, Java.get(n.f1)).assign();
	}

	public JVar visit(NotExpression n, Scope scope) {
		JVar exp = n.f1.accept(this, scope);

		Java.Boolean().Assignable(exp.Type(), true, n);

		if (exp.Value() == JBoolean.False()) {
			exp.assign(JBoolean.True());
		} else if (exp.Value() == JBoolean.True()) {
			exp.assign(JBoolean.False());
		}
		return exp;
	}

	public JVar visit(BracketExpression n, Scope scope) {
		return n.f1.accept(this, scope);
	}
}