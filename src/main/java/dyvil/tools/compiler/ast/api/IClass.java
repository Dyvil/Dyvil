package dyvil.tools.compiler.ast.api;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;

public interface IClass extends IASTNode, INamed, IModified, ITyped, ITypeList, IAnnotatable, IContext
{
	public CompilationUnit getUnit();
	
	public Package getPackage();
	
	public IType getSuperType();
	
	public List<IType> getInterfaces();
	
	public IType toType();
	
	public void setBody(ClassBody body);
	
	public ClassBody getBody();
	
	public boolean isSuperType(IType t);
	
	public void getMethodMatches(List<MethodMatch> matches, IType type, String name, IType... argumentTypes);
	
	public boolean isMember(IMember member);
	
	// Compilation
	
	public String getInternalName();
	
	public String getSignature();
	
	public String[] getInterfaceArray();
	
	public void write(ClassWriter writer);
}
