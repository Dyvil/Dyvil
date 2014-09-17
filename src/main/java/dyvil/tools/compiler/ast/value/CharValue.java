package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.type.Type;

public class CharValue extends ASTObject implements IValue
{
	public char	value;
	
	public CharValue(String value)
	{
		this.value = value.charAt(0);
	}
	
	public CharValue(char value)
	{
		this.value = value;
	}
	
	@Override
	public boolean isConstant()
	{
		return true;
	}
	
	@Override
	public Type getType()
	{
		return Type.CHAR;
	}
	
	@Override
	public CharValue applyState(CompilerState state)
	{
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('\'').append(this.value).append('\'');
	}
}
