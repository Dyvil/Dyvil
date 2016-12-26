package dyvil.tools.compiler.phase;

import dyvil.annotation.internal.NonNull;
import dyvil.tools.compiler.DyvilCompiler;

import java.util.function.Consumer;

public class CompilerPhase implements ICompilerPhase
{
	private final int                     id;
	private final String                  name;
	private final Consumer<DyvilCompiler> apply;
	
	public CompilerPhase(int id, String name, Consumer<DyvilCompiler> apply)
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
		this.apply.accept(compiler);
	}
	
	@Override
	public int compareTo(@NonNull ICompilerPhase o)
	{
		return Integer.compare(this.id, o.getID());
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
}
