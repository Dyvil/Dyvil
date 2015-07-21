package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
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

public final class IntValue extends ASTNode implements INumericValue
{
	private static IntValue		NULL;
	
	public int					value;
	
	public IntValue(int value)
	{
		this.value = value;
	}
	
	public IntValue(ICodePosition position, int value)
	{
		this.position = position;
		this.value = value;
	}
	
	public static IntValue getNull()
	{
		if (NULL == null)
		{
			NULL = new IntValue(0);
		}
		return NULL;
	}
	
	@Override
	public int valueTag()
	{
		return INT;
	}
	
	@Override
	public IType getType()
	{
		return Types.INT;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.INT)
		{
			return this;
		}
		if (type.isSuperTypeOf(Types.INT))
		{
			return new BoxedValue(this, Types.INT.boxMethod);
		}
		if (type.getTheClass().getAnnotation(Types.INT_CONVERTIBLE_CLASS) != null)
		{
			return new LiteralExpression(this).withType(type, typeContext, markers, context);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.INT || type.isSuperTypeOf(Types.INT) || type.getTheClass().getAnnotation(Types.INT_CONVERTIBLE_CLASS) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(Types.INT_CONVERTIBLE_CLASS) != null)
		{
			return CONVERSION_MATCH;
		}
		return type.getSubTypeDistance(Types.INT);
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
	public Integer toObject()
	{
		return Integer.valueOf(this.value);
	}
	
	@Override
	public int stringSize()
	{
		return Integer.toString(this.value).length();
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
		writer.writeInsn(Opcodes.IRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}
