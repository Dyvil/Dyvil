package dyvil.tools.compiler;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

import dyvil.io.FileUtils;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.util.TestThread;

public class CompilerPhase implements Comparable<CompilerPhase>
{
	/**
	 * Tokenizes the input file.
	 */
	public static final CompilerPhase				TOKENIZE		= new CompilerPhase(10, "TOKENIZE", units -> {
																		for (ICompilationUnit cu : units)
																		{
																			cu.tokenize();
																		}
																	});
	
	/**
	 * Parses the token chain.
	 */
	public static final CompilerPhase				PARSE			= new CompilerPhase(20, "PARSE", units -> {
																		for (Iterator<ICompilationUnit> iterator = units.iterator(); iterator.hasNext();)
																		{
																			ICompilationUnit cu = iterator.next();
																			if (!cu.parse())
																			{
																				iterator.remove();
																			}
																		}
																	});
	
	/**
	 * Resolves packages, classes and types.
	 */
	public static final CompilerPhase				RESOLVE_TYPES	= new CompilerPhase(30, "RESOLVE_TYPES", units -> {
																		for (ICompilationUnit cu : units)
																		{
																			cu.resolveTypes();
																		}
																	});
	
	/**
	 * Resolves methods and field names.
	 */
	public static final CompilerPhase				RESOLVE			= new CompilerPhase(40, "RESOLVE", units -> {
																		for (ICompilationUnit cu : units)
																		{
																			cu.resolve();
																		}
																	});
	
	/**
	 * Checks for semantical errors.
	 */
	public static final CompilerPhase				CHECK			= new CompilerPhase(50, "CHECK", units -> {
																		for (ICompilationUnit cu : units)
																		{
																			cu.check();
																		}
																	});
	
	/**
	 * Prints the AST.
	 */
	public static final CompilerPhase				PRINT			= new CompilerPhase(60, "PRINT", units -> {
																		for (ICompilationUnit cu : units)
																		{
																			DyvilCompiler.logger.info(cu.getInputFile() + ":\n" + cu.toString());
																		}
																	});
	
	/**
	 * Saves the formatted AST to the input file
	 */
	public static final CompilerPhase				FORMAT			= new CompilerPhase(70, "FORMAT", units -> {
																		for (ICompilationUnit cu : units)
																		{
																			String s = cu.toString();
																			FileUtils.write(cu.getInputFile(), s);
																		}
																	});
	
	/**
	 * Folds constants.
	 */
	public static final CompilerPhase				FOLD_CONSTANTS	= new CompilerPhase(80, "FOLD_CONSTANTS", null)
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
																	};
	
	/**
	 * Compiles the AST to byte code and stores the generated .class files in
	 * the bin directory.
	 */
	public static final CompilerPhase				COMPILE			= new CompilerPhase(90, "COMPILE", units -> {
																		for (ICompilationUnit cu : units)
																		{
																			cu.compile();
																		}
																	});
	
	/**
	 * Converts the .class files in the bin directory to a JAR file, sets up the
	 * classpath and signs the JAR.
	 */
	public static final CompilerPhase				JAR				= new CompilerPhase(100, "JAR", units -> ClassWriter.generateJAR(DyvilCompiler.files));
	
	/**
	 * Tests the main type specified in {@link CompilerConfig#mainType}.
	 */
	public static final CompilerPhase				TEST			= new CompilerPhase(110, "TEST", units -> new TestThread().start());
	
	private int										id;
	private String									name;
	private Consumer<Collection<ICompilationUnit>>	apply;
	
	public CompilerPhase(int id, String name, Consumer<Collection<ICompilationUnit>> apply)
	{
		this.id = id;
		this.name = name;
		this.apply = apply;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void apply(Collection<ICompilationUnit> units)
	{
		this.apply.accept(units);
	}
	
	@Override
	public int compareTo(CompilerPhase o)
	{
		return Integer.compare(this.id, o.id);
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
}
