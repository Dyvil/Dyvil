package dyvilx.tools.gensrc.ast.directive;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;

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

	public void setBlock(IValue block)
	{
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append('#'); // lazy but ok
		super.toString(indent, buffer);
	}
}
