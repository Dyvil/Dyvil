package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.structure.IContext;

public interface IVariableList extends IContext
{
	public void addVariable(Variable variable);
}
