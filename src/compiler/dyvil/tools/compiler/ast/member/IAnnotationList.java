package dyvil.tools.compiler.ast.member;

import java.lang.annotation.ElementType;

import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;

public interface IAnnotationList
{
	public int annotationCount();
	
	public void setAnnotations(IAnnotation[] annotations, int annotationCount);
	
	public void setAnnotation(int index, IAnnotation annotation);
	
	public void addAnnotation(IAnnotation annotation);
	
	public void removeAnnotation(int index);
	
	public IAnnotation[] getAnnotations();
	
	public IAnnotation getAnnotation(int index);
	
	public IAnnotation getAnnotation(IClass type);
	
	public default boolean addRawAnnotation(String type)
	{
		return true;
	}
	
	public ElementType getAnnotationType();
}
