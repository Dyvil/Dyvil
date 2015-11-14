package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.statement.loop.ILoop;
import dyvil.tools.parsing.Name;

public interface ILabelContext
{
	public Label resolveLabel(Name name);
	
	public ILoop getEnclosingLoop();
}
