package dyvil.tools.compiler;

import java.util.List;

import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.marker.Marker;

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
	DYVILDOC, DEBUG;
	
	public CodeFile	file;
	
	public void apply(List<CompilationUnit> units, IContext context)
	{
		long now = 0L;
		if (Dyvilc.debug)
		{
			System.out.println("Applying State " + this.name());
			now = System.nanoTime();
		}
		
		for (CompilationUnit unit : units)
		{
			this.file = unit.getFile();
			try
			{
				unit.applyState(this, context);
			}
			catch (Exception ex)
			{}
		}
		
		if (Dyvilc.debug)
		{
			now = System.nanoTime() - now;
			float n = now / 1000000F;
			float f = (float) n / units.size();
			System.out.println(String.format("Finished State %s (%.1f ms, %.1f ms/CU, %.2f CU/s)", this.name(), n, f, 1000F / f));
		}
	}
	
	public void addMarker(Marker marker)
	{
		this.file.markers.add(marker);
	}
}
