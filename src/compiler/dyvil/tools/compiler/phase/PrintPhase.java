package dyvil.tools.compiler.phase;

import dyvil.collection.Collection;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;

public class PrintPhase implements ICompilerPhase
{
	private final int id;
	
	public PrintPhase(int id)
	{
		this.id = id;
	}
	
	@Override
	public String getName()
	{
		return "PRINT";
	}
	
	@Override
	public int getID()
	{
		return this.id;
	}
	
	@Override
	public void apply(Collection<ICompilationUnit> units)
	{
		DyvilCompiler.log("--- Syntax Trees at the end of PARSER ---");
		for (ICompilationUnit unit : units)
		{
			DyvilCompiler.log(unit.getInputFile() + ":\n" + unit.toString());
		}
	}
	
	@Override
	public String toString()
	{
		return "PRINT";
	}
}
