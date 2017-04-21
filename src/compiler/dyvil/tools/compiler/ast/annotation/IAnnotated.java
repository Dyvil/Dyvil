package dyvil.tools.compiler.ast.annotation;

import dyvil.tools.compiler.ast.classes.IClass;

import java.lang.annotation.ElementType;

public interface IAnnotated
{
	ElementType getElementType();

	AnnotationList getAnnotations();

	void setAnnotations(AnnotationList annotations);

	IAnnotation getAnnotation(IClass type);

	default boolean addRawAnnotation(String type, IAnnotation annotation)
	{
		return true;
	}
}
