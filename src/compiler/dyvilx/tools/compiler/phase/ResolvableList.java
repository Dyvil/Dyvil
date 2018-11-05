package dyvilx.tools.compiler.phase;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.parsing.marker.MarkerList;

public interface ResolvableList<T extends Resolvable> extends Iterable<T>, Resolvable
{
	// --------------- Resolution Phases ---------------

	@Override
	default void resolveTypes(MarkerList markers, IContext context)
	{
		for (Resolvable resolvable : this)
		{
			resolvable.resolveTypes(markers, context);
		}
	}

	@Override
	default void resolve(MarkerList markers, IContext context)
	{
		for (Resolvable resolvable : this)
		{
			resolvable.resolve(markers, context);
		}
	}

	// --------------- Diagnostic Phases ---------------

	@Override
	default void checkTypes(MarkerList markers, IContext context)
	{
		for (Resolvable resolvable : this)
		{
			resolvable.checkTypes(markers, context);
		}
	}

	@Override
	default void check(MarkerList markers, IContext context)
	{
		for (Resolvable resolvable : this)
		{
			resolvable.check(markers, context);
		}
	}

	// --------------- Compilation Phases ---------------

	@Override
	default void foldConstants()
	{
		for (Resolvable resolvable : this)
		{
			resolvable.foldConstants();
		}
	}

	@Override
	default void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (Resolvable resolvable : this)
		{
			resolvable.cleanup(compilableList, classCompilableList);
		}
	}
}
