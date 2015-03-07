package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public interface IStatement extends IValue
{
	@Override
	public default boolean isStatement()
	{
		return true;
	}
	
	@Override
	public default IType getType()
	{
		return Type.NONE;
	}
	
	@Override
	public default IValue withType(IType type)
	{
		return this;
	}
	
	@Override
	public default boolean isType(IType type)
	{
		return type == Type.NONE || type == Type.VOID;
	}
	
	@Override
	public default int getTypeMatch(IType type)
	{
		return 0;
	}
	
	public void setParent(IStatement parent);
	
	public IStatement getParent();
	
	public default Label resolveLabel(String name)
	{
		return null;
	}
}
