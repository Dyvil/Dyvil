package dyvil.tools.compiler.ast.classes;

import dyvil.collection.Collection;
import dyvil.collection.Set;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationMetadata;
import dyvil.tools.compiler.ast.classes.metadata.*;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.ITypeParametricMember;
import dyvil.tools.compiler.ast.header.IClassCompilable;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilable;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IParametric;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.lang.Name;
import dyvil.tools.parsing.marker.MarkerList;

public interface IClass
	extends IClassMember, IParametric, ITypeParametricMember, ICompilable, IContext, IClassCompilableList
{
	@Override
	default MemberKind getKind()
	{
		return MemberKind.CLASS;
	}

	@Override
	IHeaderUnit getHeader();

	void setHeader(IHeaderUnit unit);

	@Override
	IClass getEnclosingClass();

	@Override
	void setEnclosingClass(IClass enclosingClass);

	// Modifiers

	default boolean isAnonymous()
	{
		return false;
	}

	// Full Name

	@Override
	String getFullName();

	void setFullName(String name);

	// Super Types

	@Override
	@Deprecated
	@dyvil.annotation.Deprecated(replacements = { "getThisType", "getReceiverType", "getClassType" })
	default IType getType()
	{
		return this.getThisType();
	}

	@Override
	@Deprecated
	default void setType(IType type)
	{
	}

	default IType getReceiverType()
	{
		return this.getThisType().asParameterType();
	}

	IType getClassType();

	void setSuperType(IType type);

	IType getSuperType();

	boolean isSubClassOf(IType type);

	default ArgumentList getSuperConstructorArguments()
	{
		return null;
	}

	default void setSuperConstructorArguments(ArgumentList arguments)
	{
	}

	// Interfaces

	TypeList getInterfaces();

	// Generics

	IType resolveType(ITypeParameter typeVar, IType concrete);

	// Body

	ClassBody getBody();

	void setBody(ClassBody body);

	IClassMetadata getMetadata();

	void setMetadata(IClassMetadata metadata);

	IMethod getFunctionalMethod();

	boolean isMember(IClassMember member);

	byte getVisibility(IClassMember member);

	Collection<IMethod> getMethods(Name name);

	boolean checkImplements(IMethod candidate, ITypeContext typeContext);

	void checkMethods(MarkerList markers, IClass checkedClass, ITypeContext typeContext, Set<IClass> checkedClasses);

	// Other Compilables (Lambda Expressions, ...)

	@Override
	int classCompilableCount();

	@Override
	void addClassCompilable(IClassCompilable compilable);

	// Compilation

	@Override
	String getInternalName();

	String getSignature();

	String[] getInterfaceArray();

	@Override
	void write(ClassWriter writer) throws BytecodeException;

	@Override
	void writeClassInit(MethodWriter writer) throws BytecodeException;

	@Override
	void writeStaticInit(MethodWriter writer) throws BytecodeException;

	void writeInnerClassInfo(ClassWriter writer);

	static IClassMetadata getClassMetadata(IClass forClass, int modifiers)
	{
		if ((modifiers & Modifiers.ANNOTATION) != 0)
		{
			return new AnnotationMetadata(forClass);
		}
		if ((modifiers & Modifiers.TRAIT) != 0)
		{
			return new TraitMetadata(forClass);
		}
		if ((modifiers & Modifiers.INTERFACE) != 0)
		{
			return new InterfaceMetadata(forClass);
		}
		if ((modifiers & Modifiers.ENUM) != 0)
		{
			return new EnumClassMetadata(forClass);
		}
		if ((modifiers & Modifiers.OBJECT) != 0)
		{
			return new ObjectClassMetadata(forClass);
		}
		// All modifiers above are single-bit flags
		if ((modifiers & Modifiers.CASE_CLASS) != 0)
		{
			return new CaseClassMetadata(forClass);
		}
		return new ClassMetadata(forClass);
	}
}
