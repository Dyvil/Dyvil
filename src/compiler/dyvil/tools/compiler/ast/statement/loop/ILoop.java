package dyvil.tools.compiler.ast.statement.loop;

import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.parsing.Name;

public interface ILoop extends ILabelContext
{
	Label getContinueLabel();
	
	Label getBreakLabel();
	
	@Override
	default ILoop getEnclosingLoop()
	{
		return this;
	}
	
	@Override
	Label resolveLabel(Name name);
}
