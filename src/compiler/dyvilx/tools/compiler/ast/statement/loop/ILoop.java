package dyvilx.tools.compiler.ast.statement.loop;

import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.statement.control.Label;
import dyvil.lang.Name;

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
