package dyvil.tools.compiler;

public enum CompilerState
{	
	TOKENIZE,
	PARSE,
	RESOLVE,
	OPERATOR_PRECEDENCE,
	FOLD_CONSTANTS,
	CONVERT,
	OPTIMIZE,
	COMPILE;
}
