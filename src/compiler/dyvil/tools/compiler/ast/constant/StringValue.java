package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralExpression;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class StringValue implements IConstantValue
{
	protected ICodePosition	position;
	protected String		value;
	
	public StringValue(String value)
	{
		this.value = value;
	}
	
	public StringValue(ICodePosition position, String value)
	{
		this.position = position;
		this.value = value;
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
		if (type == Types.STRING || type.isSuperTypeOf(Types.STRING))
		{
			return this;
		}
		
		IAnnotation annotation = type.getTheClass().getAnnotation(Types.STRING_CONVERTIBLE_CLASS);
		if (annotation != null)
		{
			return new LiteralExpression(this, annotation).withType(type, typeContext, markers, context);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(Types.STRING) || type.getTheClass().getAnnotation(Types.STRING_CONVERTIBLE_CLASS) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(Types.STRING_CONVERTIBLE_CLASS) != null)
		{
			return CONVERSION_MATCH;
		}
		return type.getSubTypeDistance(Types.STRING);
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
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeLDC(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		writer.writeLDC(this.value);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.value.length();
		buffer.ensureCapacity(buffer.length() + len + 3);
		buffer.append('"');
		append(this.value, len, buffer);
		buffer.append('"');
	}
	
	public static void append(String value, int len, StringBuilder buffer)
	{
		for (int i = 0; i < len; i++)
		{
			char c = value.charAt(i);
			switch (c)
			{
			case '"':
				buffer.append("\\\"");
				continue;
			case '\\':
				buffer.append("\\\\");
				continue;
			case '\n':
				buffer.append("\\n");
				continue;
			case '\t':
				buffer.append("\\t");
				continue;
			case '\r':
				buffer.append("\\r");
				continue;
			case '\b':
				buffer.append("\\b");
				continue;
			case '\f':
				buffer.append("\\f");
				continue;
			}
			buffer.append(c);
		}
	}
}
