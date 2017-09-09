package dyvilx.tools.compiler.ast.expression.optional;

import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.compound.NullableType;

public interface OptionalChainAware
{
	IValue getReceiver();

	IType getType();

	void setType(IType type);

	default boolean needsOptionalElseLabel()
	{
		final IValue receiver = this.getReceiver();
		return receiver instanceof OptionalChainAware && ((OptionalChainAware) receiver).needsOptionalElseLabel();
	}

	default Label getOptionalElseLabel()
	{
		final IValue receiver = this.getReceiver();
		return receiver instanceof OptionalChainAware ? ((OptionalChainAware) receiver).getOptionalElseLabel() : null;
	}

	default boolean setOptionalElseLabel(Label label, boolean top)
	{
		final IValue receiver = this.getReceiver();
		if (!(receiver instanceof OptionalChainAware) || !((OptionalChainAware) receiver).setOptionalElseLabel(label, false))
		{
			return false;
		}

		if (!top)
		{
			this.setType(NullableType.apply(this.getType()));
		}
		return true;
	}
}
