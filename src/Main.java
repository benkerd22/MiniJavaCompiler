import minijava.typecheck.*;
import minijava.minijava2piglet.*;
import spiglet.spiglet2kanga.*;
import kanga.kanga2MIPS.*;

public class Main {
	private static int pos(String filename) {
		int i = filename.lastIndexOf(".");
		int p = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));

		if (i > p)
			return i;
		return filename.length();
	}

	private static String name(String filename) {
		return filename.substring(0, pos(filename));
	}

	private static String ext(String filename) {
		return filename.substring(pos(filename));
	}

	public static void main(String[] args) {
		try {
			String f = "samples\\ccc.java"; // default source file
			if (args.length > 0)
				f = args[0];

			String n = name(f), e = ext(f);
			switch (e) {
			case ".java":
				boolean succ = TypeCheck.check(f);
				if (!succ)
					return;

				ToPiglet.compile(f, n + ".spg");
			case ".spg":
				ToKanga.compile(n + ".spg", n + ".kg");
			case ".kg":
				ToMIPS.compile(n + ".kg", n + ".s");
			}
		} catch (Exception e) {
			System.out.println("Oops");
			e.printStackTrace();
		}
	}
}