package dyvil.tools.compiler.ast.reference;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ArrayReference implements IReference
{
	private IValue receiver;
	private IValue argument;

	public ArrayReference(IValue receiver, IValue argument)
	{

		this.receiver = receiver;
		this.argument = argument;
	}

	@Override
	public void check(ICodePosition position, MarkerList markers)
	{
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
	}

	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{

	}
}
