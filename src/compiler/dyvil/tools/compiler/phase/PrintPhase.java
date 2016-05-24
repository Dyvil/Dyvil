package dyvil.tools.compiler.phase;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.util.Markers;

public class PrintPhase implements ICompilerPhase
{
	private final ICompilerPhase predecessor;
	
	public PrintPhase(ICompilerPhase predecessor)
	{
		this.predecessor = predecessor;
	}
	
	@Override
	public String getName()
	{
		return "PRINT";
	}
	
	@Override
	public int getID()
	{
		return this.predecessor.getID() + 1;
	}
	
	@Override
	public void apply(DyvilCompiler compiler)
	{
		compiler.log(Markers.getInfo("phase.syntax_trees", this.predecessor.getName()));
		for (ICompilationUnit unit : compiler.fileFinder.units)
		{
			try
			{
				compiler.log(unit.getInputFile() + ":\n" + unit.toString());
			}
			catch (Throwable throwable)
			{
				compiler.error(Markers.getInfo("phase.syntax_trees.error", unit.getInputFile()), throwable);
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "PRINT[" + this.predecessor.getName() + "]";
	}
}
