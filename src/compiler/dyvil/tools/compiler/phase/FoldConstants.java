package dyvil.tools.compiler.phase;


import dyvil.lang.Collection;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;

public class FoldConstants implements ICompilerPhase
{
	private int	id;
	
	public FoldConstants(int id)
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
	public int compareTo(ICompilerPhase o)
	{
		return Integer.compare(this.id, o.getID());
	}
	
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
}
