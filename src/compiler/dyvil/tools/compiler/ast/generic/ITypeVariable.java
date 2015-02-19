package dyvil.tools.compiler.ast.generic;

import java.util.List;

import dyvil.tools.compiler.ast.classes.CaptureClass;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface ITypeVariable extends INamed
{
	public CaptureClass getCaptureClass();
	
	// Bounds
	
	public void setUpperBounds(List<IType> bounds);
	
	public List<IType> getUpperBounds();
	
	public void addUpperBound(IType bound);
	
	public void setLowerBound(IType bound);
	
	public IType getLowerBound();
	
	// Super Types
	
	public boolean isSuperTypeOf(IType type);
	
	// Resolve Types
	
	public void resolveTypes(List<Marker> markers, IContext context);
	
	// Compilation
	
	public void appendSignature(StringBuilder buffer);
}
