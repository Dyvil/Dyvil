package dyvil.tools.compiler.phase;

import java.util.function.Consumer;

import dyvil.lang.Collection;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;

public class ParallelCompilerPhase implements ICompilerPhase
{
	private int							id;
	private String						name;
	private Consumer<ICompilationUnit>	apply;
	
	public ParallelCompilerPhase(int id, String name, Consumer<ICompilationUnit> apply)
	{
		this.id = id;
		this.name = name;
		this.apply = apply;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public int getID()
	{
		return this.id;
	}
	
	@Override
	public void apply(Collection<ICompilationUnit> units)
	{
		for (ICompilationUnit unit : units)
		{
			try
			{
				this.apply.accept(unit);
			}
			catch (Throwable t)
			{
				DyvilCompiler.logger.warning(this.name + " failed on Compilation Unit '" + unit.getInputFile() + "'");
				DyvilCompiler.logger.throwing(this.name, "apply", t);
			}
		}
	}
	
	@Override
	public int compareTo(ICompilerPhase o)
	{
		return Integer.compare(this.id, o.getID());
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
}
