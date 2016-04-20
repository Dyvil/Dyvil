package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class DummyValue implements IValue
{
	@Override
	public int valueTag()
	{
		return UNKNOWN;
	}

	@Override
	public boolean isResolved()
	{
		return false;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
	}

	@Override
	public ICodePosition getPosition()
	{
		return null;
	}

	@Override
	public IType getType()
	{
		return Types.UNKNOWN;
	}

	@Override
	public boolean isType(IType type)
	{
		return false;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public IValue foldConstants()
	{
		return this;
	}

	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
	}

	@Override
	public String toString()
	{
		return "dummy";
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("dummy");
	}
}
