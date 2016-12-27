package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.position.ICodePosition;

public final class NullValue implements IConstantValue
{
	public static final NullValue NULL = new NullValue();

	private ICodePosition position;

	public NullValue()
	{
	}

	public NullValue(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public int valueTag()
	{
		return IValue.NULL;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public boolean isPrimitive()
	{
		return false;
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
