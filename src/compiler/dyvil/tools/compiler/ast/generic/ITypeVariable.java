package dyvil.tools.compiler.ast.generic;

import java.util.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface ITypeVariable extends INamed
{
	public IClass getCaptureClass();
	
	public void setUpperBounds(List<IType> bounds);
	
	public List<IType> getUpperBounds();
	
	public void addUpperBound(IType bound);
	
	public void setLowerBound(IType bound);
	
	public IType getLowerBound();
	
	public void resolveTypes(List<Marker> markers, IContext context);
}
