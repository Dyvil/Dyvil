package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.value.BoxValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.LiteralValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class FloatValue extends ASTNode implements INumericValue
{
	public static final Type	FLOAT_CONVERTIBLE	= new Type(Package.dyvilLangLiteral.resolveClass("FloatConvertible"));
	
	private static FloatValue	NULL;
	
	public float				value;
	
	public FloatValue(float value)
	{
		this.value = value;
	}
	
	public FloatValue(ICodePosition position, float value)
	{
		this.position = position;
		this.value = value;
	}
	
	public static FloatValue getNull()
	{
		if (NULL == null)
		{
			NULL = new FloatValue(0F);
		}
		return NULL;
	}
	
	@Override
	public int getValueType()
	{
		return FLOAT;
	}
	
	@Override
	public Type getType()
	{
		return Types.FLOAT;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Types.FLOAT)
		{
			return this;
		}
		if (type.isSuperTypeOf(Types.FLOAT))
		{
			return new BoxValue(this, Types.FLOAT.boxMethod);
		}
		if (FLOAT_CONVERTIBLE.isSuperTypeOf(type))
		{
			return new LiteralValue(type, this);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.FLOAT || type.isSuperTypeOf(Types.FLOAT) || FLOAT_CONVERTIBLE.isSuperTypeOf(type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Types.FLOAT)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Types.FLOAT) || FLOAT_CONVERTIBLE.isSuperTypeOf(type))
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
		return this.value;
	}
	
	@Override
	public double doubleValue()
	{
		return this.value;
	}
	
	@Override
	public Float toObject()
	{
		return Float.valueOf(this.value);
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
		writer.writeInsn(Opcodes.FRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('F');
	}
}
