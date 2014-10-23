package dyvil.tools.compiler.ast.method;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Modifiers;

public class Method extends Member implements IMethod
{
	private IValue				statement;
	
	private List<Parameter>		parameters			= new ArrayList(3);
	private List<ThrowsDecl>	throwsDeclarations	= new ArrayList(1);
	
	public Method(IClass iclass)
	{
		super(iclass);
	}
	
	@Override
	public void setParameters(List<Parameter> parameters)
	{
		this.parameters = parameters;
	}
	
	@Override
	public List<Parameter> getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public void addParameter(Parameter parameter)
	{
		this.parameters.add(parameter);
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
	public List<ThrowsDecl> getThrows()
	{
		return this.throwsDeclarations;
	}
	
	@Override
	public void addThrows(ThrowsDecl throwsDecl)
	{
		this.throwsDeclarations.add(throwsDecl);
	}
	
	@Override
	public IValue getValue()
	{
		return this.statement;
	}
	
	@Override
	public boolean isStatic()
	{
		return this.hasModifier(Modifiers.STATIC);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.theClass.resolveClass(name);
	}
	
	@Override
	public IField resolveField(String name)
	{
		for (Parameter param : this.parameters)
		{
			if (param.name.equals(name))
			{
				return param;
			}
		}
		
		return this.theClass.resolveField(name);
	}
	
	@Override
	public IMethod resolveMethodName(String name)
	{
		return this.theClass.resolveMethodName(name);
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		return this.theClass.resolveMethod(name, args);
	}
	
	@Override
	public Method applyState(CompilerState state, IContext context)
	{
		if (this.statement != null)
		{
			this.statement = this.statement.applyState(state, this);
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		buffer.append(Modifiers.METHOD.toString(this.modifiers));
		if (this.type != null)
		{
			this.type.toString("", buffer);
			buffer.append(' ');
		}
		buffer.append(this.name);
		
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
