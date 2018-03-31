package minijava.symbol;

import minijava.syntaxtree.*;

public class JType {
	// JType is only a prototype, which defines the property and behaviour of raw data
	// TODO: default value ?
	// TODO: organize value ?
	public String Name() {return "";}

	public boolean Assignable(JType a, boolean report, Node n) {return true;} // is this = a legal?
}