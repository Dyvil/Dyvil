package dyvil.tools.compiler.ast.classes;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;

public interface IClass extends IASTNode, INamed, IModified, ITyped, ITypeList, IAnnotatable, IContext
{
	public CompilationUnit getUnit();
	
	public Package getPackage();
	
	public Type getSuperType();
	
	public List<Type> getInterfaces();
	
	public Type toType();
	
	public void setBody(ClassBody body);
	
	public ClassBody getBody();
	
	public boolean isSuperType(Type t);
	
	public void getMethodMatches(List<MethodMatch> matches, Type type, String name, Type... argumentTypes);
	
	public boolean isMember(IMember member);
	
	// Compilation
	
	public String getInternalName();
	
	public String getSignature();
	
	public String[] getInterfaceArray();
	
	public void write(ClassWriter writer);
}
