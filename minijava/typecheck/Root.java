package minijava.typecheck;

import java.util.*;
import minijava.syntaxtree.*;

class Root {
}

abstract class Java {
	private static Goal root;
	private static String filename;
	private static JType Int = new JInt(), Boolean = new JBoolean(), ArrayInt = new JArray(Int),
			Undefined = new JUndefined();
	private static JType String = new JClass(new Identifier(new NodeToken("String")), null, null, null),
			ArrayString = new JArray(String);
	private static HashMap<String, JType> class_list = new HashMap<String, JType>();
	//private static HashMap<String, JBuiltIn> built_in_list = new HashMap<String, JBuiltIn>();

	public static void init(Goal _root, String _filename) {
		root = _root;
		filename = _filename;
		class_list.clear();
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

	public static void addClass(String name, JClass type) {
		if (class_list.containsKey(name)) {
			ErrorHandler.send("Class " + name + " is already defined", type.Node());
		} else {
			class_list.put(name, type);
		}
	}

	private static JType getClass(String name, Node n) {
		if (class_list.containsKey(name)) {
			return class_list.get(name);
		} else {
			ErrorHandler.send("Try to access an undefined type " + name, n);
			return Undefined;
		}
	}

	public static JType get(Node type) {
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
		for (Map.Entry<String, JType> e : class_list.entrySet()) {
			JClass c = (JClass) e.getValue();
			c.release();
		}

		for (Map.Entry<String, JType> e : class_list.entrySet()) {
			JClass c = (JClass) e.getValue();
			JClass p = c.Father();

			while (p != null) {
				if (p.Name() == c.Name()) {
					ErrorHandler.send("Extensions loop found : Class " + c.Name() + " -> Class " + c.Father().Name());
					return;
				}
				p = p.Father();
			}
		}

		for (Map.Entry<String, JType> e : class_list.entrySet()) {
			JClass c = (JClass) e.getValue();
			c.checkMethods();
		}
	}

	public static void buildScope() {
		for (Map.Entry<String, JType> e : class_list.entrySet()) {
			JClass c = (JClass) e.getValue();
			c.buildScope();
		}
	}

	public static void show() {
		for (Map.Entry<String, JType> e : class_list.entrySet()) {
			JClass c = (JClass) e.getValue();
			c.info();
		}
	}
}