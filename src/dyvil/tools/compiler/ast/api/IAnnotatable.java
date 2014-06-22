package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;

public interface IAnnotatable
{
	public boolean addAnnotation(Annotation annotation);

	public List<Annotation> getAnnotations();
}
