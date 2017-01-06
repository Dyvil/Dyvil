package dyvil.tools.compiler.ast.constant;

import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralConversion;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class LongValue implements IConstantValue
{
	private static LongValue NULL;

	protected ICodePosition position;
	protected long          value;

	public LongValue(long value)
	{
		this.value = value;
	}

	public LongValue(ICodePosition position, long value)
	{
		this.position = position;
		this.value = value;
	}

	public static LongValue getNull()
	{
		if (NULL == null)
		{
			NULL = new LongValue(0L);
		}
		return NULL;
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
	public int valueTag()
	{
		return LONG;
	}

	@Override
	public IType getType()
	{
		return Types.LONG;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.LONG)
		{
			return this;
		}
		if (type.isPrimitive())
		{
			switch (type.getTypecode())
			{
			case PrimitiveType.FLOAT_CODE:
				return new FloatValue(this.position, this.value);
			case PrimitiveType.DOUBLE_CODE:
				return new DoubleValue(this.position, this.value);
			}
		}
		if (Types.isSuperType(type, Types.LONG))
		{
			return this;
		}

		final IAnnotation annotation = type.getAnnotation(Types.FROMLONG_CLASS);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		return null;
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSuperType(type, Types.LONG) || type.getAnnotation(Types.FROMLONG_CLASS) != null;
	}

	@Override
	public int getTypeMatch(IType type)
	{
		final int i = IConstantValue.super.getTypeMatch(type);
		if (i != MISMATCH)
		{
			return i;
		}
		if (type.getAnnotation(Types.FROMLONG_CLASS) != null)
		{
			return CONVERSION_MATCH;
		}
		return MISMATCH;
	}

	@Override
	public int intValue()
	{
		return (int) this.value;
	}

	@Override
	public long longValue()
	{
		return this.value;
	}

	@Override
	public float floatValue()
	{
		return this.value;
	}

	@Override
	public double doubleValue()
	{
		return this.value;
	}

	@Override
	public Long toObject()
	{
		return this.value;
	}

	@Override
	public int stringSize()
	{
		return Long.toString(this.value).length();
	}

	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		builder.append(this.value);
		return true;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		writer.visitLdcInsn(this.value);

		if (type != null)
		{
			Types.LONG.writeCast(writer, type, this.getLineNumber());
		}
	}

	@Override
	public String toString()
	{
		return this.value + "L";
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}
