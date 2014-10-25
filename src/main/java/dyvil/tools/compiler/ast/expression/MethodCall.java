package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ParserUtil;

public class MethodCall extends Call implements INamed, IValued
{
	protected IValue	instance;
	protected String	name;
	
	public MethodCall(ICodePosition position)
	{
		super(position);
	}
	
	public MethodCall(ICodePosition position, IValue instance, String name)
	{
		super(position);
		this.instance = instance;
		this.name = name;
	}
	
	@Override
	public Type getType()
	{
		return this.descriptor.getType();
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
		if (this.isSugarCall)
		{
			this.arguments.add(value);
		}
		else
		{
			this.instance = value;
		}
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	@Override
	public void setIsArray(boolean isArray)
	{}
	
	@Override
	public boolean isArray()
	{
		return false;
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.applyState(state, context);
		}
		
		super.applyState(state, context);
		
		if (state == CompilerState.RESOLVE)
		{
			if (this.instance != null)
			{
				context = this.instance.getType();
			}
			
			try
			{
				this.descriptor = context.resolveMethod(this.name, this.getTypes());
			}
			catch (Exception ex)
			{}
			
			if (this.descriptor == null)
			{
				// This might be a field access instead of a method call
				IField field = context.resolveField(this.name);
				if (field != null)
				{
					// Yes it is, convert this method call to a field access
					FieldAccess fieldAccess = new FieldAccess(this.position, this.instance, this.name);
					fieldAccess.field = field;
					fieldAccess.isSugarAccess = this.isSugarCall;
					return fieldAccess;
				}
				
				state.addMarker(new SemanticError(this.position, "'" + this.name + "' cannot be resolved to a method"));
			}
		}
		// TODO Operator precedence
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.isSugarCall && !Formatting.Method.convertSugarCalls)
		{
			if (this.instance != null)
			{
				this.instance.toString("", buffer);
				buffer.append(Formatting.Method.sugarCallStart);
			}
			
			buffer.append(this.name);
			buffer.append(Formatting.Method.sugarCallEnd);
			this.arguments.get(0).toString("", buffer);
		}
		else
		{
			if (this.instance != null)
			{
				this.instance.toString("", buffer);
				buffer.append('.');
			}
			buffer.append(this.name);
			ParserUtil.parametersToString(this.arguments, buffer, !this.isSugarCall);
		}
	}
}
