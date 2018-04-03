package minijava.symbol;

import minijava.syntaxtree.*;
import tools.*;

public class JBoolean extends JType {

	// ***** Build (TypeCheck) *****

	public static int True() {
		return 2;
	}

	public static int False() {
		return 1;
	}

	public static int Unknown() {
		return 0;
	}

	// ***** Attribute *****

	public String Name() {
		return "Boolean";
	}

	public int Size() {
		return 4;
	}

	public boolean Assignable(JType a, boolean report, Node n) {
		if (a instanceof JBoolean)
			return true;
		else if (report)
			ErrorHandler.typeMismatch(this, a, n);

		return false;
	}
}