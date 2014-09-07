package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;

public interface IAnnotatable
{
	public void setAnnotations(List<Annotation> annotations);
	
	public List<Annotation> getAnnotations();
	
	public default boolean addAnnotation(Annotation annotation)
	{
		return this.getAnnotations().add(annotation);
	}
}
