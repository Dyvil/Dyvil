package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.generic.IGeneric;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterized;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface IClass extends IASTNode, IClassMember, IGeneric, IContext, IParameterized
{
	@Override
	public default void setTheClass(IClass iclass)
	{
	}
	
	@Override
	public default IClass getTheClass()
	{
		return this;
	}
	
	public void setUnit(IDyvilHeader unit);
	
	public IDyvilHeader getUnit();
	
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
	
	// Generics
	
	public IType resolveType(ITypeVariable typeVar, IType concrete);
	
	// Body
	
	public void setBody(IClassBody body);
	
	public IClassBody getBody();
	
	public void setMetadata(IClassMetadata metadata);
	
	public IClassMetadata getMetadata();
	
	public IMethod getFunctionalMethod();
	
	public IMethod getMethod(Name name, IParameter[] parameters, int parameterCount);
	
	public IMethod getSuperMethod(Name name, IParameter[] parameters, int parameterCount);
	
	public boolean isMember(IClassMember member);
	
	// Other Compilables (Lambda Expressions, ...)
	
	public int compilableCount();
	
	public void addCompilable(IClassCompilable compilable);
	
	public IClassCompilable getCompilable(int index);
	
	// Compilation
	
	public String getInternalName();
	
	public String getSignature();
	
	public String[] getInterfaceArray();
	
	@Override
	public default boolean hasSeparateFile()
	{
		return true;
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException;
	
	public void writeInnerClassInfo(ClassWriter writer);
	
	public static IClassMetadata getClassMetadata(IClass iclass, int modifiers)
	{
		
		if ((modifiers & Modifiers.OBJECT_CLASS) != 0)
		{
			return new ObjectClassMetadata(iclass);
		}
		if ((modifiers & Modifiers.CASE_CLASS) != 0)
		{
			return new CaseClassMetadata(iclass);
		}
		if ((modifiers & Modifiers.ANNOTATION) != 0)
		{
			return new AnnotationMetadata(iclass);
		}
		if ((modifiers & Modifiers.INTERFACE_CLASS) != 0)
		{
			return new InterfaceMetadata();
		}
		// TODO Enums
		return new ClassMetadata(iclass);
	}
}
