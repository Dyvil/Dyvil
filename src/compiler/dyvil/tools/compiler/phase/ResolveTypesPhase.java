package dyvil.tools.compiler.phase;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.type.builtin.Types;
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
	public void apply(DyvilCompiler compiler)
	{
		long now = System.nanoTime();
		
		// Loads primitive data types
		Types.initTypes();

		compiler.checkLibraries();
		
		if (compiler.config.isDebug())
		{
			compiler.log("Loaded Base Types (" + Util.toTime(System.nanoTime() - now) + ")");
		}
		
		for (ICompilationUnit unit : compiler.fileFinder.units)
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
