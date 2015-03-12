package dyvil.tools.compiler.ast.classes;

import org.objectweb.asm.ClassWriter;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.IGeneric;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.IDyvilUnit;
import dyvil.tools.compiler.ast.type.IType;

public interface IClass extends IASTNode, IMember, IGeneric, IContext
{
	public IDyvilUnit getUnit();
	
	public void setOuterClass(IClass iclass);
	
	public IClass getOuterClass();
	
	// Modifiers
	
	public boolean isAbstract();
	
	// Full Name
	
	public void setFullName(String name);
	
	public String getFullName();
	
	// Super Types
	
	public void setSuperType(IType type);
	
	public IType getSuperType();
	
	public boolean isSubTypeOf(IType type);
	
	// Interfaces
	
	public int interfaceCount();
	
	public void setInterface(int index, IType type);
	
	public void addInterface(IType type);
	
	public IType getInterface(int index);
	
	// Body
	
	public void setBody(IClassBody body);
	
	public IClassBody getBody();
	
	public IField getInstanceField();
	
	public IMethod getFunctionalMethod();
	
	public boolean isMember(IMember member);
	
	// Other Compilables (Lambda Expressions, ...)
	
	public int compilableCount();
	
	public void addCompilable(IClassCompilable compilable);
	
	public IClassCompilable getCompilable(int index);
	
	// Compilation
	
	public String getInternalName();
	
	public String getSignature();
	
	public String[] getInterfaceArray();
	
	public void write(ClassWriter writer);
	
	public void writeInnerClassInfo(ClassWriter writer);
}
