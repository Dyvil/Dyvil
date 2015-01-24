package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class DoubleValue extends ASTNode implements INumericValue
{
	public double	value;
	
	public DoubleValue(double value)
	{
		this.value = value;
	}
	
	public DoubleValue(ICodePosition position, double value)
	{
		this.position = position;
		this.value = value;
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
		writer.visitLdcInsn(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.position == null)
		{
			buffer.append(this.value).append('D');
			return;
		}
		
		buffer.append(this.position.getText());
	}
}
