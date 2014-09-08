package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class ClassAccess implements IValue
{
	public IClass iclass;
	
	public ClassAccess(IClass iclass)
	{
		this.iclass = iclass;
	}
	
	@Override
	public IValue fold()
	{
		return this;
	}
	
	@Override
	public Type getType()
	{
		return null;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		// TODO
	}
}
