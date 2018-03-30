import minijava.typecheck.TypeCheck;

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
			//System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("log.txt"))));
			//TypeCheck.check(new File("minijava/samples/ccc.java"));
			//TypeCheck.check("samples");
			if (args.length > 1) {
				TypeCheck.check(args[1]);
			} else {
				TypeCheck.check("samples");
			}
		} catch (Exception e) {
			System.out.println("Oops");
			e.printStackTrace();
		}
	}
}