package dyvil.tools.compiler.ast.generic;

import dyvil.tools.asm.ClassWriter;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface ITypeVariable extends IASTNode, INamed
{
	public IGeneric getGeneric();
	
	public void setIndex(int index);
	
	public int getIndex();
	
	// Variance
	
	public void setVariance(Variance variance);
	
	public Variance getVariance();
	
	// Upper Bounds
	
	public int upperBoundCount();
	
	public void setUpperBound(int index, IType bound);
	
	public void addUpperBound(IType bound);
	
	public IType getUpperBound(int index);
	
	public IType[] getUpperBounds();
	
	// Lower Bounds
	
	public void setLowerBound(IType bound);
	
	public IType getLowerBound();
	
	// Super Types
	
	public IClass getTheClass();
	
	public boolean isSuperTypeOf(IType type);
	
	// Phases
	
	public void resolveTypes(MarkerList markers, IContext context);
	
	public void resolve(MarkerList markers, IContext context);
	
	public void checkTypes(MarkerList markers, IContext context);
	
	public void check(MarkerList markers, IContext context);
	
	public void foldConstants();
	
	public void cleanup(IContext context, IClassCompilableList compilableList);
	
	// Compilation
	
	public void appendSignature(StringBuilder buffer);
	
	public void write(ClassWriter writer);
}
