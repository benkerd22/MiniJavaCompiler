package minijava.symbol;

import minijava.syntaxtree.*;
import tools.*;

public class JBoolean extends JType {
	public String Name() {
		return "Boolean";
	}

	public static long True() {
		return -2;
	}

	public static long False() {
		return -1;
	}

	public boolean Assignable(JType a, boolean report, Node n) {
		if (a instanceof JBoolean)
			return true;
		else if (report)
			ErrorHandler.typeMismatch(this, a, n);

		return false;
	}
}