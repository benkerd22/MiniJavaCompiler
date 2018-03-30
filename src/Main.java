import java.io.*;
import minijava.typecheck.TypeCheck;

public class Main {
	String s;
	/*	TODO
			s.compareTo(anotherString)
			s.hashCode()
			s.indexOf(str)
			s.isEmpty()
			s.length()*/

	public static void main(String[] args) {
		try {
			//System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("log.txt"))));
			//TypeCheck.check(new File("minijava/samples/ccc.java"));
			TypeCheck.check("samples");
		} catch (Exception e) {
			System.out.println("Oops");
			e.printStackTrace();
		}
	}
}