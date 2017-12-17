package dyvilx.tools.compiler.ast.expression;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.constant.IConstantValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class DummyValue implements IConstantValue
{
	private final @NonNull Supplier<IType> type;

	private final @Nullable BiConsumer<MethodWriter, IType> writer;

	public DummyValue(Supplier<IType> typer)
	{
		this.type = typer;
		this.writer = null;
	}

	public DummyValue(Supplier<IType> typer, BiConsumer<MethodWriter, IType> writer)
	{
		this.type = typer;
		this.writer = writer;
	}

	public DummyValue(IType type)
	{
		this.type = () -> type;
		this.writer = null;
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
		return this.type.get();
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
		this.type.get().toString(indent, buffer);
		buffer.append('>');
	}
}
