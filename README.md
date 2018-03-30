## Mini-Java Compiler

Features:
- typecheck 

### Compile
To compile the project and store the `.class` files in `bin`, use the following commands.

    javac -d bin -sourcepath src -g -encoding UTF-8 src/minijava/typecheck/Root.java  
    javac -d bin -cp bin -sourcepath src -g -encoding UTF-8 src/Main.java  

If `javac` doesn't exists, try:

Windows PowerShell:

    $Env:path = $Env:path + ";C:\Program Files\Java\jdk-(version)\bin"
    

### Run
Assume that the `.class` files in `bin`, to run the project:

    java -cp bin Main [file or folder]  