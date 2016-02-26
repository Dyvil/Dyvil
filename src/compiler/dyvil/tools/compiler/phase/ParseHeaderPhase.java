package dyvil.tools.compiler.phase;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.type.Types;

public class ParseHeaderPhase implements ICompilerPhase
{
	private final int id;
	
	public ParseHeaderPhase(int id)
	{
		this.id = id;
	}
	
	@Override
	public String getName()
	{
		return "PARSE_HEADERS";
	}
	
	@Override
	public int getID()
	{
		return this.id;
	}
	
	@Override
	public void apply(DyvilCompiler compiler)
	{
		Types.initHeaders();
		
		for (ICompilationUnit unit : compiler.fileFinder.units)
		{
			unit.parseHeader();
		}
	}
	
	@Override
	public String toString()
	{
		return "PARSE_HEADERS";
	}
}
