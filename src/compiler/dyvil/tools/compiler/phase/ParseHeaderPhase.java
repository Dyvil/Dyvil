package dyvil.tools.compiler.phase;

import dyvil.collection.Collection;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
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
	public void apply(Collection<ICompilationUnit> units)
	{
		Package.init();
		Types.initHeaders();
		
		for (ICompilationUnit unit : units)
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
