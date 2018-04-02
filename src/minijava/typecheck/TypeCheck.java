package minijava.typecheck;

import java.io.*;
import minijava.*;
import minijava.symbol.*;

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

		System.out.println("Type checking " + f.toPath());
	}
}

public abstract class TypeCheck {
	public static void check(final File f) {
		try {
			String filename = f.getName();
			MJPHelper.accept(f);

			MJava.init(MiniJavaParser.Goal(), filename);
			MJava.buildClass();
			MJava.buildScope();
			//MJava.show();

			System.out.println("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void check(String src) {
		File f = new File(src);

		if (f.isDirectory()) {
			File[] listOfFiles = f.listFiles();
			for (File file : listOfFiles) {
				if (file.isFile()) {
					check(file);
				}
			}
		} else if (f.isFile()) {
			check(f);
		} else {
			System.out.println(src + " is not a folder nor a file");
			return;
		}

		System.out.println("DONE");
	}
}