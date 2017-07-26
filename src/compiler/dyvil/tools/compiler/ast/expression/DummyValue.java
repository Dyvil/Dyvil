package dyvil.tools.compiler.ast.expression;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.source.position.SourcePosition;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.constant.IConstantValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.function.BiConsumer;

public class DummyValue implements IConstantValue
{
	private final IType type;

	private final @Nullable BiConsumer<MethodWriter, IType> writer;

	public DummyValue(IType type)
	{
		this.type = type;
		this.writer = null;
	}

	public DummyValue(IType type, BiConsumer<MethodWriter, IType> writer)
	{
		this.type = type;
		this.writer = writer;
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
	public SourcePosition getPosition()
	{
		return null;
	}

	@Override
	public void setPosition(SourcePosition position)
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
		if (this.writer != null)
		{
			this.writer.accept(writer, type);
		}
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
