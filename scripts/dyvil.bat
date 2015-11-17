set DYVIL_HOME=%~dp0\..
java -Xbootclasspath/a:%DYVIL_HOME%\lib\dyvil-compiler.jar -Xbootclasspath/a:%DYVIL_HOME%\lib\dyvil-library.jar -jar %DYVIL_HOME%\lib\dyvil-repl.jar %*
