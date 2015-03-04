package dyvil.tools.compiler.ast.member;

import java.lang.annotation.ElementType;
import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface IMember extends INamed, ITyped, IModified, IAnnotationList
{
	public void setAnnotations(Annotation[] annotations, int count);
	
	public boolean processAnnotation(Annotation annotation);
	
	public ElementType getAnnotationType();
	
	public IClass getTheClass();
	
	public int getAccessLevel();
	
	public byte getAccessibility();
	
	public default IType getType(ITypeContext context)
	{
		return this.getType();
	}
	
	// States
	
	public void resolveTypes(List<Marker> markers, IContext context);
	
	public void resolve(List<Marker> markers, IContext context);
	
	public void check(List<Marker> markers, IContext context);
	
	public void foldConstants();
}
