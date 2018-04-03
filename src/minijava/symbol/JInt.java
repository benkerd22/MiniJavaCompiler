package minijava.symbol;

import minijava.syntaxtree.*;
import tools.*;

public class JInt extends JType {

	// ***** Attribute *****

	public String Name() {
		return "Int";
	}

	public int Size() {
		return 4;
	}

	public boolean Assignable(JType a, boolean report, Node n) {
		if (a instanceof JInt)
			return true;
		else if (report)
			ErrorHandler.typeMismatch(this, a, n);

		return false;
	}
}