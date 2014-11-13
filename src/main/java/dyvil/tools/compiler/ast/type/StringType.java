package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;

public class StringType extends Type
{
	public StringType(String name)
	{
		super(name);
	}
	
	@Override
	public MethodMatch resolveMethod(IContext returnType, String name, Type... argumentTypes)
	{
		if (name.equals("$plus") && argumentTypes.length == 1)
		{
			return this.theClass.resolveMethod(returnType, "concat", argumentTypes);
		}
		
		return super.resolveMethod(returnType, name, argumentTypes);
	}
}
