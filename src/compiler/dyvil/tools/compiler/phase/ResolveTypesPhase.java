package dyvil.tools.compiler.phase;

import dyvil.collection.Collection;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.util.Util;

public class ResolveTypesPhase implements ICompilerPhase
{
	private int id;
	
	public ResolveTypesPhase(int id)
	{
		this.id = id;
	}
	
	@Override
	public String getName()
	{
		return "RESOLVE_TYPES";
	}
	
	@Override
	public int getID()
	{
		return this.id;
	}
	
	@Override
	public void apply(Collection<ICompilationUnit> units)
	{
		long now = System.nanoTime();
		
		Package.init();
		// Loads primitive data types
		Types.init();
		
		if (DyvilCompiler.debug)
		{
			DyvilCompiler.log("Loaded Base Types (" + Util.toTime(System.nanoTime() - now) + ")");
		}
		
		for (ICompilationUnit unit : units)
		{
			unit.resolveTypes();
		}
	}
	
	@Override
	public String toString()
	{
		return "RESOLVE_TYPES";
	}
}
