package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.method.MethodMatch;

public interface IType extends IASTNode, INamed, IContext
{
	public void setClass(IClass theClass);
	
	public IClass getTheClass();
	
	public void setArrayDimensions(int dimensions);
	
	public int getArrayDimensions();
	
	public void setVarargs();
	
	public void addArrayDimension();
	
	public boolean isArrayType();
	
	public IType getSuperType();
	
	public boolean isAssignableFrom(IType type);
	
	public boolean classEquals(IType type);
	
	// Resolve
	
	public IType resolve(IContext context);
	
	public boolean isResolved();
	
	public void getMethodMatches(List<MethodMatch> list, IType type, String name, IType... argumentTypes);
	
	// Compilation
	
	public String getInternalName();
	
	public String getExtendedName();
	
	public String getSignature();
	
	public Object getFrameType();
	
	public int getLoadOpcode();
	
	public int getArrayLoadOpcode();
	
	public int getStoreOpcode();
	
	public int getArrayStoreOpcode();
	
	public int getReturnOpcode();
	
	@Override
	public IType applyState(CompilerState state, IContext context);
	
	public IType clone();
}
