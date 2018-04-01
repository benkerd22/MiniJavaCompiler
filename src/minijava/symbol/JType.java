package minijava.symbol;

import minijava.syntaxtree.*;

public abstract class JType {
	// JType is only a prototype, which defines the property and behaviour of raw data

	abstract public String Name();
	abstract public int Size();
	abstract public boolean Assignable(JType a, boolean report, Node n); // is this = a legal?
}