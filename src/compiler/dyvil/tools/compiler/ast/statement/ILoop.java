package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.member.Name;

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
