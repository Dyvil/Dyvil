package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IASTObject;
import dyvil.tools.compiler.ast.type.Type;

public interface IValue extends IASTObject
{
	public boolean isConstant();
	
	public Type getType();
	
	@Override
	public IValue applyState(CompilerState state);
}
