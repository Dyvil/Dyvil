package dyvilx.tools.compiler.ast.expression.constant;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
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

public class FloatValue implements IConstantValue
{
	private static FloatValue NULL;

	protected SourcePosition position;
	protected float          value;

	public FloatValue(float value)
	{
		this.value = value;
	}

	public FloatValue(SourcePosition position, float value)
	{
		this.position = position;
		this.value = value;
	}

	public static FloatValue getNull()
	{
		if (NULL == null)
		{
			NULL = new FloatValue(0F);
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
		return FLOAT;
	}

	@Override
	public IType getType()
	{
		return Types.FLOAT;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.FLOAT)
		{
			return this;
		}
		if (Types.isExactType(type, Types.DOUBLE))
		{
			return new DoubleValue(this.position, this.value);
		}
		if (Types.isSuperType(type, Types.FLOAT))
		{
			return this;
		}

		final Annotation annotation = type.getAnnotation(Types.FROMFLOAT_CLASS);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		return null;
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSuperType(type, Types.FLOAT) || type.getAnnotation(Types.FROMFLOAT_CLASS) != null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		final int i = IConstantValue.super.getTypeMatch(type, implicitContext);
		if (i != MISMATCH)
		{
			return i;
		}
		if (type.getAnnotation(Types.FROMFLOAT_CLASS) != null)
		{
			return CONVERSION_MATCH;
		}
		return 0;
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
		return this.value;
	}

	@Override
	public double doubleValue()
	{
		return this.value;
	}

	@Override
	public Float toObject()
	{
		return this.value;
	}

	@Override
	public int stringSize()
	{
		return Float.toString(this.value).length();
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
			Types.FLOAT.writeCast(writer, type, this.lineNumber());
		}
	}

	@Override
	public String toString()
	{
		if (!Float.isFinite(this.value))
		{
			return String.valueOf(this.value);
		}
		return this.value + "F";
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
		if (Float.isFinite(this.value))
		{
			buffer.append('F');
		}
	}
}
