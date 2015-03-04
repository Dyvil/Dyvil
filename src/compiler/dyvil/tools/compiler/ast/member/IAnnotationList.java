package dyvil.tools.compiler.ast.member;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.type.IType;

public interface IAnnotationList
{
	public void setAnnotation(int index, Annotation annotation);
	
	public void addAnnotation(Annotation annotation);
	
	public void removeAnnotation(int index);
	
	public Annotation getAnnotation(int index);
	
	public Annotation getAnnotation(IType type);
}
