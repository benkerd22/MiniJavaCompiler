package minijava.typecheck;

import java.util.*;
import minijava.syntaxtree.*;
import minijava.visitor.*;

abstract class JType {
	// JType is only a prototype, which defines the property and behaviour of raw data
	// TODO: default value ?
	// TODO: organize value ?
	abstract public String Name();

	abstract public boolean Assignable(JType a, boolean report, Node n); // is this = a legal?
}

abstract class JBuiltIn extends JType {
}

class JUndefined extends JType {
	public String Name() {
		return "Undefined";
	}

	public boolean Assignable(JType a, boolean report, Node n) {
		if (report)
			ErrorHandler.send("Use a Undefined variable as left value", n);
		return false;
	}
}

class JInt extends JBuiltIn {
	public String Name() {
		return "Int";
	}

	public boolean Assignable(JType a, boolean report, Node n) {
		if (a instanceof JInt)
			return true;
		else if (report)
			ErrorHandler.typeMismatch(this, a, n);

		return false;
	}
}

class JBoolean extends JBuiltIn {
	public String Name() {
		return "Boolean";
	}

	public static long True() {
		return -2;
	}

	public static long False() {
		return -1;
	}

	public boolean Assignable(JType a, boolean report, Node n) {
		if (a instanceof JBoolean)
			return true;
		else if (report)
			ErrorHandler.typeMismatch(this, a, n);

		return false;
	}
}

class JArray extends JBuiltIn {
	private JType type;

	JArray(JType _type) {
		type = _type;
	}

	public String Name() {
		return type.Name() + "[]";
	}

	public JType ElementType() {
		return type;
	}

	public boolean Assignable(JType a) {
		return a instanceof JArray && this.type == ((JArray) a).type;
	}

	public boolean Assignable(JType a, boolean report, Node n) {
		if (!(a instanceof JArray)) {
			if (report)
				ErrorHandler.typeMismatch(this, a, n);
			return false;
		} else if (this.type != ((JArray) a).type) {
			if (report)
				ErrorHandler.typeMismatch(this.type, ((JArray) a).type, n);
			return false;
		} else
			return true;
	}
}

class JClass extends JType {
	private Identifier name;
	private NodeListOptional v_node, m_node; // vars, methods
	private Node f_node; //father

	private JClass father = null;
	private HashMap<String, JMethod> methods = new HashMap<String, JMethod>();
	private HashMap<String, JType> vars = new HashMap<String, JType>();

	JClass(MainClass n) {
		name = n.f1;
		v_node = null;
		m_node = null;
		f_node = null;

		ArrayList<JType> list = new ArrayList<JType>();
		list.add(Java.ArrayString());

		JMethod m = new JMethod(new Identifier(n.f6), this, Java.Undefined(), list, null, n.f14, n.f15, null);
		m.SetAsMainRoute(n.f11);

		add_method(m);
	}

	JClass(Identifier _name, Node f, NodeListOptional v, NodeListOptional m) {
		name = _name;
		v_node = v;
		m_node = m;
		f_node = f;
	}

	private void add_method(JMethod m) {
		if (methods.containsKey(m.Name())) { // Not allow Method Overidding
			ErrorHandler.send("Dupilcate method " + m.Name() + " in Class " + Name(), m.Node());
		} else {
			methods.put(m.Name(), m);
		}
	}

	private void add_var(Identifier id, JType type) {
		String sid = id.f0.toString();

		if (vars.containsKey(sid)) { // allow covering the member in Father Class; thus DON'T query in father.vars
			ErrorHandler.send("Dupilcate variable " + sid + " in Class " + Name(), id);
		} else {
			vars.put(sid, type);
		}
	}

	public JType queryType(String id) {
		JClass p = this;
		while (p != null) {
			if (p.vars.containsKey(id)) {
				return p.vars.get(id);
			}

			p = p.father;
		}

		return Java.Undefined();
	}

