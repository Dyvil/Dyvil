package dyvil.tools.compiler;

import java.util.List;

import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.bytecode.ClassWriter;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.util.Util;

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
	 * Resolves packages, classes and types.
	 */
	RESOLVE_TYPES,
	/**
	 * Resolves methods and field names.
	 */
	RESOLVE,
	/**
	 * Checks for semantical errors.
	 */
	CHECK,
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
	 * Compiles the AST to byte code and stores the generated .class files in
	 * the bin directory.
	 */
	COMPILE,
	/**
	 * Converts the .class files in the bin directory to a JAR file, sets up the
	 * classpath and signs the JAR.
	 */
	JAR,
	/**
	 * Decompiles the byte code to an AST.
	 */
	DECOMPILE,
	/**
	 * Generates the Dyvildoc files.
	 */
	DYVILDOC,
	/**
	 * Prints the AST.
	 */
	DEBUG;
	
	public CodeFile	file;
	
	public void apply(List<CompilationUnit> units, IContext context)
	{
		long now = 0L;
		if (Dyvilc.debug)
		{
			Dyvilc.logger.info("Applying State " + this.name());
			now = System.nanoTime();
		}
		
		if (this == JAR)
		{
			ClassWriter.generateJAR(Dyvilc.files);
		}
		else
		{
			for (CompilationUnit unit : units)
			{
				this.file = unit.getFile();
				unit.applyState(this, context);
			}
		}
		
		if (Dyvilc.debug)
		{
			Util.logProfile(now, units.size(), "Finished State " + this.name() + " (%.1f ms, %.1f ms/CU, %.2f CU/s)");
		}
	}
	
	public void addMarker(Marker marker)
	{
		this.file.markers.add(marker);
	}
}
