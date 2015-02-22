package dyvil.tools.compiler;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.util.Util;
import dyvil.util.FileUtils;

public enum CompilerState
{
	/**
	 * Tokenizes the input file.
	 */
	TOKENIZE
	{
		@Override
		public void apply(Collection<ICompilationUnit> units)
		{
			for (ICompilationUnit cu : units)
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
		public void apply(Collection<ICompilationUnit> units)
		{
			for (Iterator<ICompilationUnit> iterator = units.iterator(); iterator.hasNext();)
			{
				ICompilationUnit cu = iterator.next();
				if (!cu.parse())
				{
					// If the file could not be parsed, do not attempt to link or compile it.
					iterator.remove();
				}
			}
		}
	},
	/**
	 * Resolves packages, classes and types.
	 */
	RESOLVE_TYPES
	{
		@Override
		public void apply(Collection<ICompilationUnit> units)
		{
			for (ICompilationUnit cu : units)
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
		public void apply(Collection<ICompilationUnit> units)
		{
			for (ICompilationUnit cu : units)
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
		public void apply(Collection<ICompilationUnit> units)
		{
			for (ICompilationUnit cu : units)
			{
				cu.check();
			}
		}
	},
	/**
	 * Prints the AST.
	 */
	PRINT
	{
		@Override
		public void apply(Collection<ICompilationUnit> units)
		{
			for (ICompilationUnit cu : units)
			{
				DyvilCompiler.logger.info(cu.getInputFile() + ":\n" + cu.toString());
			}
		}
	},
	FORMAT
	{
		@Override
		public void apply(Collection<ICompilationUnit> units)
		{
			for (ICompilationUnit cu : units)
			{
				String s = cu.toString();
				FileUtils.write(cu.getInputFile(), s);
			}
		}
	},
	/**
	 * Folds constants.
	 */
	FOLD_CONSTANTS
	{
		@Override
		public void apply(Collection<ICompilationUnit> units)
		{
			for (int i = 0; i < DyvilCompiler.constantFolding; i++)
			{
				for (ICompilationUnit cu : units)
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
		public void apply(Collection<ICompilationUnit> units)
		{
			for (ICompilationUnit cu : units)
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
		public void apply(Collection<ICompilationUnit> units)
		{
			ClassWriter.generateJAR(DyvilCompiler.files);
		}
	},
	TEST
	{
		@Override
		public void apply(Collection<ICompilationUnit> units)
		{
			new Thread()
			{
				@Override
				public void run()
				{
					String mainType = DyvilCompiler.config.mainType;
					String[] args = DyvilCompiler.config.getMainArgs();
					PrintStream out = System.out;
					PrintStream err = System.err;
					
					System.setOut(DyvilCompiler.loggerOut);
					System.setErr(DyvilCompiler.loggerErr);
					try
					{
						Class c = Class.forName(mainType);
						Method m = c.getMethod("main", String[].class);
						m.invoke(null, new Object[] { args });
					}
					catch (Throwable ex)
					{
						System.setOut(out);
						System.setErr(err);
						
						StringBuilder builder = new StringBuilder("TEST FAILED\n\n");
						builder.append("Main Type: ").append(mainType).append('\n');
						builder.append("Main Args: ").append(Arrays.toString(args));
						builder.append("\n\n----- ERROR -----\n");
						DyvilCompiler.logger.log(Level.SEVERE, builder.toString(), ex);
						
						return;
					}
					
					System.setOut(out);
					System.setErr(err);
				}
			}.start();
		}
	};
	
	public static void applyState(CompilerState state, Collection<ICompilationUnit> units)
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
	
	public void apply(Collection<ICompilationUnit> units)
	{
		
	}
}