	public JMethod queryMethod(String id) {
		JClass p = this;
		while (p != null) {
			if (p.methods.containsKey(id)) {
				return p.methods.get(id);
			}

			p = p.father;
		}

		return null;
	}

	private void release_father() {
		if (f_node != null) {
			JType _father = Java.get(f_node);
			if (_father == Java.Undefined()) {
				ErrorHandler.send("Class " + Name() + " extends an undefined Class");
			} else
				father = (JClass) _father;
		}
	}

	private void release_methods() {
		class ParaListHelper extends GJVoidDepthFirst<ArrayList<JType>> {
			public void visit(FormalParameter n, ArrayList<JType> list) {
				JType type = Java.get(n.f0.f0.choice);
				//String name = n.f1.f0.toString();

				list.add(type);
			}
		}

		class MethodListHelper extends GJVoidDepthFirst<JClass> {
			public void visit(MethodDeclaration n, JClass c) {
				ArrayList<JType> para_list = new ArrayList<JType>();

				n.f4.accept(new ParaListHelper(), para_list);

				JMethod m = new JMethod(n.f2, c, Java.get(n.f1.f0.choice), para_list, n.f4, n.f7, n.f8, n.f10);

				c.add_method(m);
			}
		}

		if (m_node == null)
			return;

		m_node.accept(new MethodListHelper(), this);
	}

	private void release_vars() {
		class VarListHelper extends GJVoidDepthFirst<JClass> {
			public void visit(VarDeclaration n, JClass c) {
				c.add_var(n.f1, Java.get(n.f0.f0.choice));
			}
		}

		if (v_node == null)
			return;

		v_node.accept(new VarListHelper(), this);
	}

	private void strict_var_method() {
		// method should have a different ID from variable in the same class
		// However, this is allowed in Java
		// thus we throw an warning instead of error

		for (Map.Entry<String, JMethod> e : methods.entrySet()) {
			JMethod m = e.getValue();
			if (vars.containsKey(m.Name())) {
				ErrorHandler.warn("Method " + m.Name() + " has the same name as the member in Class " + Name(),
						m.Node());
			}
		}
	}

	public void release() {
		release_father();
		release_methods();
		release_vars();

		strict_var_method();
	}

	public void checkMethods() {
		for (Map.Entry<String, JMethod> e : methods.entrySet()) {
			JMethod m = e.getValue();

			JClass p = this.father;
			while (p != null) {
				if (p.methods.containsKey(m.Name())) {
					JMethod t = p.methods.get(m.Name());
					if (!t.Same(m)) {
						ErrorHandler.send("Duplicate method " + m.Name() + " in Class " + Name()
								+ ": not allow overriding father's method", m.Node());
						break;
					}
				}

				p = p.father;
			}
		}
	}

	public void buildScope() {
		for (Map.Entry<String, JMethod> e : methods.entrySet()) {
			JMethod m = e.getValue();
			m.buildScope();
		}
	}

	public Set<Map.Entry<String, JType>> var_entry() {
		return vars.entrySet();
	}

	public void info() {
		System.out.println("Class " + Name());
		if (methods.size() != 0) {
			System.out.println("  methods:");
			for (Map.Entry<String, JMethod> e : methods.entrySet()) {
				System.out.println("\t" + e.getValue().Name());
			}
		}
		if (vars.size() != 0) {
			System.out.println("  vars:");
			for (Map.Entry<String, JType> e : vars.entrySet()) {
				System.out.println("\t" + e.getValue().Name() + " " + e.getKey());
			}
		}
	}

	public Node Node() {
		return name;
	}

	public String Name() {
		return name.f0.toString();
	}

	public JClass Father() {
		return father;
	}

	public boolean Assignable(JType c, boolean report, Node n) {
		if (!(c instanceof JClass)) {
			if (report)
				ErrorHandler.typeMismatch(this, c, n);
			return false;
		}
		JClass p = (JClass) c;
		while (p != null) {
			if (p == this) {
				return true;
			}

			p = p.father;
		}

		if (report)
			ErrorHandler.typeMismatch(this, c, n);
		return false;
	}
}
