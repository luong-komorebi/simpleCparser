java -cp JFlex.jar JFlex.Main SimpleC.lex
java -cp java-cup-11a.jar java_cup.Main -interface < SimpleC.cup
javac -cp ;jflex.jar;java-cup-11a.jar *.java
java -cp ;jflex.jar;java-cup-11a.jar MyParser input.txt output.txt