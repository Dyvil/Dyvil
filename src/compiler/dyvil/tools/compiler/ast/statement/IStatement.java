package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;

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
		return Types.UNKNOWN;
	}
	
	@Override
	public default IValue withType(IType type)
	{
		return this;
	}
	
	@Override
	public default boolean isType(IType type)
	{
		return type == Types.UNKNOWN || type == Types.VOID;
	}
	
	@Override
	public default int getTypeMatch(IType type)
	{
		return 0;
	}
	
	public void setParent(IStatement parent);
	
	public IStatement getParent();
	
	public default Label resolveLabel(Name name)
	{
		return null;
	}
}
