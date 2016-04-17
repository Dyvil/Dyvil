package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.statement.loop.ILoop;
import dyvil.tools.parsing.Name;

public class CombiningLabelContext implements ILabelContext
{
	private ILabelContext inner;
	private ILabelContext outer;
	
	public CombiningLabelContext(ILabelContext inner, ILabelContext outer)
	{
		this.inner = inner;
		this.outer = outer;
	}
	
	@Override
	public Label resolveLabel(Name name)
	{
		final Label innerLabel = this.inner.resolveLabel(name);
		return innerLabel != null ? innerLabel : this.outer.resolveLabel(name);
	}
	
	@Override
	public ILoop getEnclosingLoop()
	{
		final ILoop innerLoop = this.inner.getEnclosingLoop();
		return innerLoop == null ? this.outer.getEnclosingLoop() : innerLoop;
	}
}
