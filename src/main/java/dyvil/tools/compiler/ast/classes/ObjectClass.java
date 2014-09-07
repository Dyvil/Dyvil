package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.util.Classes;

public class ObjectClass extends AbstractClass
{
	public ObjectClass()
	{
		super(Classes.OBJECT, new ClassBody());
	}
}
