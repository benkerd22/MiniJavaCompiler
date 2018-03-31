import minijava.typecheck.TypeCheck;
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
			if (args.length > 1) {
				TypeCheck.check(args[1]);
			} else {
				TypeCheck.check("samples/Factorial.java");
			}

			ToPiglet.compile("samples/Factorial.java");
		} catch (Exception e) {
			System.out.println("Oops");
			e.printStackTrace();
		}
	}
}