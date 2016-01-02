package dyvil.tools.compiler.ast.reference;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface IReference
{
	default void resolve(ICodePosition position, MarkerList markers, IContext context)
	{
	}

	default void checkTypes(ICodePosition position, MarkerList markers, IContext context)
	{
	}

	default void check(ICodePosition position, MarkerList markers, IContext context)
	{
	}
	
	default void cleanup(IContext context, IClassCompilableList compilableList)
	{
	}
	
	void writeReference(MethodWriter writer) throws BytecodeException;
}
