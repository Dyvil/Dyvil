package dyvilx.tools.compiler.phase;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.parsing.marker.MarkerList;

public interface Resolvable
{
	// --------------- Resolution Phases ---------------

	void resolveTypes(MarkerList markers, IContext context);

	void resolve(MarkerList markers, IContext context);

	// --------------- Diagnostic Phases ---------------

	void checkTypes(MarkerList markers, IContext context);

	void check(MarkerList markers, IContext context);

	// --------------- Compilation Phases ---------------

	void foldConstants();

	void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList);
}
