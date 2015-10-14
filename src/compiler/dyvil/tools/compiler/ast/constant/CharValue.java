package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralExpression;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class CharValue implements IConstantValue
{
	protected ICodePosition	position;
	protected char			value;
	
	public CharValue(char value)
	{
		this.value = value;
	}
	
	public CharValue(ICodePosition position, char value)
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
		return CHAR;
	}
	
	@Override
	public IType getType()
	{
		return Types.CHAR;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.CHAR || type.isSuperTypeOf(Types.CHAR))
		{
			return this;
		}
		IAnnotation annotation = type.getTheClass().getAnnotation(Types.CHAR_CONVERTIBLE_CLASS);
		if (annotation != null)
		{
			return new LiteralExpression(this, annotation).withType(type, typeContext, markers, context);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.CHAR || type.isSuperTypeOf(Types.CHAR) || type.getTheClass().getAnnotation(Types.CHAR_CONVERTIBLE_CLASS) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(Types.CHAR_CONVERTIBLE_CLASS) != null)
		{
			return CONVERSION_MATCH;
		}
		return type.getSubTypeDistance(Types.CHAR);
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
	public Character toObject()
	{
		return Character.valueOf(this.value);
	}
	
	@Override
	public int stringSize()
	{
		return 1;
	}
	
	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		builder.append(this.value);
		return true;
	}
	
	@Override
	public void writeExpression(MethodWriter visitor) throws BytecodeException
	{
		visitor.writeLDC(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		writer.writeLDC(this.value);
		writer.writeInsn(Opcodes.IRETURN);
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.ensureCapacity(buffer.length() + 4);
		buffer.append('\'');
		switch (this.value)
		{
		case '\'':
			buffer.append("\\'");
			break;
		case '\\':
			buffer.append("\\\\");
			break;
		case '\n':
			buffer.append("\\n");
			break;
		case '\t':
			buffer.append("\\t");
			break;
		case '\r':
			buffer.append("\\r");
			break;
		case '\b':
			buffer.append("\\b");
			break;
		case '\f':
			buffer.append("\\f");
			break;
		default:
			buffer.append(this.value);
		}
		buffer.append('\'');
	}
}
