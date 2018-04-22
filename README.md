## Mini-Java Compiler

Features:
- typecheck 
- compile Mini-Java to sPiglet
- compile sPiglet to Kanga

### Build

To build this project, use the following command at the root of this repository:

    javac -d bin -sourcepath src -encoding UTF-8 src/Main.java

the executable files will be stored in `bin` .

### Run

To typecheck and compile a Mini-Java source file `a.java` to Kanga, use:

    java -cp bin Main a.java

If typecheck succeeds, a Kanga source file `a.kg` (with the same name) will be created at the same directory. Otherwise the program will report the errors and terminate.

### Judge

To check the correctness, just click and run the `judge/judge.bat` in Windows. It will go through every `.java` file in the `sample` directory, and check if the output files have the same behaviour(output) as the origin Mini-Java source file.

It requires the sPiglet Parser [`judge/spp.jar`](http://compilers.cs.ucla.edu/cs132/software/spp.jar) , the Piglet Interpreter [`judge/pgi.jar`](http://compilers.cs.ucla.edu/cs132/software/pgi.jar) and the Kanga Interpreter [`judge/kgi.jar`](http://compilers.cs.ucla.edu/cs132/software/kgi.jar)