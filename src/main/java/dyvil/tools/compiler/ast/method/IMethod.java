package dyvil.tools.compiler.ast.method;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.util.Modifiers;

public interface IMethod extends IASTObject, IMember, IValued, IThrower, IParameterized, IContext
{
	@Override
	public IMethod applyState(CompilerState state, IContext context);
	
	public void setParametersOpenBracket(String bracket);
	
	public void setParametersCloseBracket(String bracket);
	
	public default int getSignatureMatch(String name, Type type, Type... argumentTypes)
	{
		if (name.equals(this.getName()))
		{
			int pOff = 0;
			int match = 1;
			int len = argumentTypes.length;
			List<Parameter> parameters = this.getParameters();
			
			if (type != null && this.hasModifier(Modifiers.IMPLICIT))
			{
				if (len != parameters.size() - 1)
				{
					return 0;
				}
				
				Type t2 = parameters.get(0).type;
				if (type.equals(t2))
				{
					match += 2;
				}
				else if (Type.isSuperType(type, t2))
				{
					match += 1;
				}
				else
				{
					return 0;
				}
				
				pOff = 1;
			}
			else if (len != argumentTypes.length)
			{
				return 0;
			}
			
			for (int i = 0; i < len; i++)
			{
				Type t1 = parameters.get(i + pOff).type;
				Type t2 = argumentTypes[i];
				if (t1.equals(t2))
				{
					match += 2;
				}
				else if (Type.isSuperType(t1, t2))
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
