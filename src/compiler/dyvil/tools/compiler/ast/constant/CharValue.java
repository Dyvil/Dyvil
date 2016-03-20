package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralConversion;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.lexer.LexerUtil;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class CharValue implements IConstantValue
{
	private static final byte CHAR   = 1;
	private static final byte STRING = 2;

	protected ICodePosition position;
	protected String        value;

	private byte type;

	public CharValue(ICodePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}

	public CharValue(ICodePosition position, String value, boolean forceChar)
	{
		this.position = position;
		this.value = value;
		this.type = forceChar ? CHAR : STRING;
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
		return IValue.CHAR;
	}

	@Override
	public IType getType()
	{
		if (this.type == CHAR)
		{
			return Types.CHAR;
		}
		return Types.STRING;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.value.length() == 1 && this.type != STRING)
		{
			if (Types.isSuperType(type, Types.CHAR))
			{
				this.type = CHAR;
				return this;
			}

			final IAnnotation annotation = type.getAnnotation(Types.CHAR_CONVERTIBLE_CLASS);
			if (annotation != null)
			{
				this.type = CHAR;
				return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
			}
		}

		if (this.type == CHAR)
		{
			return null;
		}

		if (Types.isSuperType(type, Types.STRING))
		{
			this.type = STRING;
			return this;
		}

		final IAnnotation annotation = type.getAnnotation(Types.STRING_CONVERTIBLE_CLASS);
		if (annotation != null)
		{
			this.type = STRING;
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}

		return null;
	}

	@Override
	public boolean isType(IType type)
	{
		if (this.value.length() == 1 && this.type != STRING)
		{
			if (Types.isSuperType(type, Types.CHAR) || type.getAnnotation(Types.CHAR_CONVERTIBLE_CLASS) != null)
			{
				return true;
			}
		}
		else if (this.type == CHAR)
		{
			return false;
		}

		return Types.isSuperType(type, Types.STRING) || (type.getAnnotation(Types.STRING_CONVERTIBLE_CLASS) != null);
	}

	@Override
	public int getTypeMatch(IType type)
	{
		if (this.value.length() == 1 && this.type != STRING)
		{
			final int distance = Types.getDistance(type, Types.CHAR);
			if (distance > 0)
			{
				return distance;
			}

			if (type.getAnnotation(Types.CHAR_CONVERTIBLE_CLASS) != null)
			{
				return CONVERSION_MATCH;
			}
		}

		if (this.type == CHAR)
		{
			return 0;
		}
		final int distance = Types.getDistance(type, Types.STRING);
		if (distance > 0F)
		{
			return distance;
		}
		if (type.getAnnotation(Types.STRING_CONVERTIBLE_CLASS) != null)
		{
			return CONVERSION_MATCH + 1;
		}
		return 0;
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
		if (this.type == CHAR)
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
		if (this.type == CHAR)
		{
			writer.visitLdcInsn(this.value.charAt(0));

			if (type != null)
			{
				Types.CHAR.writeCast(writer, type, this.getLineNumber());
			}
			return;
		}

		writer.visitLdcInsn(this.value);
		if (type == Types.VOID)
		{
			writer.visitInsn(Opcodes.ARETURN);
		}
		else if (type != null)
		{
			Types.STRING.writeCast(writer, type, this.getLineNumber());
		}
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		LexerUtil.appendCharLiteral(this.value, buffer);
	}
}
