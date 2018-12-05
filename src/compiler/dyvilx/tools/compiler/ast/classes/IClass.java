package dyvilx.tools.compiler.ast.classes;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.metadata.IClassMetadata;
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
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.Collection;
import java.util.Set;

public interface IClass
	extends ClassMember, IParametric, ITypeParametricMember, ICompilable, IContext, IClassCompilableList
{
	// =============== Properties ===============

	// --------------- Enclosing Header ---------------

	@Override
	IHeaderUnit getHeader();

	void setHeader(IHeaderUnit unit);

	// --------------- Enclosing Package ---------------

	Package getPackage();

	void setPackage(Package pack);

	// --------------- Enclosing Class ---------------

	@Override
	IClass getEnclosingClass();

	@Override
	void setEnclosingClass(IClass enclosingClass);

	// --------------- Attributes ---------------

	@Override
	MemberKind getKind();

	@Override
	default int getJavaFlags()
	{
		return ITypeParametricMember.super.getJavaFlags() | (!this.isInterface() ? ClassFormat.ACC_SUPER : 0);
	}

	default boolean isAnonymous()
	{
		return false;
	}

	// --------------- Name ---------------

	@Override
	Name getName();

	@Override
	void setName(Name name);

	// --------------- Internal Name ---------------

	@Override
	String getInternalName();

	// --------------- Full Name ---------------

	@Override
	String getFullName();

	void setFullName(String name);

	// --------------- Type Parameters ---------------

	@Override
	TypeParameterList getTypeParameters();

	void setTypeParameters(TypeParameterList typeParameters);

	// --------------- Default Constructor Attributes ---------------

	default AttributeList getConstructorAttributes()
	{
		return null;
	}

	default void setConstructorAttributes(AttributeList attributes)
	{
	}

	// --------------- Super Type ---------------

	IType getSuperType();

	void setSuperType(IType type);

	// --------------- Super Constructor Arguments ---------------

	default ArgumentList getSuperConstructorArguments()
	{
		return null;
	}

	default void setSuperConstructorArguments(ArgumentList arguments)
	{
	}

	// --------------- Interfaces ---------------

	TypeList getInterfaces();

	// --------------- Class Body ---------------

	ClassBody createBody();

	ClassBody getBody();

	void setBody(ClassBody body);

	// --------------- (end of formal properties) ---------------

	// --------------- Metadata ---------------

	IClassMetadata getMetadata();

	void setMetadata(IClassMetadata metadata);

	IMethod getFunctionalMethod();

	// --------------- Types ---------------

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

	// =============== Methods ===============

	// --------------- Subclasses ---------------

	boolean isSubClassOf(IType type);

	// --------------- Type Parameter Resolution ---------------

	IType resolveType(ITypeParameter typeVar, IType concrete);

	// --------------- Members ---------------

	Collection<IMethod> allMethods();

	boolean isMember(ClassMember member);

	byte getVisibility(ClassMember member);

	boolean checkImplements(IMethod candidate, ITypeContext typeContext);

	void checkMethods(MarkerList markers, IClass checkedClass, ITypeContext typeContext, Set<IClass> checkedClasses);

	// --------------- Compilables ---------------

	@Override
	int classCompilableCount();

	@Override
	void addClassCompilable(ClassCompilable compilable);

	// --------------- Compilation ---------------

	@Override
	void write(ClassWriter writer) throws BytecodeException;

	@Override
	void writeClassInit(MethodWriter writer) throws BytecodeException;

	@Override
	void writeStaticInit(MethodWriter writer) throws BytecodeException;

	void writeInnerClassInfo(ClassWriter writer);
}
