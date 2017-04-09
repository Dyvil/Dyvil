package dyvil.tools.compiler.ast.expression.optional;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.expression.IValue;

public interface OptionalChainAware extends IValue
{
	@Override
	boolean needsOptionalElseLabel();

	@Override
	Label getOptionalElseLabel();

	@Override
	boolean setOptionalElseLabel(Label label, boolean top);
}
