package dyvil.tools.compiler.phase;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.parsing.marker.MarkerList;

public interface IResolvable
{
	void resolveTypes(MarkerList markers, IContext context);

	void resolve(MarkerList markers, IContext context);

	void checkTypes(MarkerList markers, IContext context);

	void check(MarkerList markers, IContext context);

	void foldConstants();

	void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList);
}
