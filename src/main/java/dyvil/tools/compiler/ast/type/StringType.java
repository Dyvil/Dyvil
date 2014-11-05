package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.method.MethodMatch;

public class StringType extends Type
{
	public StringType(String name)
	{
		super(name);
	}
	
	@Override
	public MethodMatch resolveMethod(String name, Type returnType, Type... argumentTypes)
	{
		if (name.equals("+") && argumentTypes.length == 1)
		{
			return this.theClass.resolveMethod("concat", returnType, argumentTypes);
		}
		
		return super.resolveMethod(name, returnType, argumentTypes);
	}
}
