package dyvilx.tools.compiler.ast.expression.constant;

import dyvilx.tools.compiler.ast.annotation.IAnnotation;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.LiteralConversion;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class DoubleValue implements IConstantValue
{
	private static DoubleValue NULL;

	protected SourcePosition position;
	protected double        value;

	public DoubleValue(double value)
	{
		this.value = value;
	}

	public DoubleValue(SourcePosition position, double value)
	{
		this.position = position;
		this.value = value;
	}

	public static DoubleValue getNull()
	{
		if (NULL == null)
		{
			NULL = new DoubleValue(0D);
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
		return DOUBLE;
	}

	@Override
	public IType getType()
	{
		return Types.DOUBLE;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (Types.isSuperType(type, Types.DOUBLE))
		{
			return this;
		}

		final IAnnotation annotation = type.getAnnotation(Types.FROMDOUBLE_CLASS);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		return null;
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSuperType(type, Types.DOUBLE) || type.getAnnotation(Types.FROMDOUBLE_CLASS) != null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		final int i = IConstantValue.super.getTypeMatch(type, implicitContext);
		if (i != MISMATCH)
		{
			return i;
		}
		if (type.getAnnotation(Types.FROMDOUBLE_CLASS) != null)
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
		return (long) this.value;
	}

	@Override
	public float floatValue()
	{
		return (float) this.value;
	}

	@Override
	public double doubleValue()
	{
		return this.value;
	}

	@Override
	public Double toObject()
	{
		return this.value;
	}

	@Override
	public int stringSize()
	{
		return Double.toString(this.value).length();
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
			Types.DOUBLE.writeCast(writer, type, this.lineNumber());
		}
	}

	@Override
	public String toString()
	{
		if (!Double.isFinite(this.value))
		{
			return String.valueOf(this.value);
		}
		return this.value + "D";
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
		if (Double.isFinite(this.value))
		{
			buffer.append('D');
		}
	}
}
