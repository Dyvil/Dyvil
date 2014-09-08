package dyvil.tools.compiler;

public enum CompilerState
{	
	TOKENIZE,
	PARSE,
	CHECK_SEMANTICS,
	FOLD_CONSTANTS,
	CONVERT,
	OPTIMIZE,
	COMPILE;
}
