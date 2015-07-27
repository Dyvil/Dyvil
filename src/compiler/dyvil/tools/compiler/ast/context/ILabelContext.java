package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.statement.ILoop;
import dyvil.tools.compiler.ast.statement.Label;

public interface ILabelContext
{
	public Label resolveLabel(Name name);
	
	public ILoop getEnclosingLoop();
}
