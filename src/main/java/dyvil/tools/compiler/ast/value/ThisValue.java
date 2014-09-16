package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.type.Type;

public class ThisValue extends ASTObject implements IValue
{
	@Override
	public boolean isConstant()
	{
		return true;
	}
	
	@Override
	public IValue fold()
	{
		return this;
	}
	
	@Override
	public Type getType()
	{
		// FIXME
		return null;
	}
	
	@Override
	public void applyState(CompilerState state)
	{}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("this");
	}
}
