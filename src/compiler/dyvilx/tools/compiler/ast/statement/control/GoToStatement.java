package dyvilx.tools.compiler.ast.statement.control;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class GoToStatement extends JumpStatement
{
	public GoToStatement(SourcePosition position)
	{
		super(position);
	}

	@Override
	public int valueTag()
	{
		return GOTO;
	}

	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		if (this.name == null)
		{
			return;
		}

		this.label = context.resolveLabel(this.name);
		if (this.label == null)
		{
			markers.add(Markers.semanticError(this.position, "resolve.label", this.name));
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.label == null)
		{
			markers.add(Markers.semanticError(this.position, "goto.invalid"));
			return this;
		}

		markers.add(Markers.semantic(this.position, "goto.warning"));
		return this;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("goto");
		if (this.name != null)
		{
			buffer.append(' ').append(this.name);
		}
	}
}
