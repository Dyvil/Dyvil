package dyvilx.tools.compiler.ast.expression.constant;

import dyvilx.tools.compiler.ast.annotation.IAnnotation;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.LiteralConversion;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public final class IntValue implements IConstantValue
{
	private static IntValue NULL;

	protected SourcePosition position;
	protected int           value;

	public IntValue(int value)
	{
		this.value = value;
	}

	public IntValue(SourcePosition position, int value)
	{
		this.position = position;
		this.value = value;
	}

	public static IntValue getNull()
	{
		if (NULL == null)
		{
			NULL = new IntValue(0);
		}
		return NULL;
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
	public int valueTag()
	{
		return INT;
	}

	@Override
	public IType getType()
	{
		return Types.INT;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.INT)
		{
			return this;
		}
		if (type.isPrimitive())
		{
			switch (type.getTypecode())
			{
			case PrimitiveType.CHAR_CODE:
				return new CharValue(this.position, String.valueOf((char) this.value), true);
			case PrimitiveType.BYTE_CODE:
			case PrimitiveType.SHORT_CODE:
			case PrimitiveType.INT_CODE:
				return this;
			case PrimitiveType.LONG_CODE:
				return new LongValue(this.position, this.value);
			case PrimitiveType.FLOAT_CODE:
				return new FloatValue(this.position, this.value);
			case PrimitiveType.DOUBLE_CODE:
				return new DoubleValue(this.position, this.value);
			}
		}
		if (Types.isSuperType(type, Types.INT))
		{
			return this;
		}

		final IAnnotation annotation = type.getAnnotation(Types.FROMINT_CLASS);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		return null;
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSuperType(type, Types.INT) || type.getAnnotation(Types.FROMINT_CLASS) != null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		final int i = IConstantValue.super.getTypeMatch(type, implicitContext);
		if (i != MISMATCH)
		{
			return i;
		}
		if (type.getAnnotation(Types.FROMINT_CLASS) != null)
		{
			return CONVERSION_MATCH;
		}
		return MISMATCH;
	}

	@Override
	public int intValue()
	{
		return this.value;
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
	public Integer toObject()
	{
		return this.value;
	}

	@Override
	public int stringSize()
	{
		return Integer.toString(this.value).length();
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
			Types.INT.writeCast(writer, type, this.lineNumber());
		}
	}

	@Override
	public String toString()
	{
		return Integer.toString(this.value);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}
