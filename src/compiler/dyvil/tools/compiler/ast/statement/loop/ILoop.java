package dyvil.tools.compiler.ast.statement.loop;

import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.parsing.Name;

public interface ILoop extends ILabelContext
{
	public Label getContinueLabel();
	
	public Label getBreakLabel();
	
	@Override
	public default ILoop getEnclosingLoop()
	{
		return this;
	}
	
	@Override
	public Label resolveLabel(Name name);
}
