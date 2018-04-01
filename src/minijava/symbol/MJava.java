package minijava.symbol;

import java.util.*;
import minijava.syntaxtree.*;
import minijava.typecheck.*;
import minijava.minijava2piglet.*;
import tools.*;

public abstract class MJava {
	private static Goal root;
	private static String filename;
	private static JClass main_class;
	private static JType Int = new JInt(), Boolean = new JBoolean(), ArrayInt = new JArray(Int),
			Undefined = new JUndefined();
	private static JClass String = new JClass(new Identifier(new NodeToken("String")), null, null, null);
	private static JType ArrayString = new JArray(String);
	private static HashMap<String, JClass> classes = new HashMap<String, JClass>();	// user defined classes
	private static HashMap<String, JClass> bin_classes = new HashMap<String, JClass>();	// bulit-in classes

	public static void init(Goal _root, String _filename) {
		root = _root;
		filename = _filename;
		classes.clear();
		bin_classes.put("String", String);
		// It seems that "String" will NOT be recognized as a <IDENTIFIER>...
		// thus the warning below is unnecessary.
		// However, it is allowed in Java.
		// So why not implement it? (^_^)
	}

	public static JType Int() {
		return Int;
	}

	public static JType Boolean() {
		return Boolean;
	}

	public static JType String() {
		return String;
	}

	public static JType ArrayInt() {
		return ArrayInt;
	}

	public static JType ArrayString() {
		return ArrayString;
	}

	public static JType Undefined() {
		return Undefined;
	}

	public static void declareMainClass(JClass type) {
		main_class = type;
		declareClass(type.Name(), type);
	}

	public static void declareClass(String name, JClass type) {	// We already have a JClass object, so we don't need a Identifier as an argument
		if (classes.containsKey(name)) {
			ErrorHandler.send("Class " + name + " is already defined", type.Node());
		} else {
			if (bin_classes.containsKey(name))
				ErrorHandler.warn("Class " + name + " may conflict with a pre-defined class", type.Node());	// the warning is unnecessary.

			classes.put(name, type);
		}
	}

	private static JType getClass(String name, Node n) {
		if (classes.containsKey(name)) {
			return classes.get(name);
		} else if (bin_classes.containsKey(name)) {
			return bin_classes.get(name);
		} else {
			ErrorHandler.send("Try to access an undefined type " + name, n);
			return Undefined;
		}
	}

	public static JType getType(Node type) {
		if (type instanceof ArrayType) {
			return ArrayInt;
		} else if (type instanceof BooleanType) {
			return Boolean;
		} else if (type instanceof IntegerType) {
			return Int;
		} else if (type instanceof Identifier) {
			return getClass(((Identifier) type).f0.toString(), type);
		}

		return Undefined;
	}

	public static void buildClass() {
		root.accept(new ClassTreeBuilder(filename));
		release();
	}

	private static void release() {
		for (Map.Entry<String, JClass> e : classes.entrySet()) {
			JClass c = e.getValue();
			c.release();
		}

		for (Map.Entry<String, JClass> e : classes.entrySet()) {
			JClass c = e.getValue();
			JClass p = c.Father();

			while (p != null) {
				if (p.Name() == c.Name()) {
					ErrorHandler.send("Extensions loop found : Class " + c.Name() + " -> Class " + c.Father().Name());
					return;
				}
				p = p.Father();
			}
		}

		for (Map.Entry<String, JClass> e : classes.entrySet()) {
			JClass c = e.getValue();
			c.checkMethods();
		}
	}

	public static void buildScope() {
		for (Map.Entry<String, JClass> e : classes.entrySet()) {
			JClass c = e.getValue();
			c.buildScope();
		}
	}

	public static void show() {
		for (Map.Entry<String, JClass> e : classes.entrySet()) {
			JClass c = e.getValue();
			c.info();
		}
	}

	public static void buildCode() {
		JMethod m = main_class.queryMethod(new Identifier(new NodeToken("main")));
		m.buildCode_asMain();

		Code.clearFunc();

		int index = 0;
		for (Map.Entry<String, JClass> e : classes.entrySet()) {
			JClass c = e.getValue();
			index = c.buildCode(index);
		}
	}
}