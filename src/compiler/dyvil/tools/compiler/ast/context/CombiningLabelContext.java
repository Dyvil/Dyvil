package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.statement.loop.ILoop;
import dyvil.tools.parsing.Name;

public class CombiningLabelContext implements ILabelContext
{
	private ILabelContext	context1;
	private ILabelContext	context2;
	
	public CombiningLabelContext(ILabelContext context1, ILabelContext context2)
	{
		this.context1 = context1;
		this.context2 = context2;
	}
	
	@Override
	public Label resolveLabel(Name name)
	{
		Label label = this.context1.resolveLabel(name);
		return label == null ? this.context2.resolveLabel(name) : label;
	}
	
	@Override
	public ILoop getEnclosingLoop()
	{
		ILoop loop = this.context1.getEnclosingLoop();
		return loop == null ? this.context2.getEnclosingLoop() : loop;
	}
}
