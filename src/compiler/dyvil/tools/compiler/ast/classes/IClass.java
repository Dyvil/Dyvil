package dyvil.tools.compiler.ast.classes;

import java.util.List;

import org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.IGeneric;
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
	
	public boolean equals(IClass iclass);
	
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
	
	public void setInterfaces(List<IType> interfaces);
	
	public List<IType> getInterfaces();
	
	public void addInterface(IType type);
	
	// Body
	
	public void setBody(IClassBody body);
	
	public IClassBody getBody();
	
	public IField getInstanceField();
	
	public IMethod getFunctionalMethod();
	
	public boolean isMember(IMember member);
	
	// Compilation
	
	public String getInternalName();
	
	public String getSignature();
	
	public String[] getInterfaceArray();
	
	public void write(ClassWriter writer);
	
	public void writeInnerClassInfo(ClassWriter writer);
}
