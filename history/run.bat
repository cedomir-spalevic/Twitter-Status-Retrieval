@echo off
SET username=%1
SET ending_date=%2
SET beginning_date=%3
SET classpath=".;vendor\twitter4j-core-4.0.4.jar;vendor\twitter4j-stream-4.0.4.jar;vendor\json-simple-1.1.1.jar;"
javac -cp %classpath% History.java
java -cp %classpath% History %username% %ending_date% %beginning_date%
del *.class /S