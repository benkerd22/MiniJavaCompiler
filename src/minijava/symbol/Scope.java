package minijava.symbol;

import java.util.*;
import minijava.syntaxtree.*;
import tools.*;

public class Scope {
	private Scope father;
	private JClass owner;
	private Map<String, JVar> vars = new HashMap<String, JVar>();

	public Scope(JClass _owner) { // for scope in func
		father = null;
		owner = _owner;
	}

	public Scope(Scope _father) { // for scope in a block
		father = _father;
		owner = father.owner;
	}

	// ***** Build *****

	public void declare(JType t, Identifier id) {
		String sid = id.f0.toString();

		JVar v = queryVarOnlyFunc(id);
		if (v != null) {
			ErrorHandler.send("Dupilcate variable " + sid, id);
			return;
		}

		vars.put(sid, new JVar(id, t));
	}

	// ***** Query *****

	private JVar queryVarOnlyFunc(Identifier id) {
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

	public JVar queryVar(Identifier id) {
		// look up id in Func body
		JVar t = queryVarOnlyFunc(id);
		if (t != null)
			return t;

		// look up id in Class & Father Class
		return owner.queryVar(id);
	}

	// ***** Attribute *****

	public JClass Owner() {
		return owner;
	}
}
