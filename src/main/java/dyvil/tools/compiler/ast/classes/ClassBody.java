package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.Type;

public class ClassBody extends ASTObject
{
	private AbstractClass		theClass;
	private List<IField>	fields	= new ArrayList();
	private List<IMethod>		methods	= new ArrayList();
	
	public ClassBody()
	{}
	
	public void setTheClass(AbstractClass theClass)
	{
		this.theClass = theClass;
	}
	
	public AbstractClass getTheClass()
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
	public void applyState(CompilerState state)
	{
		for (IField field : this.fields)
		{
			field.applyState(state);
		}
		
		for (IMethod method : this.methods)
		{
			method.applyState(state);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (IField field : this.fields)
		{
			field.toString(prefix, buffer);
			buffer.append('\n');
		}
		
		if (!this.methods.isEmpty())
		{
			buffer.append("\n");
			
			for (IMethod method : this.methods)
			{
				method.toString(prefix, buffer);
				buffer.append('\n');
				buffer.append('\n');
			}
		}
	}
}
