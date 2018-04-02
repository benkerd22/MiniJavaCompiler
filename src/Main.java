import minijava.typecheck.*;
import minijava.minijava2piglet.*;

public class Main {
	/*	TODO
		String s;
		s.compareTo(anotherString)
		s.hashCode()
		s.indexOf(str)
		s.isEmpty()
		s.length()
	*/

	public static void main(String[] args) {
		try {
			String def = "samples\\TreeVisitor.java"; // default source file
			if (args.length > 1)
				def = args[1];

			boolean succ = TypeCheck.check(def);
			if (!succ)
				return;

			ToPiglet.compile(def);
		} catch (Exception e) {
			System.out.println("Oops");
			e.printStackTrace();
		}
	}
}