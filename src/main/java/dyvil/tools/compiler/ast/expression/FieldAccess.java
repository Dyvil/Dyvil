package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.ThisValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.marker.Warning;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;

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
	public IValue applyState(CompilerState state, IContext context)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.applyState(state, context);
		}
		
		if (state == CompilerState.RESOLVE)
		{
			if (this.instance != null)
			{
				context = this.instance.getType();
			}
			
			this.field = context.resolveField(this.name);
			if (this.field == null)
			{
				IMethod method = context.resolveMethod(this.name, Type.EMPTY_TYPES);
				if (method != null)
				{
					MethodCall call = new MethodCall(this.position, this.instance, this.name);
					call.method = method;
					call.isSugarCall = true;
					return call;
				}
				
				state.addMarker(new SemanticError(this.position, "'" + this.name + "' cannot be resolved to a field"));
			}
			else if (this.field.hasModifier(Modifiers.STATIC) && this.instance instanceof ThisValue)
			{
				state.addMarker(new Warning(this.position, "'" + this.name + "' is a static field and should be accessed in a static way"));
				this.instance = null;
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
