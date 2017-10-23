package dyvilx.tools.compiler.ast.expression.constant;

import dyvil.lang.Formattable;
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
		if (this.type == TYPE_CHAR)
		{
			return Types.CHAR;
		}
		return Types.STRING;
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
			if (Types.isSuperType(type, Types.CHAR))
			{
				this.type = TYPE_CHAR;
				return this;
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
	public boolean isType(IType type)
	{
		if (this.value.length() == 1 && this.type != TYPE_STRING)
		{
			if (Types.isSuperType(type, Types.CHAR) || type.getAnnotation(Types.FROMCHAR_CLASS) != null)
			{
				return true;
			}
		}
		else if (this.type == TYPE_CHAR)
		{
			return false;
		}

		return Types.isSuperType(type, Types.STRING) || (type.getAnnotation(Types.FROMSTRING_CLASS) != null);
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (this.value.length() == 1 && this.type != TYPE_STRING)
		{
			if (Types.isSameType(type, Types.CHAR))
			{
				return EXACT_MATCH;
			}
			if (Types.isSuperType(type, Types.CHAR))
			{
				return SUBTYPE_MATCH;
			}
			if (type.getAnnotation(Types.FROMCHAR_CLASS) != null)
			{
				return CONVERSION_MATCH;
			}
		}

		if (this.type == TYPE_CHAR)
		{
			return MISMATCH;
		}
		if (Types.isSameType(type, Types.STRING))
		{
			return SECONDARY_MATCH;
		}
		if (Types.isSuperType(type, Types.STRING))
		{
			return SECONDARY_SUBTYPE_MATCH;
		}
		if (type.getAnnotation(Types.FROMSTRING_CLASS) != null)
		{
			return CONVERSION_MATCH;
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
	public void toString(String prefix, StringBuilder buffer)
	{
		StringLiterals.appendCharLiteral(this.value, buffer);
	}
}
