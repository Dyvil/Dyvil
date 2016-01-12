package dyvil.tools.compiler.ast.statement.loop;

import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.parsing.Name;

public interface ILoop extends ILabelContext
{
	Label getContinueLabel();
	
	Label getBreakLabel();

	IValue getAction();

	void setAction(IValue action);
	
	@Override
	default ILoop getEnclosingLoop()
	{
		return this;
	}
	
	@Override
	Label resolveLabel(Name name);
}
