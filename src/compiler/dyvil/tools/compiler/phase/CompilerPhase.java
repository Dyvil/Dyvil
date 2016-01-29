package dyvil.tools.compiler.phase;

import dyvil.collection.Collection;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;

import java.util.function.Consumer;

public class CompilerPhase implements ICompilerPhase
{
	private final int                                    id;
	private final String                                 name;
	private final Consumer<Collection<ICompilationUnit>> apply;
	
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
