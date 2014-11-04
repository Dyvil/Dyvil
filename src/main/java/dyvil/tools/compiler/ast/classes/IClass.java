package dyvil.tools.compiler.ast.classes;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;

public interface IClass extends IASTObject, INamed, IModified, ITyped, ITypeList, IAnnotatable, IContext
{	
	public void setBody(ClassBody body);
	
	public ClassBody getBody();
	
	public boolean isSuperType(Type t);
	
	public void getMethodMatches(List<MethodMatch> matches, String name, Type... types);
	
	// Compilation
	
	public String getInternalName();
	
	public String getSignature();
	
	public String[] getInterfaces();
	
	public void write(ClassWriter writer);
}
