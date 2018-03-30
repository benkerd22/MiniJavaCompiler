package minijava.typecheck;

import minijava.syntaxtree.*;
import java.util.*;

class JMethod {
	class Body {
		NodeOptional para;
		NodeListOptional var, st;
		Expression ret;
	}

	private Body body;
	private Identifier name;
	private Scope scope;
	private JType ret;
	private ArrayList<JType> para_list;

	JMethod(Identifier _name, JClass owner, JType _ret, ArrayList<JType> _para_list, NodeOptional b_para,
			NodeListOptional b_var, NodeListOptional b_st, Expression b_ret) {
		name = _name;
		scope = new Scope(null, owner);
		ret = _ret;
		para_list = _para_list;
		body = new Body();
		body.para = b_para;
		body.var = b_var;
		body.st = b_st;
		body.ret = b_ret;
	}

	public void buildScope() {
		ScopeBuilder b = new ScopeBuilder();
		if (body.para != null)
			body.para.accept(b, scope);
		if (body.var != null)
			body.var.accept(b, scope);
		if (body.st != null)
			body.st.accept(b, scope);
		if (body.ret != null) {
			JVar user_ret = body.ret.accept(b, scope);
			ret.Assignable(user_ret.Type(), true, body.ret);
		}
	}

	public Identifier Node() {
		return name;
	}

	public String Name() {
		return name.f0.toString();
	}

	public ArrayList<JType> List() {
		return para_list;
	}

	public JType Ret() {
		return ret;
	}

	public boolean Same(JMethod m) {
		if (ret != m.ret)
			return false;
		if (para_list.size() != m.para_list.size())
			return false;
		for (int i = 0; i < para_list.size(); i++) {
			if (para_list.get(i) != m.para_list.get(i))
				return false;
		}

		return true;
	}

	public void SetAsMainRoute(Identifier args) {
		scope.declare(Java.ArrayString(), args);
		scope.getVar(args).assign();
	}
}