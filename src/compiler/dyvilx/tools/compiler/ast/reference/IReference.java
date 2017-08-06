package dyvilx.tools.compiler.ast.reference;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public interface IReference
{
	default void resolve(SourcePosition position, MarkerList markers, IContext context)
	{
	}

	default void checkTypes(SourcePosition position, MarkerList markers, IContext context)
	{
	}

	default void check(SourcePosition position, MarkerList markers, IContext context)
	{
	}
	
	default void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
	}
	
	void writeReference(MethodWriter writer) throws BytecodeException;
}
