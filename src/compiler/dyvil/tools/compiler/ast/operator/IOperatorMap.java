package dyvil.tools.compiler.ast.operator;

import dyvil.tools.parsing.Name;

public interface IOperatorMap
{
	IOperator resolveOperator(Name name, byte type);
	
	void addOperator(IOperator operator);
}
