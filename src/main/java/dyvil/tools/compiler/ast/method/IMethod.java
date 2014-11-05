package dyvil.tools.compiler.ast.method;

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
	
	public int getSignatureMatch(String name, Type type, Type... argumentTypes);
	
	// Compilation
	
	public String getDescriptor();
	
	public String getSignature();
	
	public String[] getExceptions();
	
	public void write(ClassWriter writer);
}
