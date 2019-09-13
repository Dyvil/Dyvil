package dyvilx.tools.compiler.ast.context;

import dyvilx.tools.compiler.ast.statement.control.Label;
import dyvil.lang.Name;

public interface ILabelContext
{
	Label resolveLabel(Name name);

	Label getBreakLabel();

	Label getContinueLabel();
}
