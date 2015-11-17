package dyvil.tools.compiler.ast.operator;

import dyvil.tools.parsing.Name;

public interface IOperatorMap
{
	Operator getOperator(Name name);
	
	void addOperator(Operator operator);
}
