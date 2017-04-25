package dyvil.tools.compiler.ast.reference;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
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
