package dyvil.tools.compiler.phase;

import java.util.function.Consumer;

import dyvil.lang.Collection;

import dyvil.tools.compiler.ast.structure.ICompilationUnit;

public class CompilerPhase implements ICompilerPhase
{
	private int										id;
	private String									name;
	private Consumer<Collection<ICompilationUnit>>	apply;
	
	public CompilerPhase(int id, String name, Consumer<Collection<ICompilationUnit>> apply)
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
		this.apply.accept(units);
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
