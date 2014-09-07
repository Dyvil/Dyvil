package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.util.Classes;

public class DefaultClass extends AbstractClass
{
	public DefaultClass()
	{
		super(Classes.CLASS, new ClassBody());
	}
}
