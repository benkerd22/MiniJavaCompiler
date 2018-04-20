package minijava.symbol;

import minijava.minijava2piglet.*;
import minijava.syntaxtree.*;

public class JVar {
	// JVar is the instance, having its address space and value
	private Identifier id;
	private Node where;
	private JType type;
	private int value = 0; // for typecheck boolean literal only
	private int reg = -1; // TEMP (?) for piglet
	private boolean assigned = false; // for typecheck if unused
	private String literal = "";// is this Var JUST a literal? not "" <==> is a literal
	private boolean literalLoad; // has this literal already been loaded?
	private boolean vola = false; // is this Var volatile? ex. this.a, b[0]
	private int base_reg = -1, bias = 0; // address base (in reg) & bias

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

	public JVar setLiter(String val) {
		literal = val;
		literalLoad = false;
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
		if (literal != "" && !literalLoad) {
			Code.mov(reg, literal);
			literalLoad = true;
		}
		return reg;
	}

	public String Exp() {
		if (literal != "")
			return literal;

		return Code.T(reg);
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