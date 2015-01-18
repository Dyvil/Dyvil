package dyvil.tools.compiler.ast.member;

import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.type.IType;

public interface IAnnotated
{
	public void setAnnotations(List<Annotation> annotations);
	
	public List<Annotation> getAnnotations();
	
	public Annotation getAnnotation(IType type);
	
	public void addAnnotation(Annotation annotation);
}
