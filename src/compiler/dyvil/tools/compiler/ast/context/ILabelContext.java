package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.parsing.Name;

public interface ILabelContext
{
	Label resolveLabel(Name name);

	Label getBreakLabel();

	Label getContinueLabel();
}
