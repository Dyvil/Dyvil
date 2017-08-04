package dyvil.tools.compiler.ast.expression.operator;

import dyvil.lang.Name;

public interface IOperatorMap
{
	IOperator resolveOperator(Name name, byte type);
	
	void addOperator(IOperator operator);
}
