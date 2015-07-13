package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.BoxedValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralExpression;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class FloatValue extends ASTNode implements INumericValue
{
	public static final IClass	FLOAT_CONVERTIBLE	= Package.dyvilLangLiteral.resolveClass("FloatConvertible");
	
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
	public int valueTag()
	{
		return FLOAT;
	}
	
	@Override
	public IType getType()
	{
		return Types.FLOAT;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.FLOAT)
		{
			return this;
		}
		if (type.isSuperTypeOf(Types.FLOAT))
		{
			return new BoxedValue(this, Types.FLOAT.boxMethod);
		}
		if (type.getTheClass().getAnnotation(FLOAT_CONVERTIBLE) != null)
		{
			return new LiteralExpression(this).withType(type, typeContext, markers, context);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.FLOAT || type.isSuperTypeOf(Types.FLOAT) || type.getTheClass().getAnnotation(FLOAT_CONVERTIBLE) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(FLOAT_CONVERTIBLE) != null)
		{
			return CONVERSION_MATCH;
		}
		return type.getSubTypeDistance(Types.FLOAT);
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
	public int stringSize()
	{
		return Float.toString(this.value).length();
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
		writer.writeInsn(Opcodes.FRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('F');
	}
}
