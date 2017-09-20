package dyvilx.tools.compiler.ast.expression.optional;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class OptionalChainOperator extends OptionalUnwrapOperator implements IValue
{
	// Metadata
	protected Label elseLabel;

	public OptionalChainOperator(IValue receiver)
	{
		super(receiver);
		this.force = true;
	}

	@Override
	public int valueTag()
	{
		return OPTIONAL_CHAIN;
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		markers.add(Markers.semanticError(this.getPosition(), "optional.chain.invalid"));

		super.check(markers, context);
	}

	@Override
	protected String getTypeError()
	{
		return "optional.chain.type.incompatible";
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.receiver.toString(indent, buffer);
		buffer.append('?');
	}
}
