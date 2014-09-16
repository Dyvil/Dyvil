package dyvil.tools.compiler.ast.method;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Modifiers;

public class Method extends Member implements IMethod
{
	private IValue				statement;
	
	private List<Parameter>		parameters			= new ArrayList();
	private List<ThrowsDecl>	throwsDeclarations	= new ArrayList();
	
	@Override
	public void setParameters(List<Parameter> parameters)
	{
		this.parameters = parameters;
	}
	
	@Override
	public void setValue(IValue statement)
	{
		this.statement = statement;
	}
	
	@Override
	public void setThrows(List<ThrowsDecl> throwsDecls)
	{
		this.throwsDeclarations = throwsDecls;
	}
	
	@Override
	public IValue getValue()
	{
		return this.statement;
	}
	
	@Override
	public List<Parameter> getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public List<ThrowsDecl> getThrows()
	{
		return this.throwsDeclarations;
	}
	
	@Override
	public boolean isStatic()
	{
		return this.hasModifier(Modifiers.STATIC);
	}
	
	@Override
	public void applyState(CompilerState state)
	{
		if (state == CompilerState.FOLD_CONSTANTS)
		{
			if (this.statement != null)
			{
				this.statement = this.statement.fold();
			}
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		if (!this.parameters.isEmpty())
		{
			buffer.append(Formatting.Method.parametersStart);
			Iterator<Parameter> iterator = this.parameters.iterator();
			while (true)
			{
				Parameter parameter = iterator.next();
				parameter.toString("", buffer);
				
				if (iterator.hasNext())
				{
					buffer.append(parameter.getSeperator());
				}
				else
				{
					break;
				}
			}
			buffer.append(Formatting.Method.parametersEnd);
		}
		else
		{
			buffer.append(Formatting.Method.emptyParameters);
		}
		
		IValue statement = this.getValue();
		if (statement != null)
		{
			buffer.append(Formatting.Method.signatureBodySeperator);
			statement.toString(prefix, buffer);
		}
		buffer.append(';');
	}
}
