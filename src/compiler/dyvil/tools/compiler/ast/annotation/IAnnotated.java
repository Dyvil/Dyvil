package dyvil.tools.compiler.ast.annotation;

import java.lang.annotation.ElementType;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.IAnnotationConsumer;

public interface IAnnotated extends IAnnotationConsumer
{
	public AnnotationList getAnnotations();
	
	public void setAnnotations(AnnotationList annotations);
	
	public void addAnnotation(IAnnotation annotation);
	
	public default boolean addRawAnnotation(String type, IAnnotation annotation)
	{
		return true;
	}
	
	public IAnnotation getAnnotation(IClass type);
	
	public ElementType getElementType();
	
	@Override
	public default void setAnnotation(IAnnotation annotation)
	{
		this.addAnnotation(annotation);
	}
}
