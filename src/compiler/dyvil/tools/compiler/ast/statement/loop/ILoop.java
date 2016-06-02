package dyvil.tools.compiler.ast.statement.loop;

import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.parsing.Name;

public interface ILoop extends ILabelContext
{
	IValue getAction();

	void setAction(IValue action);

	@Override
	Label getBreakLabel();

	@Override
	Label getContinueLabel();

	@Override
	Label resolveLabel(Name name);
}
