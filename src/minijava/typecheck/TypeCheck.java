package minijava.typecheck;

import java.io.*;
import minijava.*;
import minijava.symbol.*;
import tools.*;

// 整型字面值过大
// 缺少主类
// 死代码消除（警告）
// 同名的变量和方法（警告）

class MJPHelper {
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
	public static boolean check(final File src) {
		try {
			MJPHelper.accept(src);
			MJava.init(MiniJavaParser.Goal(), src.getName());
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}

		System.out.println("Type checking " + src.toPath());
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

	public static boolean check(String src) {
		File f = new File(src);

		if (f.isDirectory()) {
			return false;	// do not support dir
			/*
			File[] listOfFiles = f.listFiles();
			for (File file : listOfFiles) {
				if (file.isFile()) {
					check(file);
				}
			}
			*/
		} else if (f.isFile()) {
			return check(f);
		} else {
			System.out.println(src + " is not a folder nor a file");
			return false;
		}
	}
}