package dyvil.tools.compiler;

import java.util.List;

import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.bytecode.ClassWriter;
import dyvil.tools.compiler.util.Util;

public enum CompilerState
{
	/**
	 * Tokenizes the input file.
	 */
	TOKENIZE
	{
		@Override
		public void apply(List<CompilationUnit> units)
		{
			for (CompilationUnit cu : units)
			{
				cu.tokenize();
			}
		}
	},
	/**
	 * Parsers the token chain.
	 */
	PARSE
	{
		@Override
		public void apply(List<CompilationUnit> units)
		{
			for (CompilationUnit cu : units)
			{
				cu.parse();
			}
		}
	},
	/**
	 * Resolves packages, classes and types.
	 */
	RESOLVE_TYPES
	{
		@Override
		public void apply(List<CompilationUnit> units)
		{
			for (CompilationUnit cu : units)
			{
				cu.resolveTypes();
			}
		}
	},
	/**
	 * Resolves methods and field names.
	 */
	RESOLVE
	{
		@Override
		public void apply(List<CompilationUnit> units)
		{
			for (CompilationUnit cu : units)
			{
				cu.resolve();
			}
		}
	},
	/**
	 * Checks for semantical errors.
	 */
	CHECK
	{
		@Override
		public void apply(List<CompilationUnit> units)
		{
			for (CompilationUnit cu : units)
			{
				cu.check();
			}
		}
	},
	/**
	 * Prints the AST.
	 */
	DEBUG
	{
		@Override
		public void apply(List<CompilationUnit> units)
		{
			for (CompilationUnit cu : units)
			{
				cu.debug();
			}
		}
	},
	/**
	 * Folds constants.
	 */
	FOLD_CONSTANTS
	{
		@Override
		public void apply(List<CompilationUnit> units)
		{
			for (int i = 0; i < DyvilCompiler.constantFolding; i++)
			{
				for (CompilationUnit cu : units)
				{
					cu.foldConstants();
				}
			}
		}
		
		@Override
		public String toString()
		{
			return "FOLD_CONSTANTS (" + DyvilCompiler.constantFolding + "x)";
		}
	},
	/**
	 * Compiles the AST to byte code and stores the generated .class files in
	 * the bin directory.
	 */
	COMPILE
	{
		@Override
		public void apply(List<CompilationUnit> units)
		{
			for (CompilationUnit cu : units)
			{
				cu.compile();
			}
		}
	},
	/**
	 * Converts the .class files in the bin directory to a JAR file, sets up the
	 * classpath and signs the JAR.
	 */
	JAR
	{
		@Override
		public void apply(List<CompilationUnit> units)
		{
			ClassWriter.generateJAR(DyvilCompiler.files);
		}
	};
	
	public static void applyState(CompilerState state, List<CompilationUnit> units)
	{
		long now = 0L;
		if (DyvilCompiler.debug)
		{
			DyvilCompiler.logger.info("Applying State " + state.name());
			now = System.nanoTime();
		}
		
		state.apply(units);
		
		if (DyvilCompiler.debug)
		{
			Util.logProfile(now, units.size(), "Finished State " + state.name() + " (%.1f ms, %.1f ms/CU, %.2f CU/s)");
		}
	}
	
	public void apply(List<CompilationUnit> units)
	{
		
	}
}
