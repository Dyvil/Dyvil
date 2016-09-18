package dyvil.tools.compiler.phase;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.header.ICompilationUnit;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.lang.I18n;
import dyvil.tools.compiler.util.Util;

public class ResolveTypesPhase implements ICompilerPhase
{
	private static final String NAME = "RESOLVE_TYPES";

	private final int id;

	public ResolveTypesPhase(int id)
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
		final long now = System.nanoTime();

		// Loads primitive data types
		Types.initTypes();

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
		return NAME;
	}
}
