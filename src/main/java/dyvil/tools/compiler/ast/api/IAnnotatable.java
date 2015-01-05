package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;

public interface IAnnotatable
{
	public void setAnnotations(List<Annotation> annotations);
	
	public List<Annotation> getAnnotations();
	
	public Annotation getAnnotation(IType type);
	
	public void addAnnotation(Annotation annotation);
}
