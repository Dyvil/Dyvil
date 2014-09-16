package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;

public class FieldAccess implements IValue, INamed, IValued
{
	protected IValue	instance;
	protected String	name;
	
	protected boolean	isSugarAccess;
	
	public IField		field;
	
	public FieldAccess()
	{}
	
	public FieldAccess(IValue instance, String name)
	{
		this.instance = instance;
		this.name = name;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public IValue fold()
	{
		return this;
	}
	
	@Override
	public Type getType()
	{
		return this.field.getType();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.isSugarAccess && !Formatting.Field.convertSugarAccess)
		{
			if (this.instance != null)
			{
				this.instance.toString("", buffer);
				buffer.append(Formatting.Field.sugarAccessStart);
			}
			
			buffer.append(this.name);
			buffer.append(Formatting.Field.sugarAccessEnd);
		}
		else
		{
			if (this.instance != null)
			{
				this.instance.toString("", buffer);
				buffer.append('.');
			}
			buffer.append(this.name);
		}
	}
	
	@Override
	public void applyState(CompilerState state)
	{}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.instance = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	public void setSugarAccess(boolean isSugarAccess)
	{
		this.isSugarAccess = isSugarAccess;
	}
	
	public boolean isSugarAccess()
	{
		return this.isSugarAccess;
	}
}
