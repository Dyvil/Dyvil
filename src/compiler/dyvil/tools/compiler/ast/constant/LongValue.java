package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.BoxedValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralExpression;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class LongValue implements IConstantValue
{
	private static LongValue NULL;
	
	protected ICodePosition	position;
	protected long			value;
	
	public LongValue(long value)
	{
		this.value = value;
	}
	
	public LongValue(ICodePosition position, long value)
	{
		this.position = position;
		this.value = value;
	}
	
	public static LongValue getNull()
	{
		if (NULL == null)
		{
			NULL = new LongValue(0L);
		}
		return NULL;
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
		return LONG;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return true;
	}
	
	@Override
	public IType getType()
	{
		return Types.LONG;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.LONG)
		{
			return this;
		}
		if (type.isSuperTypeOf(Types.LONG))
		{
			return new BoxedValue(this, Types.LONG.boxMethod);
		}
		if (type.getTheClass().getAnnotation(Types.LONG_CONVERTIBLE_CLASS) != null)
		{
			return new LiteralExpression(this).withType(type, typeContext, markers, context);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.LONG || type.isSuperTypeOf(Types.LONG) || type.getTheClass().getAnnotation(Types.LONG_CONVERTIBLE_CLASS) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(Types.LONG_CONVERTIBLE_CLASS) != null)
		{
			return CONVERSION_MATCH;
		}
		return type.getSubTypeDistance(Types.LONG);
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
		return Long.valueOf(this.value);
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
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeLDC(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		writer.writeLDC(this.value);
		writer.writeInsn(Opcodes.LRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}
