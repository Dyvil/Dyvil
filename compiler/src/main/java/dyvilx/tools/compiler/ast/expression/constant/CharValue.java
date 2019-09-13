package dyvilx.tools.compiler.ast.expression.constant;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.LiteralConversion;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.lexer.StringLiterals;
import dyvilx.tools.parsing.marker.MarkerList;

public final class CharValue implements IConstantValue
{
	private static final byte TYPE_CHAR   = 1;
	private static final byte TYPE_STRING = 2;

	protected SourcePosition position;
	protected String         value;

	private byte type;

	public CharValue(SourcePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}

	public CharValue(SourcePosition position, String value, boolean forceChar)
	{
		this.position = position;
		this.value = value;
		this.type = forceChar ? TYPE_CHAR : TYPE_STRING;
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
		return IValue.CHAR;
	}

	@Override
	public IType getType()
	{
		return this.type == TYPE_CHAR ? Types.CHAR : Types.STRING;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.type != TYPE_CHAR)
		{
			if (Types.isSuperType(type, Types.STRING))
			{
				this.type = TYPE_STRING;
				return this;
			}

			final Annotation annotation = type.getAnnotation(Types.FROMSTRING_CLASS);
			if (annotation != null)
			{
				this.type = TYPE_STRING;
				return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
			}
		}

		if (this.type != TYPE_STRING && this.value.length() == 1)
		{
			switch (type.getTypecode())
			{
			// char is binary assignment compatible to these primitive types
			case PrimitiveType.BYTE_CODE:
			case PrimitiveType.SHORT_CODE:
			case PrimitiveType.INT_CODE:
			case PrimitiveType.CHAR_CODE:
				this.type = TYPE_CHAR;
				return this;
			case PrimitiveType.LONG_CODE:
				// assigning char literal to long -> convert the literal
				return new LongValue(this.position, this.value.charAt(0));
			}

			final Annotation annotation = type.getAnnotation(Types.FROMCHAR_CLASS);
			if (annotation != null)
			{
				this.type = TYPE_CHAR;
				return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
			}
		}

		return null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (this.type != TYPE_CHAR)
		{
			if (Types.isSameType(type, Types.STRING))
			{
				return EXACT_MATCH;
			}
			if (Types.isSuperType(type, Types.STRING))
			{
				return SUBTYPE_MATCH;
			}
			if (type.getAnnotation(Types.FROMSTRING_CLASS) != null)
			{
				return CONVERSION_MATCH;
			}
		}

		if (this.value.length() == 1 && this.type != TYPE_STRING)
		{
			switch (type.getTypecode())
			{
			case PrimitiveType.CHAR_CODE:
				return SECONDARY_MATCH;
			case PrimitiveType.BYTE_CODE:
			case PrimitiveType.SHORT_CODE:
			case PrimitiveType.INT_CODE:
			case PrimitiveType.LONG_CODE:
				return CONVERSION_MATCH;
			}
			if (type.getAnnotation(Types.FROMCHAR_CLASS) != null)
			{
				return CONVERSION_MATCH;
			}
		}
		return MISMATCH;
	}

	@Override
	public int intValue()
	{
		return this.value.charAt(0);
	}

	@Override
	public long longValue()
	{
		return this.value.charAt(0);
	}

	@Override
	public float floatValue()
	{
		return this.value.charAt(0);
	}

	@Override
	public double doubleValue()
	{
		return this.value.charAt(0);
	}

	@Override
	public String stringValue()
	{
		return this.value;
	}

	@Override
	public Object toObject()
	{
		if (this.type == TYPE_CHAR)
		{
			return this.value.charAt(0);
		}
		return this.value;
	}

	@Override
	public int stringSize()
	{
		return this.value.length();
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
		if (this.type == TYPE_CHAR)
		{
			writer.visitLdcInsn(this.value.charAt(0));

			if (type != null)
			{
				Types.CHAR.writeCast(writer, type, this.lineNumber());
			}
			return;
		}

		writer.visitLdcInsn(this.value);
		if (type != null)
		{
			Types.STRING.writeCast(writer, type, this.lineNumber());
		}
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		StringLiterals.appendCharLiteral(this.value, buffer);
	}
}
