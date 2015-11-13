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
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

public interface ITypeVariable extends IASTNode, INamed, IAnnotated
{
	public IGeneric getGeneric();
	
	public void setIndex(int index);
	
	public int getIndex();
	
	// Variance
	
	public void setVariance(Variance variance);
	
	public Variance getVariance();
	
	public IType getDefaultType();
	
	// Upper Bounds
	
	public int upperBoundCount();
	
	public void setUpperBound(int index, IType bound);
	
	public void addUpperBound(IType bound);
	
	public IType getUpperBound(int index);
	
	public IType[] getUpperBounds();
	
	public void addBoundAnnotation(IAnnotation annotation, int index, TypePath typePath);
	
	// Lower Bounds
	
	public void setLowerBound(IType bound);
	
	public IType getLowerBound();
	
	// Super Types
	
	public IClass getTheClass();
	
	public boolean isSuperTypeOf(IType type);
	
	public int getSuperTypeDistance(IType superType);
	
	// Resolution
	
	public IDataMember resolveField(Name name);
	
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments);
	
	// Phases
	
	public void resolveTypes(MarkerList markers, IContext context);
	
	public void resolve(MarkerList markers, IContext context);
	
	public void checkTypes(MarkerList markers, IContext context);
	
	public void check(MarkerList markers, IContext context);
	
	public void foldConstants();
	
	public void cleanup(IContext context, IClassCompilableList compilableList);
	
	// Compilation
	
	public void appendSignature(StringBuilder buffer);
	
	public void write(TypeAnnotatableVisitor visitor);
}
