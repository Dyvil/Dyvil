package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.method.IMethod;

public class StringType extends Type
{
	public StringType(String name)
	{
		super(name);
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		if (name.equals("+") && args.length == 1)
		{
			return this.theClass.resolveMethod("concat", args);
		}
		
		return super.resolveMethod(name, args);
	}
}
