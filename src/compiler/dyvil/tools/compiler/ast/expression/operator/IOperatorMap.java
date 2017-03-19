package dyvil.tools.compiler.ast.expression.operator;

import dyvil.tools.parsing.Name;

public interface IOperatorMap
{
	IOperator resolveOperator(Name name, byte type);
	
	void addOperator(IOperator operator);
}
