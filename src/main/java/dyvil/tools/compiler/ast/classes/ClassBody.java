package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ClassBody extends ASTObject
{
	private IClass			theClass;
	private List<IField>	fields	= new ArrayList();
	private List<IMethod>	methods	= new ArrayList();
	
	public ClassBody(ICodePosition position)
	{
		this.position = position;
	}
	
	public void setTheClass(IClass theClass)
	{
		this.theClass = theClass;
	}
	
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	public void addVariable(IField var)
	{
		this.fields.add(var);
	}
	
	public void addMethod(IMethod method)
	{
		this.methods.add(method);
	}
	
	public IField getField(String name)
	{
		for (IField field : this.fields)
		{
			if (name.equals(field.getName()))
			{
				return field;
			}
		}
		return null;
	}
	
	public IMethod getMethod(String name)
	{
		for (IMethod method : this.methods)
		{
			if (name.equals(method.getName()))
			{
				return method;
			}
		}
		return null;
	}
	
	public IMethod getMethod(String name, Type... args)
	{
		for (IMethod method : this.methods)
		{
			if (method.hasSignature(name, args))
			{
				return method;
			}
		}
		return null;
	}
	
	@Override
	public ClassBody applyState(CompilerState state)
	{
		this.fields.replaceAll(f -> f.applyState(state));
		this.methods.replaceAll(m -> m.applyState(state));
		
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (!this.fields.isEmpty())
		{
			for (IField field : this.fields)
			{
				field.toString(prefix, buffer);
				buffer.append('\n');
			}
			buffer.append('\n');
		}
		
		if (!this.methods.isEmpty())
		{
			for (IMethod method : this.methods)
			{
				method.toString(prefix, buffer);
				buffer.append('\n');
				buffer.append('\n');
			}
		}
	}
}
