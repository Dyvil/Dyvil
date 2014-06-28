package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.util.Classes;

public class ModuleClass extends AbstractClass
{
	public ModuleClass()
	{
		super(Classes.MODULE, new ClassBody());
	}
}
