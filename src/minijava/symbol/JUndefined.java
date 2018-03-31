package minijava.symbol;

import minijava.syntaxtree.*;
import tools.*;

public class JUndefined extends JType {
	public String Name() {
		return "Undefined";
	}

	public boolean Assignable(JType a, boolean report, Node n) {
		if (report)
			ErrorHandler.send("Use a Undefined variable as left value", n);
		return false;
	}
}
