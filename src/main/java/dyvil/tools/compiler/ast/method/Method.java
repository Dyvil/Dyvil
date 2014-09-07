package dyvil.tools.compiler.ast.method;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;

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
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		buffer.append(Formatting.Method.parametersStart);
		if (!this.parameters.isEmpty())
		{
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
		}
		buffer.append(Formatting.Method.parametersEnd);
		
		IValue statement = this.getValue();
		if (statement != null)
		{
			buffer.append(Formatting.Method.signatureBodySeperator);
			statement.toString(prefix, buffer);
		}
		buffer.append(';');
	}
}
