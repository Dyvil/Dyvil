package dyvilx.tools.compiler.ast.expression.constant;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.annotation.IAnnotation;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.LiteralConversion;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.raw.ClassType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.lexer.StringLiterals;
import dyvilx.tools.parsing.marker.MarkerList;

public final class StringValue implements IConstantValue
{
	protected SourcePosition position;
	protected String        value;

	public StringValue(String value)
	{
		this.value = value;
	}

	public StringValue(SourcePosition position, String value)
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
		return STRING;
	}

	@Override
	public ClassType getType()
	{
		return Types.STRING;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (Types.isSuperType(type, Types.STRING))
		{
			return this;
		}

		final IAnnotation annotation = type.getAnnotation(Types.FROMSTRING_CLASS);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		return null;
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSuperType(type, Types.STRING) || type.getAnnotation(Types.FROMSTRING_CLASS) != null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		final int i = IConstantValue.super.getTypeMatch(type, implicitContext);
		if (i != MISMATCH)
		{
			return i;
		}
		if (type.getAnnotation(Types.FROMSTRING_CLASS) != null)
		{
			return CONVERSION_MATCH;
		}
		return MISMATCH;
	}

	@Override
	public Object toObject()
	{
		return this.value;
	}

	@Override
	public String stringValue()
	{
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
		StringLiterals.appendStringLiteral(this.value, buffer);
	}
}
