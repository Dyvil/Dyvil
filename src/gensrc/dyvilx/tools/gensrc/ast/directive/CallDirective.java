package dyvilx.tools.gensrc.ast.directive;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.gensrc.ast.GenSrcValue;
import dyvilx.tools.parsing.marker.MarkerList;

public class CallDirective extends MethodCall
{
	public CallDirective(SourcePosition position)
	{
		super(position);
	}

	public CallDirective(SourcePosition position, Name name)
	{
		super(position, null, name);
	}

	public CallDirective(SourcePosition position, Name name, ArgumentList arguments)
	{
		super(position, null, name, arguments);
	}

	public CallDirective(SourcePosition position, IMethod method, ArgumentList arguments)
	{
		super(position, null, method, arguments);
	}

	@Override
	public int valueTag()
	{
		return GenSrcValue.CALL_DIRECTIVE;
	}

	public void setBlock(IValue block)
	{
		// TODO
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		final IValue superResolve = super.resolve(markers, context);
		if (!Types.isVoid(superResolve.getType()))
		{
			// return type of the method was not void -> wrap in a write call
			return new WriteCall(superResolve);
		}

		return this;
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append('#'); // lazy but ok
		super.toString(indent, buffer);
	}
}
