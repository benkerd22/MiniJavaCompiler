package minijava.typecheck;

import java.io.*;
import minijava.*;
import minijava.symbol.*;
import tools.*;

/*
TypeCheck Bonus
	Too big integer literal
	Wrong main class name
	Dead code (warning)
	Method and Var have the same name (warning)
*/

class MJP {
	private static boolean b = true; // the first time using MiniJavaParser?

	public static void accept(final File f) {
		try {
			InputStream inf = new FileInputStream(f);
			if (b) {
				new MiniJavaParser(inf);
				b = false;
			} else
				MiniJavaParser.ReInit(inf);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}
}

public abstract class TypeCheck {
	public static boolean check(String src) {
		File f = new File(src);
        if (!f.isFile()) {
            System.out.println(src + " is not a folder nor a file");
            return false;
		}
		
		MJP.accept(f);
		
		try {
			MJava.init(MiniJavaParser.Goal(), f.getName());
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			return false;
		}

		System.out.println("Type checking " + f.toPath());
		ErrorHandler.init();

		MJava.buildClass();
		MJava.buildScope();

		int err = ErrorHandler.Err();
		if (err > 0) {
			System.out.println("Found " + err + " errors");
			return false;
		} else {
			System.out.println("Done");
			return true;
		}
	}
}