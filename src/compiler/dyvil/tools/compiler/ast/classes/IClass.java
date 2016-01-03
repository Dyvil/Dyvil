package dyvil.tools.compiler.ast.classes;

import dyvil.collection.Set;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationMetadata;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.ITypeParameterized;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterized;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public interface IClass extends IClassMember, ITypeParameterized, IContext, IParameterized, IClassCompilableList
{
	@Override
	default void setTheClass(IClass iclass)
	{
	}
	
	@Override
	default IClass getTheClass()
	{
		return this;
	}
	
	void setHeader(IDyvilHeader unit);
	
	@Override
	IDyvilHeader getHeader();
	
	void setOuterClass(IClass iclass);
	
	IClass getOuterClass();
	
	// Modifiers
	
	boolean isAbstract();
	
	boolean isInterface();

	boolean isAnnotation();
	
	boolean isObject();
	
	// Full Name
	
	void setFullName(String name);
	
	String getFullName();
	
	// Super Types
	
	@Override
	IType getType();
	
	IType getClassType();
	
	void setSuperType(IType type);
	
	IType getSuperType();
	
	boolean isSubTypeOf(IType type);
	
	int getSuperTypeDistance(IType superType);

	default IArguments getSuperConstructorArguments()
	{
		return null;
	}

	default void setSuperConstructorArguments(IArguments arguments)
	{
	}
	
	// Interfaces
	
	int interfaceCount();
	
	void setInterface(int index, IType type);
	
	void addInterface(IType type);
	
	IType getInterface(int index);
	
	// Generics
	
	IType resolveType(ITypeParameter typeVar, IType concrete);
	
	// Body
	
	void setBody(IClassBody body);
	
	IClassBody getBody();
	
	void setMetadata(IClassMetadata metadata);
	
	IClassMetadata getMetadata();
	
	IMethod getFunctionalMethod();
	
	IMethod getMethod(Name name, IParameter[] parameters, int parameterCount, IType concrete);
	
	IDataMember getSuperField(Name name);
	
	boolean isMember(IClassMember member);
	
	byte getVisibility(IClassMember member);
	
	boolean checkImplements(MarkerList markers, IClass iclass, IMethod candidate, ITypeContext typeContext);
	
	void checkMethods(MarkerList markers, IClass iclass, ITypeContext typeContext, Set<IClass> checkedClasses);
	
	// Other Compilables (Lambda Expressions, ...)
	
	@Override
	int compilableCount();
	
	@Override
	void addCompilable(IClassCompilable compilable);
	
	@Override
	IClassCompilable getCompilable(int index);
	
	// Compilation
	
	String getInternalName();
	
	String getSignature();
	
	String[] getInterfaceArray();
	
	@Override
	default boolean hasSeparateFile()
	{
		return true;
	}
	
	@Override
	void write(ClassWriter writer) throws BytecodeException;
	
	void writeInnerClassInfo(ClassWriter writer);
	
	static IClassMetadata getClassMetadata(IClass iclass, int modifiers)
	{
		if ((modifiers & Modifiers.OBJECT_CLASS) != 0)
		{
			return new ObjectClassMetadata(iclass);
		}
		if ((modifiers & Modifiers.CASE_CLASS) != 0)
		{
			return new CaseClassMetadata(iclass);
		}
		if ((modifiers & Modifiers.ANNOTATION) == Modifiers.ANNOTATION)
		{
			return new AnnotationMetadata(iclass);
		}
		final int interfaceModifiers = modifiers & Modifiers.TRAIT_CLASS;
		if (interfaceModifiers == Modifiers.TRAIT_CLASS)
		{
			return new TraitMetadata(iclass);
		}
		else if (interfaceModifiers == Modifiers.INTERFACE_CLASS)
		{
			return new InterfaceMetadata(iclass);
		}
		return new ClassMetadata(iclass);
	}
}
