package minijava.symbol;

import minijava.minijava2piglet.CodeGenerator;
import minijava.syntaxtree.*;
import minijava.typecheck.*;
import minijava.minijava2piglet.*;
import java.util.*;

public class JMethod {
	private static int count = 0; // for index allocation

	class Body {
		NodeOptional para;
		Identifier args; // special for main route
		NodeListOptional var, st;
		Expression ret;
	}

	private Body body;
	private Identifier name;
	private JType ret;
	private ArrayList<JType> paras;
	private int index; // index == 0 indicates main route
	private JClass owner;

	JMethod(Identifier _name, JClass _owner, JType _ret, ArrayList<JType> _para_list, NodeOptional b_para,
			NodeListOptional b_var, NodeListOptional b_st, Expression b_ret) {
		name = _name;
		owner = _owner;
		ret = _ret;
		paras = _para_list;
		body = new Body();
		body.para = b_para;
		body.var = b_var;
		body.st = b_st;
		body.ret = b_ret;

		index = count;
		count++;
	}

	public void setMainArgs(Identifier args) {
		body.args = args;
	}

	// ***** Build (TypeCheck) *****

	private Scope newScope() {
		Scope scope = new Scope(owner);
		if (index == 0) {
			scope.declare(MJava.ArrayString(), body.args);
			scope.queryVar(body.args).assign();
		}

		return scope;
	}

	public void buildScope() {
		ScopeBuilder b = new ScopeBuilder();
		Scope scope = newScope();

		if (index != 0) {
			body.para.accept(b, scope);
			body.var.accept(b, scope);
			body.st.accept(b, scope);
			JVar user_ret = body.ret.accept(b, scope);
			ret.Assignable(user_ret.Type(), true, body.ret);
		} else {
			body.var.accept(b, scope);
			body.st.accept(b, scope);
		}
	}

	// ***** Build (ToPiglet) *****

	public void buildCode_asMain() {
		CodeGenerator g = new CodeGenerator();
		Scope scope = newScope();

		body.var.accept(g, scope);
		Code.emit("MAIN\n", "");
		body.st.accept(g, scope);
		Code.emit("END\n\n", "");
	}

	public void buildCode() {
		if (index == 0) {
			Code.emit("f0_" + Name() + " [0]\nBEGIN\n\tERROR\nRETURN\n\t0\nEND\n\n", "");
			return;
		}

		CodeGenerator g = new CodeGenerator();
		Scope scope = newScope();

		body.para.accept(g, scope);
		body.var.accept(g, scope);

		if (paras.size() <= (20 - 1)) {
			// remember that paras includes "this"
			Code.emit("f" + index + "_" + owner.Name() + "_" + Name() + " [" + (paras.size() + 1) + "]\nBEGIN\n", "");
		} else {
			Code.emit("f" + index + "_" + owner.Name() + "_" + Name() + " [1]\nBEGIN\n", "");
			for (int i = 1; i <= paras.size(); i++) {
				Code.load(i, 0, i * 4);
			}
			Code.load(0, 0, 0);
		}

		body.st.accept(g, scope);
		JVar ret = body.ret.accept(g, scope);

		Code.emit("RETURN\n\tTEMP " + ret.Reg() + "\nEND\n\n", "");
	}

	// ***** Attribute *****

	public int Index() {
		return index;
	}

	public Identifier Node() {
		return name;
	}

	public JClass Owner() {
		return owner;
	}

	public String Name() {
		return name.f0.toString();
	}

	public JType Ret() {
		return ret;
	}

	public int SamePara(ArrayList<JType> plist) {
		if (plist.size() != paras.size())
			return paras.size();

		for (int i = 0; i < paras.size(); i++)
			if (paras.get(i) != plist.get(i))
				return i;

		return -1;
	}

	public boolean Same(JMethod m) {
		if (ret != m.ret)
			return false;

		return SamePara(m.paras) == -1;
	}
}