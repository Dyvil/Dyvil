package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.structure.IContext;

public class PrimitiveType extends Type
{
	public PrimitiveType(String name)
	{
		super(name);
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public Type resolve(IContext context)
	{
		return this;
	}
	
	@Override
	public Type applyState(CompilerState state, IContext context)
	{
		return this;
	}
}
