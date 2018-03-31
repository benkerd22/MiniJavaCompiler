package minijava.typecheck;

import minijava.syntaxtree.*;

class JArray extends JType {
	private JType type;

	JArray(JType _type) {
		type = _type;
	}

	public String Name() {
		return type.Name() + "[]";
	}

	public JType ElementType() {
		return type;
	}

	public boolean Assignable(JType a) {
		return a instanceof JArray && this.type == ((JArray) a).type;
	}

	public boolean Assignable(JType a, boolean report, Node n) {
		if (!(a instanceof JArray)) {
			if (report)
				ErrorHandler.typeMismatch(this, a, n);
			return false;
		} else if (this.type != ((JArray) a).type) {
			if (report)
				ErrorHandler.typeMismatch(this.type, ((JArray) a).type, n);
			return false;
		} else
			return true;
	}
}