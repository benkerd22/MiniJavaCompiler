package minijava.symbol;

import java.util.*;
import minijava.syntaxtree.*;
import minijava.visitor.*;
import minijava.minijava2piglet.*;
import tools.*;

public class JClass extends JType {
	// ***** attributes *****
	private Identifier name;
	private NodeListOptional v_node, m_node; // vars, methods
	private Node f_node; //father

	private JClass father = null;
	private int size = 0, heritage = 0; // size count of vars && the vars that extends from fathers

	// ***** methods *****
	private Map<String, JMethod> methods = new HashMap<String, JMethod>();
	private Map<String, Integer> mbiases = new HashMap<String, Integer>(); // method biases in vTable
	private Map<String, Integer> mstatus = new HashMap<String, Integer>(); // 0: free, 1: covered by child, 2: covers father's, 3: both 1 & 2
	private Set<Integer> reserve = new HashSet<Integer>();
	private boolean isFinishmBiases = false; // topo sort

	// ***** vars *****
	private Map<String, JType> vars = new HashMap<String, JType>();
	private Map<String, Integer> vbiases = new HashMap<String, Integer>(); // var biases in an instance

	// **********

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

	// ***** Build (Typecheck) *****

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

	private void release_father() {
		if (f_node != null) {
			JType _father = MJava.queryType(f_node);
			if (_father == MJava.Undefined()) {
				ErrorHandler.send("Class " + Name() + " extends an undefined Class");
			} else
				father = (JClass) _father;
		}
	}

	private void release_methods() {
		class ParaListHelper extends GJVoidDepthFirst<ArrayList<JType>> {
			public void visit(FormalParameter n, ArrayList<JType> list) {
				JType type = MJava.queryType(n.f0.f0.choice);

				list.add(type);
			}
		}

		class MethodListHelper extends GJVoidDepthFirst<JClass> {
			public void visit(MethodDeclaration n, JClass c) {
				ArrayList<JType> para_list = new ArrayList<JType>();

				n.f4.accept(new ParaListHelper(), para_list);

				JMethod m = new JMethod(n.f2, c, MJava.queryType(n.f1.f0.choice), para_list, n.f4, n.f7, n.f8, n.f10);

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
				c.add_var(n.f1, MJava.queryType(n.f0.f0.choice));
			}
		}

		if (v_node == null)
			return;

		v_node.accept(new VarListHelper(), this);
	}

	public void release() {
		release_father();
		release_methods();
		release_vars();
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

					// this.method ==> p.method
					set_mstatus(mid, 2);
					p.set_mstatus(mid, 1);
				}

				p = p.father;
			}

			set_mstatus(mid, 0);
		}
	}

	public void buildScope() {
		for (Map.Entry<String, JMethod> e : methods.entrySet()) {
			JMethod m = e.getValue();
			m.buildScope();
		}
	}

	// ***** Build (ToPiglet) *****

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

	private void set_mstatus(String mid, int i) {
		if (!mstatus.containsKey(mid))
			mstatus.put(mid, 0);

		mstatus.put(mid, mstatus.get(mid) | i);
	}

	public void buildmBiases() {
		if (isFinishmBiases)
			return;

		if (father != null) {
			if (!father.isFinishmBiases)
				father.buildmBiases();
		}

		if (father != null) {
			reserve.addAll(father.reserve);
		}

		HashSet<Integer> used = new HashSet<Integer>();
		for (Map.Entry<String, Integer> e : mstatus.entrySet()) {
			String mid = e.getKey();

			if (e.getValue() == 2 || e.getValue() == 3) {
				JClass p = this.father;
				while (p != null) {
					if (p.methods.containsKey(mid)) {
						int inherit = p.mbiases.get(mid);

						mbiases.put(mid, inherit);
						used.add(inherit);
					}

					p = p.father;
				}
			}
		}

		int assign = 0;

		for (Map.Entry<String, Integer> e : mstatus.entrySet()) {
			String mid = e.getKey();

			if (e.getValue() == 1) {
				while (used.contains(assign) || reserve.contains(assign))
					assign++;

				mbiases.put(mid, assign * 4);
				assign++;
			}
		}

		for (Map.Entry<String, Integer> e : mstatus.entrySet()) {
			String mid = e.getKey();

			if (e.getValue() == 1 || e.getValue() == 3) {
				reserve.add(mbiases.get(mid));
			}
		}

		isFinishmBiases = true;
	}

	public void buildClassCode() {
		Code.emit("new_" + Name() + " [0]\nBEGIN\n", "");

		if (mbiases.size() > 0) {
			int max = Collections.max(mbiases.values()) + 4;
			Code.malloc(0, Integer.toString(max));
		} else {
			Code.mov(0, "0");
		}

		for (Map.Entry<String, Integer> e : mbiases.entrySet()) {
			JMethod m = methods.get(e.getKey());
			Code.mov(1, "f" + m.Index() + "_" + Name() + "_" + m.Name());
			Code.store(0, e.getValue(), 1);
		}

		Code.emit("RETURN\n\tTEMP 0\nEND\n\n", "");
	}

	public void buildMethodCode() {
		for (Map.Entry<String, JMethod> e : methods.entrySet()) {
			JMethod m = e.getValue();
			m.buildCode();
		}
	}

	// ***** Query *****

	public JVar queryVar(Identifier id) {
		String sid = id.f0.toString();

		JClass p = this;
		while (p != null) {
			if (p.vars.containsKey(sid)) {
				return new JVar(id, p.vars.get(sid)).assign().setVola(true).setBias(p.vbiases.get(sid) + 4); // add space of VPTR !!!
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

	public int queryMethodStatus(Identifier id) {
		return mstatus.get(id.f0.toString());
	}

	public int querymBiases(Identifier n) {
		return mbiases.get(n.f0.toString());
	}

	// ***** Attribute *****

	public void info() {
		System.out.println("Class " + Name());
		if (methods.size() != 0) {
			System.out.println("  methods:");
			for (Map.Entry<String, JMethod> e : methods.entrySet()) {
				System.out.println("\t" + mbiases.get(e.getValue().Name()) + "\t" + e.getValue().Name());
			}
		}
		if (vars.size() != 0) {
			System.out.println("  vars:");
			for (Map.Entry<String, JType> e : vars.entrySet()) {
				System.out.println("\t" + mbiases.get(e.getKey()) + "\t" + e.getValue().Name() + " " + e.getKey());
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
