package minijava.typecheck;

import minijava.syntaxtree.*;

class JInt extends JType {
	public String Name() {
		return "Int";
	}

	public boolean Assignable(JType a, boolean report, Node n) {
		if (a instanceof JInt)
			return true;
		else if (report)
			ErrorHandler.typeMismatch(this, a, n);

		return false;
	}
}