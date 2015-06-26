package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.member.Name;

public interface IOperatorMap
{
	public Operator getOperator(Name name);
	
	public void addOperator(Operator operator);
}
