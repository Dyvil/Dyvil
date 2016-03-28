package dyvil.tools.compiler.ast.operator;

import dyvil.tools.parsing.Name;

public interface IOperatorMap
{
	Operator resolveOperator(Name name);
	
	void addOperator(Operator operator);
}
