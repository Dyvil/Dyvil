package dyvil.tools.compiler.ast.method;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;

public interface IMethod extends IASTObject, IMember, IValued, IThrower, IParameterized, IContext
{
	@Override
	public IMethod applyState(CompilerState state, IContext context);
	
	public void setParametersOpenBracket(String bracket);
	
	public void setParametersCloseBracket(String bracket);
	
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
				Type t = parameters.get(i).type;
				if (!types[i].isAssignableFrom(t))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	// Compilation
	
	public void write(ClassWriter writer);

	public String getDescriptor();
	
	public String getSignature();
	
	public String[] getExceptions();
}
