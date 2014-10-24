package dyvil.tools.compiler;

import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.ast.structure.Package;

public enum CompilerState
{
	/**
	 * Tokenizes the input file.
	 */
	TOKENIZE,
	/**
	 * Parsers the token chain.
	 */
	PARSE,
	/**
	 * Resolves packages, classes, methods and field names.
	 */
	RESOLVE,
	/**
	 * Obfuscates the code.
	 */
	OBFUSCATE,
	/**
	 * Applies operator precendence rules to method calls.
	 */
	OPERATOR_PRECEDENCE,
	/**
	 * Folds constants.
	 */
	FOLD_CONSTANTS,
	/**
	 * Converts the Dyvil AST to a valid Java AST.
	 */
	CONVERT,
	/**
	 * Optimizes the code on the AST.
	 */
	OPTIMIZE,
	/**
	 * Compiles the AST to byte code.
	 */
	COMPILE,
	/**
	 * Decompiles the byte code to an AST.
	 */
	DECOMPILE,
	/**
	 * Generates the Dyvildoc files.
	 */
	DYVILDOC, ;
	
	public Package	rootPackage;
	public CodeFile	file;
	
	public void addMarker(Marker marker)
	{
		this.file.markers.add(marker);
	}
}
