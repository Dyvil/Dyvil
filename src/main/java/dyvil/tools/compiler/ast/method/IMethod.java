package dyvil.tools.compiler.ast.method;

import java.util.List;

import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IMethodContext;
import dyvil.tools.compiler.ast.type.Type;

public interface IMethod extends IASTObject, INamed, ITyped, IValued, IModified, IAnnotatable, IThrower, IParameterized, IMethodContext
{
	public default boolean hasSignature(String name, Type... types)
	{
		if (name.equals(this.getName()))
		{
			List<Parameter> parameters = this.getParameters();
			
			if (parameters.size() != types.length)
			{
				return false;
			}
			for (int i = 0; i < types.length; i++)
			{
				if (!types[i].equals(parameters.get(i)))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public default IClass resolveClass(String name)
	{
		return this.getType().resolveClass(name);
	}
	
	@Override
	public default IField resolveField(String name)
	{
		// FIXME Local variables
		return this.getType().resolveField(name);
	}
	
	@Override
	public default IMethod resolveMethodName(String name)
	{
		return this.getType().resolveMethodName(name);
	}
	
	@Override
	public default IMethod resolveMethod(String name, Type... args)
	{
		return this.getType().resolveMethod(name, args);
	}
}
