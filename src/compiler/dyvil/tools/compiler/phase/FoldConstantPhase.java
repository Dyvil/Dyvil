package dyvil.tools.compiler.phase;

import dyvil.annotation.analysis.NotNull;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.header.ICompilationUnit;

public class FoldConstantPhase implements ICompilerPhase
{
	private final int id;
	
	public FoldConstantPhase(int id)
	{
		this.id = id;
	}
	
	@Override
	public String getName()
	{
		return "FOLD_CONSTANTS";
	}
	
	@Override
	public int getID()
	{
		return this.id;
	}
	
	@Override
	public int compareTo(@NotNull ICompilerPhase o)
	{
		return Integer.compare(this.id, o.getID());
	}
	
	@Override
	public void apply(DyvilCompiler compiler)
	{
		final int folding = compiler.config.getConstantFolding();

		for (int i = 0; i < folding; i++)
		{
			for (ICompilationUnit unit : compiler.fileFinder.units)
			{
				unit.foldConstants();
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "FOLD_CONSTANTS";
	}
}
