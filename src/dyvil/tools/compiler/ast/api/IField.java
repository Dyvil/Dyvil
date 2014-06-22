package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.ast.field.Value;

public interface IField
{
	public void setValue(Value value);
	
	public Value getValue();
}
