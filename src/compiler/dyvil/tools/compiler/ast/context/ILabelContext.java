package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.statement.ILoop;
import dyvil.tools.compiler.ast.statement.Label;
import dyvil.tools.parsing.Name;

public interface ILabelContext
{
	public Label resolveLabel(Name name);
	
	public ILoop getEnclosingLoop();
}
