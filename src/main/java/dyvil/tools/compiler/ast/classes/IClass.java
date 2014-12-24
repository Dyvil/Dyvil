package dyvil.tools.compiler.ast.classes;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;

public interface IClass extends IASTNode, INamed, IModified, ITyped, ITypeList, IAnnotatable, IContext
{
	public Type getSuperType();
	
	public List<Type> getInterfaces();
	
	public Type toType();
	
	public void setBody(ClassBody body);
	
	public ClassBody getBody();
	
	public String getQualifiedName();
	
	public boolean isSuperType(Type t);
	
	public void getMethodMatches(List<MethodMatch> matches, Type type, String name, Type... argumentTypes);
	
	// Compilation
	
	public String getInternalName();
	
	public String getSignature();
	
	public String[] getInterfaceArray();
	
	public void write(ClassWriter writer);
}
