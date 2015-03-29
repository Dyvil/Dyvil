package dyvil.tools.compiler.ast.dwt;

import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.ValueList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class DWTList extends ValueList
{
	public DWTList(ICodePosition position)
	{
		super(position);
	}
	
	@Override
	public int getValueType()
	{
		return DWTNode.LIST;
	}
	
	@Override
	public boolean isStatement()
	{
		return true;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('[');
		String prefix1 = prefix + '\t';
		for (IValue value : this.values)
		{
			buffer.append('\n').append(prefix1);
			value.toString(prefix1, buffer);
		}
		buffer.append('\n').append(prefix).append(']');
	}
}
