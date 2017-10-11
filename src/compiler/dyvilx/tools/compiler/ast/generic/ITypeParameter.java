package dyvilx.tools.compiler.ast.generic;

import dyvil.annotation.Reified;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.Attributable;
import dyvilx.tools.compiler.ast.annotation.IAnnotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.header.IObjectCompilable;
import dyvilx.tools.compiler.ast.member.INamed;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.phase.IResolvable;
import dyvil.lang.Name;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;

public interface ITypeParameter extends ASTNode, IResolvable, INamed, Attributable, IObjectCompilable
{
	ITypeParametric getGeneric();
	
	int getIndex();

	void setIndex(int index);

	// Variance

	Variance getVariance();

	void setVariance(Variance variance);

	Reified.Type getReifiedKind();

	IParameter getReifyParameter();

	default boolean isAny()
	{
		return this.getGeneric() instanceof ITypeAlias;
	}

	IType getErasure();

	IType getCovariantType();

	// Upper Bounds

	IType getUpperBound();

	void setUpperBound(IType bound);

	void addBoundAnnotation(IAnnotation annotation, int index, TypePath typePath);

	// Lower Bounds

	IType getLowerBound();

	void setLowerBound(IType bound);

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

	void getMethodMatches(MatchList<IMethod> list, IValue instance, Name name, ArgumentList arguments);

	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);

	// Phases

	@Override
	void resolveTypes(MarkerList markers, IContext context);

	@Override
	void resolve(MarkerList markers, IContext context);

	@Override
	void checkTypes(MarkerList markers, IContext context);

	@Override
	void check(MarkerList markers, IContext context);

	@Override
	void foldConstants();

	@Override
	void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList);

	// Compilation

	void appendSignature(StringBuilder buffer);

	void appendParameterDescriptor(StringBuilder buffer);

	void appendParameterSignature(StringBuilder buffer);

	void writeParameter(MethodWriter writer) throws BytecodeException;

	void writeArgument(MethodWriter writer, IType type) throws BytecodeException;

	void write(TypeAnnotatableVisitor visitor);
}
