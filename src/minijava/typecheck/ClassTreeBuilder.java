package minijava.typecheck;

import minijava.syntaxtree.*;
import minijava.visitor.*;
import minijava.symbol.*;
import tools.*;

public class ClassTreeBuilder extends DepthFirstVisitor {
	private String filename = "";

	public ClassTreeBuilder(String _filename) {
		filename = _filename;
	}

	public void visit(MainClass n) {
		JClass mc = new JClass(n);
		MJava.declareClass(mc.Name(), mc);

		if (!filename.equals(mc.Name() + ".java")) {
			ErrorHandler.send("Missing main class " + filename.substring(0, filename.length() - 5), n.f1);
		}
	}

	public void visit(ClassDeclaration n) {
		JClass c = new JClass(n.f1, null, n.f3, n.f4);
		MJava.declareClass(c.Name(), c);
	}

	public void visit(ClassExtendsDeclaration n) {
		JClass c = new JClass(n.f1, n.f3, n.f5, n.f6);
		MJava.declareClass(c.Name(), c);
	}
}