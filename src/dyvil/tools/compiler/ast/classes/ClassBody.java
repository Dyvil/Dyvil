package dyvil.tools.compiler.ast.classes;

import java.util.HashMap;
import java.util.Map;

import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.Method;

public class ClassBody
{
	private AbstractClass			theClass;
	private Map<String, Variable>	variables	= new HashMap();
	private Map<String, Method>		methods		= new HashMap();
	
	public ClassBody()
	{
	}
	
	public void setTheClass(AbstractClass theClass)
	{
		this.theClass = theClass;
	}
	
	public AbstractClass getTheClass()
	{
		return this.theClass;
	}
	
	public void addVariable(Variable var)
	{
		String key = var.getName();
		this.variables.put(key, var);
	}
	
	public void addMethod(Method method)
	{
		String key = method.getName();
		this.methods.put(key, method);
	}
}
