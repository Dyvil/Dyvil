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

public class DoubleValue extends ASTNode implements INumericValue
{
	public static final IClass	DOUBLE_CONVERTIBLE	= Package.dyvilLangLiteral.resolveClass("DoubleConvertible");
	
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
	public int valueTag()
	{
		return DOUBLE;
	}
	
	@Override
	public IType getType()
	{
		return Types.DOUBLE;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.DOUBLE)
		{
			return this;
		}
		if (type.isSuperTypeOf(Types.DOUBLE))
		{
			return new BoxedValue(this, Types.DOUBLE.boxMethod);
		}
		if (type.getTheClass().getAnnotation(DOUBLE_CONVERTIBLE) != null)
		{
			return new LiteralExpression(this).withType(type, typeContext, markers, context);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.DOUBLE || type.isSuperTypeOf(Types.DOUBLE) || type.getTheClass().getAnnotation(DOUBLE_CONVERTIBLE) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(DOUBLE_CONVERTIBLE) != null)
		{
			return CONVERSION_MATCH;
		}
		return type.getSubTypeDistance(Types.DOUBLE);
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
	public int stringSize()
	{
		return Double.toString(this.value).length();
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
		writer.writeInsn(Opcodes.DRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('D');
	}
}
