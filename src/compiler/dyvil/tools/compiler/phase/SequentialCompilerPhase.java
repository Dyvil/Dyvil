package dyvil.tools.compiler.phase;

import java.util.function.Consumer;

import dyvil.collection.Collection;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;

public class SequentialCompilerPhase implements ICompilerPhase
{
	private int							id;
	private String						name;
	private Consumer<ICompilationUnit>	apply;
	
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
	public void apply(Collection<ICompilationUnit> units)
	{
		units.forEach(this.apply);
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
