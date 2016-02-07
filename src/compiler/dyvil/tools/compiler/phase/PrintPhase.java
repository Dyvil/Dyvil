package dyvil.tools.compiler.phase;

import dyvil.collection.Collection;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;

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
	public void apply(Collection<ICompilationUnit> units)
	{
		DyvilCompiler.log("--- Syntax Trees at the end of " + this.predecessor.getName() + " ---");
		for (ICompilationUnit unit : units)
		{
			try
			{
				DyvilCompiler.log(unit.getInputFile() + ":\n" + unit.toString());
			}
			catch (Throwable throwable)
			{
				DyvilCompiler.error("Failed to print Syntax Tree for source file " + unit.getInputFile(), throwable);
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "PRINT[" + this.predecessor.getName() + "]";
	}
}
