package dyvil.tools.compiler.ast.generic;

import dyvil.annotation.Reified;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotated;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

public interface ITypeParameter extends IASTNode, INamed, IAnnotated, IObjectCompilable
{
	ITypeParametric getGeneric();
	
	void setIndex(int index);
	
	int getIndex();
	
	// Variance
	
	void setVariance(Variance variance);
	
	Variance getVariance();

	Reified.Type getReifiedKind();
	
	int getParameterIndex();

	default boolean isAny()
	{
		return this.getGeneric() instanceof ITypeAlias;
	}

	IType getErasure();

	IType getDefaultType();

	IType getCovariantType();

	// Upper Bounds

	int upperBoundCount();

	void setUpperBound(int index, IType bound);

	void addUpperBound(IType bound);

	IType getUpperBound(int index);

	IType[] getUpperBounds();

	void addBoundAnnotation(IAnnotation annotation, int index, TypePath typePath);

	// Lower Bounds

	void setLowerBound(IType bound);

	IType getLowerBound();

	// Super Types

	IClass getTheClass();

	boolean isAssignableFrom(IType type, ITypeContext typeContext);

	boolean isSameType(IType type);

	boolean isSameClass(IType type);

	boolean isSuperTypeOf(IType subType);

	boolean isSuperClassOf(IType subType);

	boolean isSubTypeOf(IType superType);

	boolean isSubClassOf(IType superType);

	// Resolution

	IDataMember resolveField(Name name);

	void getMethodMatches(MatchList<IMethod> list, IValue instance, Name name, IArguments arguments);

	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);

	// Phases

	void resolveTypes(MarkerList markers, IContext context);

	void resolve(MarkerList markers, IContext context);

	void checkTypes(MarkerList markers, IContext context);

	void check(MarkerList markers, IContext context);

	void foldConstants();

	void cleanup(IContext context, IClassCompilableList compilableList);

	// Compilation

	void appendSignature(StringBuilder buffer);

	void appendParameterDescriptor(StringBuilder buffer);

	void appendParameterSignature(StringBuilder buffer);

	void writeParameter(MethodWriter writer) throws BytecodeException;

	void writeArgument(MethodWriter writer, IType type) throws BytecodeException;

	void write(TypeAnnotatableVisitor visitor);
}
