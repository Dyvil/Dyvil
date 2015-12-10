package dyvil.tools.compiler.ast.generic;

import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotated;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

public interface ITypeVariable extends IASTNode, INamed, IAnnotated, IObjectCompilable
{
	IGeneric getGeneric();
	
	void setIndex(int index);
	
	int getIndex();
	
	// Variance
	
	void setVariance(Variance variance);
	
	Variance getVariance();
	
	IType getDefaultType();
	
	IType getParameterType();

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

	boolean isAssignableFrom(IType type);

	boolean isSuperClassOf(IType type);

	int getSuperTypeDistance(IType superType);

	// Resolution

	IDataMember resolveField(Name name);

	void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments);

	// Phases

	void resolveTypes(MarkerList markers, IContext context);

	void resolve(MarkerList markers, IContext context);

	void checkTypes(MarkerList markers, IContext context);

	void check(MarkerList markers, IContext context);

	void foldConstants();

	void cleanup(IContext context, IClassCompilableList compilableList);

	// Compilation

	void appendSignature(StringBuilder buffer);

	void write(TypeAnnotatableVisitor visitor);
}
