package dyvil.tools.compiler.phase;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.header.ICompilationUnit;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.lang.I18n;
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

		Types.initHeaders();
		
		// Loads primitive data types
		Types.initTypes();

		compiler.checkLibraries();
		
		if (compiler.config.isDebug())
		{
			compiler.log(I18n.get("library.types.loaded", Util.toTime(System.nanoTime() - now)));
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
