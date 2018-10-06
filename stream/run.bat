@echo off
SET location=%1
SET classpath=".;vendor\twitter4j-core-4.0.4.jar;vendor\twitter4j-stream-4.0.4.jar;vendor\json-simple-1.1.1.jar;"
:while
    del *.class /S
    javac -cp %classpath% Stream.java
    java -cp %classpath% Stream %location%
    goto :while