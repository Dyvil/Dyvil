package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.Member;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Modifiers;

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
	public void applyState(CompilerState state)
	{
		if (state == CompilerState.FOLD_CONSTANTS)
		{
			if (this.value != null)
			{
				this.value = this.value.fold();
			}
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		buffer.append(Modifiers.FIELD.toString(this.modifiers));
		this.type.toString("", buffer);
		buffer.append(' ');
		buffer.append(this.name);
		
		IValue value = this.value;
		if (value != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator);
			value.toString("", buffer);
		}
		buffer.append(';');
	}
}
