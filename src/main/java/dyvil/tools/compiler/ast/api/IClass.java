package dyvil.tools.compiler.ast.api;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface IClass extends IASTNode, IAnnotatable, IModified, INamed, IGeneric, IContext
{
	public CompilationUnit getUnit();
	
	public Package getPackage();
	
	public IType toType();
	
	public boolean isAbstract();
	
	// Super Types
	
	public void setSuperType(IType type);
	
	public IType getSuperType();
	
	public boolean isSuperType(IType t);
	
	// Interfaces
	
	public void setInterfaces(List<IType> interfaces);
	
	public List<IType> getInterfaces();
	
	public void addInterface(IType type);
	
	// Body
	
	public void setBody(ClassBody body);
	
	public ClassBody getBody();
	
	public void getMethodMatches(List<MethodMatch> matches, IType type, String name, IType... argumentTypes);
	
	public boolean isMember(IMember member);
	
	// States
	
	public void resolveTypes(List<Marker> markers, IContext context);
	
	public void resolve(List<Marker> markers, IContext context);
	
	public void check(List<Marker> markers);
	
	public void foldConstants();
	
	// Compilation
	
	public String getInternalName();
	
	public String getSignature();
	
	public String[] getInterfaceArray();
	
	public void write(ClassWriter writer);
}
