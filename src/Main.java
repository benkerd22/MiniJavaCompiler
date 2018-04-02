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
			TypeCheck.check("samples/BubbleSort.java");

			ToPiglet.compile("out.pg");
		} catch (Exception e) {
			System.out.println("Oops");
			e.printStackTrace();
		}
	}
}