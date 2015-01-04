package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.method.MethodMatch;

public interface IType extends IASTNode, INamed, IContext
{
	// Container Class
	
	public void setClass(IClass theClass);
	
	public IClass getTheClass();
	
	// Arrays
	
	public void setArrayDimensions(int dimensions);
	
	public int getArrayDimensions();
	
	public void addArrayDimension();
	
	public boolean isArrayType();
	
	// Super Type
	
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
	
	public void appendExtendedName(StringBuilder buffer);
	
	public String getSignature();
	
	public void appendSignature(StringBuilder buffer);
	
	public Object getFrameType();
	
	// Opcodes
	
	public int getLoadOpcode();
	
	public int getArrayLoadOpcode();
	
	public int getStoreOpcode();
	
	public int getArrayStoreOpcode();
	
	public int getReturnOpcode();
	
	// Misc
	
	@Override
	public IType applyState(CompilerState state, IContext context);
	
	public IType clone();
}
