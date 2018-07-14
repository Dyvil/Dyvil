package dyvilx.tools.compiler.ast.expression.constant;

import dyvil.annotation.internal.NonNull;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.CastOperator;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.LiteralConversion;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public class LongValue implements IConstantValue
{
	public static final LongValue ZERO = new LongValue(0);

	protected SourcePosition position;
	protected long           value;

	public LongValue(long value)
	{
		this.value = value;
	}

	public LongValue(SourcePosition position, long value)
	{
		this.position = position;
		this.value = value;
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
		switch (type.getTypecode())
		{
		case PrimitiveType.LONG_CODE:
			return this;
		case PrimitiveType.FLOAT_CODE:
			return new FloatValue(this.position, this.value);
		case PrimitiveType.DOUBLE_CODE:
			return new DoubleValue(this.position, this.value);
		}

		if (Types.isSuperType(type, Types.LONG.getObjectType()))
		{
			return new CastOperator(this, Types.LONG.getObjectType());
		}

		final Annotation annotation = type.getAnnotation(Types.FROMLONG_CLASS);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		return null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		final int i = IConstantValue.super.getTypeMatch(type, implicitContext);
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
			Types.LONG.writeCast(writer, type, this.lineNumber());
		}
	}

	@Override
	public String toString()
	{
		return this.value + "L";
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}
