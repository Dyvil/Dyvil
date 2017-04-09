package dyvil.tools.compiler.ast.expression;

import dyvil.annotation.internal.NonNull;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.constant.IConstantValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class DummyValue implements IConstantValue
{
	private final IType type;

	public DummyValue(IType type)
	{
		this.type = type;
	}

	@Override
	public int valueTag()
	{
		return UNKNOWN;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public ICodePosition getPosition()
	{
		return null;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public int stringSize()
	{
		return -1;
	}

	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		return false;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
	}

	@Override
	public String toString()
	{
		return "dummy<" + this.type + ">";
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append("dummy<");
		this.type.toString(indent, buffer);
		buffer.append('>');
	}
}
