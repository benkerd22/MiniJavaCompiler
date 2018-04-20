import minijava.typecheck.*;
import minijava.minijava2piglet.*;
import spiglet.spiglet2kanga.*;

public class Main {
	public static String ext(String filename) {
		return filename.replace(".java", "");
	}

	public static void main(String[] args) {
		try {
			String def = "samples\\LinkedList.java"; // default source file
			if (args.length > 0)
				def = args[0];

			def = ext(def);

			boolean succ = TypeCheck.check(def + ".java");
			if (!succ)
				return;

			ToPiglet.compile(def + ".java", def + ".spg");

			ToKanga.compile(def + ".spg", def + ".kg");
		} catch (Exception e) {
			System.out.println("Oops");
			e.printStackTrace();
		}
	}
}