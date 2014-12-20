package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.type.Type;

public interface IAnnotatable
{
	public void setAnnotations(List<Annotation> annotations);
	
	public List<Annotation> getAnnotations();
	
	public Annotation getAnnotation(Type type);
	
	public default void addAnnotation(Annotation annotation)
	{
		this.getAnnotations().add(annotation);
	}
}
