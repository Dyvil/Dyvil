package dyvilx.tools.compiler.ast.expression.constant;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvil.source.position.SourcePosition;

public final class NullValue implements IConstantValue
{
	public static final NullValue NULL = new NullValue();

	private SourcePosition position;

	public NullValue()
	{
	}

	public NullValue(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public int valueTag()
	{
		return IValue.NULL;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public IType getType()
	{
		return Types.NULL;
	}

	@Override
	public Object toObject()
	{
		return null;
	}

	@Override
	public int stringSize()
	{
		return 4;
	}

	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		builder.append("null");
		return true;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		writer.visitInsn(Opcodes.ACONST_NULL);
	}

	@Override
	public String toString()
	{
		return "null";
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("null");
	}
}
