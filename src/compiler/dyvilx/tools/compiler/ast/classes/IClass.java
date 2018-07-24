package dyvilx.tools.compiler.ast.classes;

import dyvil.collection.Collection;
import dyvil.collection.Set;
import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.metadata.*;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.ITypeParametricMember;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.header.ClassCompilable;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilable;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.member.ClassMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParametric;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public interface IClass
	extends ClassMember, IParametric, ITypeParametricMember, ICompilable, IContext, IClassCompilableList
{
	@Override
	MemberKind getKind();

	@Override
	IHeaderUnit getHeader();

	void setHeader(IHeaderUnit unit);

	@Override
	IClass getEnclosingClass();

	@Override
	void setEnclosingClass(IClass enclosingClass);

	// ------------------------------ Attributable Implementation ------------------------------

	@Override
	default int getJavaFlags()
	{
		return ITypeParametricMember.super.getJavaFlags() | (!this.isInterface() ? ClassFormat.ACC_SUPER : 0);
	}

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

	default AttributeList getConstructorAttributes()
	{
		return null;
	}

	default void setConstructorAttributes(AttributeList attributes)
	{
	}

	// Interfaces

	TypeList getInterfaces();

	// Generics

	@Override
	TypeParameterList getTypeParameters();

	void setTypeParameters(TypeParameterList typeParameters);

	IType resolveType(ITypeParameter typeVar, IType concrete);

	// Body

	ClassBody createBody();

	ClassBody getBody();

	void setBody(ClassBody body);

	IClassMetadata getMetadata();

	void setMetadata(IClassMetadata metadata);

	IMethod getFunctionalMethod();

	boolean isMember(ClassMember member);

	byte getVisibility(ClassMember member);

	void addMethods(Collection<IMethod> methods);

	boolean checkImplements(IMethod candidate, ITypeContext typeContext);

	void checkMethods(MarkerList markers, IClass checkedClass, ITypeContext typeContext, Set<IClass> checkedClasses);

	// Other Compilables (Lambda Expressions, ...)

	@Override
	int classCompilableCount();

	@Override
	void addClassCompilable(ClassCompilable compilable);

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

	static IClassMetadata getClassMetadata(IClass forClass, long modifiers)
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
		if ((modifiers & Modifiers.EXTENSION_CLASS) != 0)
		{
			return new ExtensionMetadata(forClass);
		}
		// All modifiers above are single-bit flags
		if ((modifiers & Modifiers.CASE_CLASS) != 0)
		{
			return new CaseClassMetadata(forClass);
		}
		return new ClassMetadata(forClass);
	}
}
