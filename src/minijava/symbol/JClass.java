package minijava.symbol;

import java.util.*;
import minijava.syntaxtree.*;
import minijava.visitor.*;
import minijava.minijava2piglet.*;
import tools.*;

public class JClass extends JType {
	private Identifier name;
	private NodeListOptional v_node, m_node; // vars, methods
	private Node f_node; //father

	private JClass father = null;
	private HashMap<String, JMethod> methods = new HashMap<String, JMethod>();
	private HashMap<String, Integer> mbiases = new HashMap<String, Integer>();	// method biases in vTable
	private HashMap<String, JType> vars = new HashMap<String, JType>();
	private HashMap<String, Integer> vbiases = new HashMap<String, Integer>();	// var biases in an instance
	private int size = 0, heritage = 0;	// size count of vars && the vars that extends from fathers

	public JClass(MainClass n) {
		name = n.f1;
		v_node = null;
		m_node = null;
		f_node = null;

		ArrayList<JType> list = new ArrayList<JType>();
		list.add(MJava.ArrayString());

		JMethod m = new JMethod(new Identifier(n.f6), this, MJava.Undefined(), list, null, n.f14, n.f15, null);
		m.setMainArgs(n.f11);

		add_method(m);
	}

	public JClass(Identifier _name, Node f, NodeListOptional v, NodeListOptional m) {
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
			size += type.Size();
		}
	}

	public JVar queryVar(Identifier id) {
		String sid = id.f0.toString();

		JClass p = this;
		while (p != null) {
			if (p.vars.containsKey(sid)) {
				return new JVar(id, p.vars.get(sid))
				.assign()
				.setVola(true)
				.setBias(p.vbiases.get(sid) + 4);	// add space of VPTR !!!
			}

			p = p.father;
		}

		return new JVar(id, MJava.Undefined()).assign();
	}

	public JMethod queryMethod(Identifier id) {
		String sid = id.f0.toString();

		JClass p = this;
		while (p != null) {
			if (p.methods.containsKey(sid)) {
				return p.methods.get(sid);
			}

			p = p.father;
		}

		return null;
	}

	private void release_father() {
		if (f_node != null) {
			JType _father = MJava.getType(f_node);
			if (_father == MJava.Undefined()) {
				ErrorHandler.send("Class " + Name() + " extends an undefined Class");
			} else
				father = (JClass) _father;
		}
	}

	private void release_methods() {
		class ParaListHelper extends GJVoidDepthFirst<ArrayList<JType>> {
			public void visit(FormalParameter n, ArrayList<JType> list) {
				JType type = MJava.getType(n.f0.f0.choice);
				//String name = n.f1.f0.toString();

				list.add(type);
			}
		}

		class MethodListHelper extends GJVoidDepthFirst<JClass> {
			public void visit(MethodDeclaration n, JClass c) {
				ArrayList<JType> para_list = new ArrayList<JType>();

				n.f4.accept(new ParaListHelper(), para_list);

				JMethod m = new JMethod(n.f2, c, MJava.getType(n.f1.f0.choice), para_list, n.f4, n.f7, n.f8, n.f10);

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
				c.add_var(n.f1, MJava.getType(n.f0.f0.choice));
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
			String mid = m.Name();

			JClass p = this.father;
			while (p != null) {
				if (p.methods.containsKey(mid)) {
					JMethod t = p.methods.get(mid);
					if (!t.Same(m)) {
						ErrorHandler.send("Duplicate method " + mid + " in Class " + Name()
								+ ": not allow overriding father's method", m.Node());
						break;
					}
				}

				p = p.father;
			}
		}
	}

	public void buildvBiases() {
		heritage = 0;
		JClass p = this.father;
		while (p != null) {
			heritage += p.size;
			p = p.father;
		}

		int bias = 0;
		for (Map.Entry<String, JType> e : vars.entrySet()) {
			vbiases.put(e.getKey(), heritage + bias);
			bias += e.getValue().Size();
		}
	}

	public void buildScope() {
		for (Map.Entry<String, JMethod> e : methods.entrySet()) {
			JMethod m = e.getValue();
			m.buildScope();
		}
	}

	public void buildmBiases() {
		int i = 0;
		for (Map.Entry<String, JMethod> e : methods.entrySet()) {
			mbiases.put(e.getKey(), i);
			i += 4;
		}
	}

	public int querymBiases(Identifier n) {
		return mbiases.get(n.f0.toString());
	}

	public void buildClassCode() {
		Code.emit("new_" + Name() + " [0]\nBEGIN\n", "");

		int max = Collections.max(mbiases.values()) + 4;
		Code.malloc(0, Integer.toString(max));

		int i = 0;
		for (Map.Entry<String, JMethod> e : methods.entrySet()) {
			JMethod m = e.getValue();
			Code.mov(1, "f" + m.Index() + "_" + Name() + "_" + m.Name());
			Code.store(0, i, 1);
			i += 4;
		}

		Code.emit("RETURN\n\tTEMP 0\nEND\n\n", "");
	}

	public void buildMethodCode() {
		for (Map.Entry<String, JMethod> e : methods.entrySet()) {
			JMethod m = e.getValue();
			m.buildCode();
		}
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
		if (mbiases.size() != 0) {
			System.out.println("  mbiases:");
			for (Map.Entry<String, Integer> e : mbiases.entrySet()) {
				System.out.println("\t" + e.getKey() + " " + e.getValue());
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

	public int Size() {
		return 4; // as a pointer
	}

	public int instanceSize() {
		return size + heritage;
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
