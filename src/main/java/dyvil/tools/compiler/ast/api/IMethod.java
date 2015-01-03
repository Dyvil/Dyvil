package dyvil.tools.compiler.ast.api;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.CompilerState;

public interface IMethod extends IASTNode, IMember, IGeneric, IValued, IThrower, IVariableList, IParameterized, IContext
{
	@Override
	public IMethod applyState(CompilerState state, IContext context);
	
	public void checkArguments(CompilerState state, IValue instance, List<IValue> arguments);
	
	public int getSignatureMatch(String name, IType type, IType... argumentTypes);
	
	// Compilation
	
	public String getDescriptor();
	
	public String getSignature();
	
	public String[] getExceptions();
	
	public void write(ClassWriter writer);
}
