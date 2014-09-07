package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.util.Classes;

public class EnumClass extends AbstractClass
{
	public EnumClass()
	{
		super(Classes.ENUM, new ClassBody());
	}
}
