package minijava.symbol;

import minijava.minijava2piglet.CodeGenerator;
import minijava.syntaxtree.*;
import minijava.typecheck.*;
import minijava.minijava2piglet.*;
import java.util.*;

public class JMethod {
	class Body {
		NodeOptional para;
		NodeListOptional var, st;
		Expression ret;
	}

	private static int count = 0;

	private Body body;
	private Identifier name;
	private Scope scope;
	private JType ret;
	private ArrayList<JType> paras;
	private int index;	// index == 0 indicates main entry

	JMethod(Identifier _name, JClass owner, JType _ret, ArrayList<JType> _para_list, NodeOptional b_para,
			NodeListOptional b_var, NodeListOptional b_st, Expression b_ret) {
		name = _name;
		scope = new Scope(owner);
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

	public void setStringArgs(Identifier args) {
		scope.declare(MJava.ArrayString(), args);
		scope.getVar(args).assign();
	}

	private void buildScope_asMain() {
		ScopeBuilder b = new ScopeBuilder();
		body.var.accept(b, scope);
		body.st.accept(b, scope);
	}

	public void buildScope() {
		if (index == 0) {
			buildScope_asMain();
			return;
		}

		ScopeBuilder b = new ScopeBuilder();
		body.para.accept(b, scope);
		body.var.accept(b, scope);
		body.st.accept(b, scope);
		JVar user_ret = body.ret.accept(b, scope);
		ret.Assignable(user_ret.Type(), true, body.ret);
	}

	public void buildCode_asMain() {
		CodeGenerator g = new CodeGenerator();

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

		body.para.accept(g, scope);
		body.var.accept(g, scope);
		// remember that paras includes "this"
		Code.emit("f" + index + "_" + Name() + " [" + (paras.size() + 1) + "]\nBEGIN\n", "");

		body.st.accept(g, scope);
		JVar ret = body.ret.accept(g, scope);

		Code.emit("RETURN\n\tTEMP " + ret.Reg() + "\nEND\n\n", "");
	}

	public int Index() {
		return index;
	}

	public Identifier Node() {
		return name;
	}

	public String Name() {
		return name.f0.toString();
	}

	public ArrayList<JType> List() {
		return paras;
	}

	public JType Ret() {
		return ret;
	}

	public boolean Same(JMethod m) {
		if (ret != m.ret)
			return false;
		if (paras.size() != m.paras.size())
			return false;
		for (int i = 0; i < paras.size(); i++) {
			if (paras.get(i) != m.paras.get(i))
				return false;
		}

		return true;
	}
}