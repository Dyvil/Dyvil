package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.BoxedValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralExpression;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class IntValue extends ASTNode implements INumericValue
{
	public static final IClass	INT_CONVERTIBLE	= Package.dyvilLangLiteral.resolveClass("IntConvertible");
	
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
	public Type getType()
	{
		return Types.INT;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Types.INT)
		{
			return this;
		}
		if (type.isSuperTypeOf(Types.INT))
		{
			return new BoxedValue(this, Types.INT.boxMethod);
		}
		if (type.getTheClass().getAnnotation(INT_CONVERTIBLE) != null)
		{
			return new LiteralExpression(type, this);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.INT || type.isSuperTypeOf(Types.INT) || type.getTheClass().getAnnotation(INT_CONVERTIBLE) != null;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Types.INT)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Types.INT) || type.getTheClass().getAnnotation(INT_CONVERTIBLE) != null)
		{
			return 2;
		}
		return 0;
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
	public void writeExpression(MethodWriter writer)
	{
		writer.writeLDC(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
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
