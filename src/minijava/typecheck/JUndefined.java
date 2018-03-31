package minijava.typecheck;

import minijava.syntaxtree.*;

class JUndefined extends JType {
	public String Name() {
		return "Undefined";
	}

	public boolean Assignable(JType a, boolean report, Node n) {
		if (report)
			ErrorHandler.send("Use a Undefined variable as left value", n);
		return false;
	}
}
