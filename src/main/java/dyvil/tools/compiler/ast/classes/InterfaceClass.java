package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.util.Classes;

public class InterfaceClass extends AbstractClass
{
	public InterfaceClass()
	{
		super(Classes.INTERFACE, new ClassBody());
	}
}
