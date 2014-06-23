package dyvil.tools.compiler.ast.classes;

public class AnnotationClass extends AbstractClass
{
	public AnnotationClass()
	{
		super(ANNOTATION, new ClassBody());
	}
}
