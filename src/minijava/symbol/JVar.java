package minijava.symbol;

import minijava.syntaxtree.*;

public class JVar {
	// JVar is the instance, having its address space and value
	private Identifier id;
	private Node where;
	private JType type;
	private int value = 0;	// for typecheck boolean literal only
	private int reg = -1;	// TEMP (?) for piglet
	private boolean assigned = false;	// for typecheck if unused
	private boolean vola = false;	// is the Var volatile? ex. this.a, b[0]
	private int base_reg = -1, bias = 0;	// address base (in reg) & bias

	public JVar(Identifier _id, JType _type) {
		id = _id;
		type = _type;
		where = _id;
	}

	public JVar(Node _where, JType _type) {
		id = null;
		type = _type;
		where = _where;
	}

	// ***** Build (TypeCheck) *****

	public JVar assign() {
		assigned = true;
		return this;
	}

	public JVar assign(int val) {
		value = val;
		assigned = true;
		return this;
	}

	// ***** Build (ToPiglet) *****

	public JVar bind(int regNum) {
		reg = regNum;
		return this;
	}

	public JVar setVola(boolean v) {
		vola = v;
		return this;
	}

	public JVar setBase(int regNum) {
		base_reg = regNum;
		return this;
	}

	public JVar setBias(int b) {
		bias = b;
		return this;
	}

	// ***** Attribute *****

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

	public long Val() {
		return value;
	}

	public boolean isAssigned() {
		return assigned;
	}

	public int Reg() {
		return reg;
	}

	public boolean isVola() {
		return vola;
	}

	public int baseReg() {
		return base_reg;
	}

	public int Bias() {
		return bias;
	}
}