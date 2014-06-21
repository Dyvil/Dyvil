package dyvil.tools.compiler.ast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dyvil.tools.compiler.ast.member.Variable;
import dyvil.tools.compiler.ast.member.methods.Constructor;
import dyvil.tools.compiler.ast.member.methods.Method;

public class ClassBody
{
	private Map<String, Variable> variables = new HashMap();
	private Map<String, Method> methods = new HashMap();
	private List<Constructor> constructors = new LinkedList();
	
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
	
	public void addConstructor(Constructor constructor)
	{
		this.constructors.add(constructor);
	}
}
