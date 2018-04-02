package tools;

import minijava.syntaxtree.*;
import minijava.symbol.*;

public class ErrorHandler {
	private static int err;

	public static void init() {
		err = 0;
	}

	public static int Err() {
		return err;
	}

	public static void send(String msg) {
		err++;
		System.out.println("\t" + msg);
	}

	public static void send(String msg, Node n) {
		LineNumberInfo i = LineNumberInfo.get(n);
		send("(Error) at (" + i.lineStart + "," + i.colStart + "): " + msg);
	}

	public static void typeMismatch(JType dst, JType src, Node n) {
		if (src instanceof JUndefined)
			return;
		send("Type mismatch: cannot convert type " + src.Name() + " to type " + dst.Name(), n);
	}

	public static void warn(String msg, Node n) {
		LineNumberInfo i = LineNumberInfo.get(n);
		System.out.println("\t(Warning) at (" + i.lineStart + "," + i.colStart + "): " + msg);
	}
}