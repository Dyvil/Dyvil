package dyvil.tools.compiler.ast.member;

import java.util.LinkedList;
import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;

public abstract class Annotatable
{
	private List<Annotation>	annotations	= new LinkedList();
	
	public boolean addAnnotation(Annotation annotation)
	{
		return this.annotations.add(annotation);
	}

	public List<Annotation> getAnnotations()
	{
		return this.annotations;
	}
}
