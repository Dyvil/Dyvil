package dyvil.tools.compiler.ast.operator;

import dyvil.tools.parsing.Name;

public interface IOperatorMap
{
	public Operator getOperator(Name name);
	
	public void addOperator(Operator operator);
}
