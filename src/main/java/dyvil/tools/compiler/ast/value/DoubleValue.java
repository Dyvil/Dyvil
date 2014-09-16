package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.type.Type;

public class DoubleValue extends ASTObject implements IValue
{
	public double	value;
	
	public DoubleValue(String value)
	{
		this.value = Double.parseDouble(value);
	}
	
	public DoubleValue(double value)
	{
		this.value = value;
	}
	
	@Override
	public IValue fold()
	{
		return this;
	}
	
	@Override
	public Type getType()
	{
		return Type.DOUBLE;
	}
	
	@Override
	public void applyState(CompilerState state)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('D');
	}
}
