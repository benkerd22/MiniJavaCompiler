package minijava.symbol;

import java.util.*;
import minijava.syntaxtree.*;
import tools.*;

public class Scope {
	private HashMap<String, JVar> vars = new HashMap<String, JVar>();
	private Scope father;
	private JClass owner;

	public Scope(JClass _owner) {	// for scope in func
		father = null;
		owner = _owner;
	}

	public Scope(Scope _father) {	// for scope in a block
		father = _father;
		owner = father.owner;
	}

	public JClass Owner() {
		return owner;
	}

	private JVar getVarFunc(Identifier id) {
		String sid = id.f0.toString();

		// look up id in Func body
		Scope p = this;
		while (p != null) {
			if (p.vars.containsKey(sid)) {
				return p.vars.get(sid);
			}

			p = p.father;
		}

		return null;
	}

	public JVar getVar(Identifier id) {
		// look up id in Func body
		JVar t = getVarFunc(id);
		if (t != null)
			return t;

		// look up id in Class & Father Class
		return owner.queryVar(id);
	}

	public void declare(JType t, Identifier id) {
		String sid = id.f0.toString();

		JVar v = getVarFunc(id);
		if (v != null) {
			ErrorHandler.send("Dupilcate variable " + sid, id);
			return;
		}

		vars.put(sid, new JVar(id, t));
	}
}
