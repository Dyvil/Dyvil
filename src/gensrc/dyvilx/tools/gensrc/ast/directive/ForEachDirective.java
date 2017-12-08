package dyvilx.tools.gensrc.ast.directive;

import dyvil.annotation.internal.NonNull;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.statement.loop.ForEachStatement;

public class ForEachDirective extends ForEachStatement
{
	public ForEachDirective(SourcePosition position, IVariable var)
	{
		super(position, var);
	}

	public ForEachDirective(SourcePosition position, IVariable var, IValue action)
	{
		super(position, var, action);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append('#'); // lazy but ok
		super.toString(indent, buffer);
	}
}
