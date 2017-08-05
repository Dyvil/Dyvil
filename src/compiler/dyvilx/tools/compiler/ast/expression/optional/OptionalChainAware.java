package dyvilx.tools.compiler.ast.expression.optional;

import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.expression.IValue;

public interface OptionalChainAware extends IValue
{
	@Override
	boolean needsOptionalElseLabel();

	@Override
	Label getOptionalElseLabel();

	@Override
	boolean setOptionalElseLabel(Label label, boolean top);
}
