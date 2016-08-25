package dyvil.tools.compiler.phase;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.header.ICompilationUnit;

import java.util.function.Consumer;

public class SequentialCompilerPhase implements ICompilerPhase
{
	private final int                        id;
	private final String                     name;
	private final Consumer<ICompilationUnit> apply;
	
	public SequentialCompilerPhase(int id, String name, Consumer<ICompilationUnit> apply)
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
	public void apply(DyvilCompiler compiler)
	{
		compiler.fileFinder.units.forEach(this.apply);
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
