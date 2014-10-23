package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class FieldAccess extends ASTObject implements IValue, INamed, IValued
{
	protected IValue	instance;
	protected String	name;
	
	protected boolean	isSugarAccess;
	
	public IField		field;
	
	public FieldAccess(ICodePosition position)
	{
		this.position = position;
	}
	
	public FieldAccess(ICodePosition position, IValue instance, String name)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public Type getType()
	{
		return this.field.getType();
	}
	
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
	
	@Override
	public FieldAccess applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE)
		{
			this.field = context.resolveField(this.name);
			if (this.field == null)
			{
				state.addMarker(new SyntaxError(this.position, "'" + this.name + "' cannot be resolved to a field"));
			}
		}
		return this;
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
}
