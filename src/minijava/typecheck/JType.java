package minijava.typecheck;

import minijava.syntaxtree.*;

abstract class JType {
	// JType is only a prototype, which defines the property and behaviour of raw data
	// TODO: default value ?
	// TODO: organize value ?
	abstract public String Name();

	abstract public boolean Assignable(JType a, boolean report, Node n); // is this = a legal?
}