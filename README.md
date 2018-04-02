## Mini-Java Compiler

Features:
- typecheck 
- Compile Mini-Java to sPiglet

### Compile

To compile this project, use the following command at the root of this repository:

    javac -d bin -sourcepath src -encoding UTF-8 src/Main.java

the executable files will be stored in `bin` .

### Run

To typecheck and compile a Mini-Java source file `a.java` to sPiglet, use:

    java -cp bin Main a.java

If typecheck fails, the program will report the errors and terminate. Otherwise a sPiglet source file `a.spg` with the same name will be created and stored at the same directory.