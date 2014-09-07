package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.Member;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;

public class Field extends Member implements IField
{
	private IValue	value;
	
	public Field()
	{}
	
	public Field(String name)
	{
		super(name);
	}
	
	public Field(String name, Type type)
	{
		super(name, type);
	}
	
	public Field(String name, Type type, int modifiers)
	{
		super(name, type, modifiers);
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		IValue value = this.getValue();
		if (value != null)
		{
			value.toString(Formatting.Field.keyValueSeperator, buffer);
		}
		buffer.append(';');
	}
}
