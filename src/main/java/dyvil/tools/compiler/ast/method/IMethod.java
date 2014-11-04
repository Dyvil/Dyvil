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
	
	public default int getSignatureMatch(String name, Type... types)
	{
		if (name.equals(this.getName()))
		{
			List<Parameter> parameters = this.getParameters();
			
			if (parameters.size() != types.length)
			{
				return 0;
			}
			
			int match = 1;
			for (int i = 0; i < types.length; i++)
			{
				Type t1 = parameters.get(i).type;
				Type t2 = types[i];
				if (t1.equals(t2))
				{	
					match += 2;
				}
				else if (Type.isSuperType(t1, types[i]))
				{
					match += 1;
				}
				else
				{
					return 0;
				}
			}
			return match;
		}
		return 0;
	}
	
	// Compilation
	
	public String getDescriptor();
	
	public String getSignature();
	
	public String[] getExceptions();
	
	public void write(ClassWriter writer);
}
