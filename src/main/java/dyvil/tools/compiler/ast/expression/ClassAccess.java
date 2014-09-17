package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ClassAccess extends ASTObject implements IValue
{
	public IClass iclass;
	
	public ClassAccess(ICodePosition position, IClass iclass)
	{
		this.position = position;
		this.iclass = iclass;
	}
	
	@Override
	public boolean isConstant()
	{
		return true;
	}
	
	@Override
	public Type getType()
	{
		return null;
	}
	
	@Override
	public ClassAccess applyState(CompilerState state)
	{
		return this;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		// TODO
	}
}
