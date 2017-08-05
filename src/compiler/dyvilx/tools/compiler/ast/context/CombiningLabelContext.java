package dyvilx.tools.compiler.ast.context;

import dyvilx.tools.compiler.ast.statement.control.Label;
import dyvil.lang.Name;

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
	public Label getBreakLabel()
	{
		final Label inner = this.inner.getBreakLabel();
		return inner != null ? inner : this.outer.getBreakLabel();
	}

	@Override
	public Label getContinueLabel()
	{
		final Label inner = this.inner.getContinueLabel();
		return inner != null ? inner : this.outer.getContinueLabel();
	}
}
