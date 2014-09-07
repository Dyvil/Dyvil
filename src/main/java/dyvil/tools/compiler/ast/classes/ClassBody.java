package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.type.Type;

public class ClassBody
{
	private AbstractClass			theClass;
	private Map<String, IField>		fields	= new HashMap();
	private List<IMethod>	methods		= new ArrayList();
	
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
	
	public void addVariable(Field var)
	{
		String key = var.getName();
		this.fields.put(key, var);
	}
	
	public void addMethod(Method method)
	{
		this.methods.add( method);
	}
	
	public IField getField(String name)
	{
		return this.fields.get(name);
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
}
