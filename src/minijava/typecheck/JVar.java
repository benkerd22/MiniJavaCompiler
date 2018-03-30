package minijava.typecheck;

import minijava.syntaxtree.*;

public class JVar {
	// JVar is the instance, having its address space and value
	private Identifier id;
	private Node where;
	private JType type;
	private long value = 0;
	private boolean assigned;

	JVar(Identifier _id, JType _type) {
		id = _id;
		type = _type;
		assigned = false;
		where = _id;
	}

	JVar(Node _where, JType _type) {
		id = null;
		type = _type;
		assigned = false;
		where = _where;
	}

	public Identifier Node() {
		return id;
	}

	public Node Where() {
		return where;
	}

	public String Id() {
		return id != null ? id.f0.toString() : ".tmp";
	}

	public JType Type() {
		return type;
	}

	public long Value() {
		return value;
	}

	public JVar assign() {
		assigned = true;
		return this;
	}

	public JVar assign(long _value) {
		value = _value;
		assigned = true;
		return this;
	}

	public boolean isAssigned() {
		return assigned;
	}
}