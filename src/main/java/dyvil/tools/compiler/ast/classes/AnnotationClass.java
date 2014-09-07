package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.util.Classes;

public class AnnotationClass extends AbstractClass
{
	public AnnotationClass()
	{
		super(Classes.ANNOTATION, new ClassBody());
	}
}
