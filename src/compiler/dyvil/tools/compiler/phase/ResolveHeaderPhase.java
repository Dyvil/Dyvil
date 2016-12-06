package dyvil.tools.compiler.phase;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.header.ICompilationUnit;
import dyvil.tools.compiler.ast.type.builtin.Types;

public class ResolveHeaderPhase implements ICompilerPhase
{
	private static final String NAME = "RESOLVE_HEADERS";
	private final int id;

	public ResolveHeaderPhase(int id)
	{
		this.id = id;
	}

	@Override
	public String getName()
	{
		return NAME;
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
		compiler.checkLibraries();

		for (ICompilationUnit unit : compiler.fileFinder.units)
		{
			unit.resolveHeaders();
		}
	}

	@Override
	public String toString()
	{
		return NAME;
	}
}
