package minijava.symbol;

import minijava.syntaxtree.*;
import tools.*;

public class JUndefined extends JType {

	// ***** Attribute *****

	public String Name() {
		return "Undefined";
	}

	public int Size() {
		return 4;
	}

	public boolean Assignable(JType a, boolean report, Node n) {
		if (report)
			ErrorHandler.send("Use a Undefined variable as left value", n);
		return false;
	}
}
