package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.BoxValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.LiteralValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class DoubleValue extends ASTNode implements INumericValue
{
	public static final Type	DOUBLE_CONVERTIBLE	= new Type(Package.dyvilLangLiteral.resolveClass("DoubleConvertible"));
	
	private static DoubleValue	NULL;
	
	public double				value;
	
	public DoubleValue(double value)
	{
		this.value = value;
	}
	
	public DoubleValue(ICodePosition position, double value)
	{
		this.position = position;
		this.value = value;
	}
	
	public static DoubleValue getNull()
	{
		if (NULL == null)
		{
			NULL = new DoubleValue(0D);
		}
		return NULL;
	}
	
	@Override
	public int getValueType()
	{
		return DOUBLE;
	}
	
	@Override
	public Type getType()
	{
		return Type.DOUBLE;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Type.DOUBLE)
		{
			return this;
		}
		if (type.isSuperTypeOf(Type.DOUBLE))
		{
			return new BoxValue(this, Type.DOUBLE.boxMethod);
		}
		if (DOUBLE_CONVERTIBLE.isSuperTypeOf(type))
		{
			return new LiteralValue(type, this);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.DOUBLE || type.isSuperTypeOf(Type.DOUBLE) || DOUBLE_CONVERTIBLE.isSuperTypeOf(type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Type.DOUBLE)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Type.DOUBLE) || DOUBLE_CONVERTIBLE.isSuperTypeOf(type))
		{
			return 2;
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
		return (float) this.value;
	}
	
	@Override
	public double doubleValue()
	{
		return this.value;
	}
	
	@Override
	public Double toObject()
	{
		return Double.valueOf(this.value);
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
		writer.writeInsn(Opcodes.DRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('D');
	}
}
