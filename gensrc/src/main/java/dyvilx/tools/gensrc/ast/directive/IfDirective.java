package dyvilx.tools.gensrc.ast.directive;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.statement.IfStatement;

public class IfDirective extends IfStatement
{
	public IfDirective(SourcePosition position)
	{
		super(position);
	}

	public IfDirective(IValue condition, IValue then, IValue elseThen)
	{
		super(condition, then, elseThen);
	}

	@Override
	public void toString(String indent, StringBuilder buffer)
	{
		buffer.append("#if (");
		this.condition.toString(indent, buffer);
		buffer.append(") ");
		this.then.toString(indent, buffer); // definitely a statement list

		if (this.elseThen != null)
		{
			buffer.append('\n').append(indent).append("#else ");
			this.elseThen.toString(indent, buffer);
		}
	}
}
