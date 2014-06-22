package dyvil.tools.compiler.ast.classes;

import java.util.HashMap;
import java.util.Map;

import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.Method;

public class ClassBody
{
	private Map<String, Variable>	variables	= new HashMap();
	private Map<String, Method>		methods		= new HashMap();
	
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
