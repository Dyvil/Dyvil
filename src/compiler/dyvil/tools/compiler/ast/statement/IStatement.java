package dyvil.tools.compiler.ast.statement;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.ast.expression.IValue;

public interface IStatement extends IValue
{
	@Override
	public default boolean isStatement()
	{
		return true;
	}
	
	public void setParent(IStatement parent);
	
	public IStatement getParent();
	
	public default Label resolveLabel(String name)
	{
		return null;
	}
}
