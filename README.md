A simple mini-java compiler  

### Compile
javac -d bin -sourcepath src -g -encoding UTF-8 src/minijava/typecheck/Root.java  
javac -d bin -cp .;bin -sourcepath src -g -encoding UTF-8 src/Main.java  

### Run
cd bin
java Main [file or folder]  