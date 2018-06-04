package dyvilx.tools.compiler.ast.statement.control;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class FallthroughStatement extends JumpStatement
{
	public FallthroughStatement(SourcePosition position)
	{
		super(position);
	}

	@Override
	public int valueTag()
	{
		return FALLTHROUGH;
	}

	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		this.label = context.getFallthroughLabel();
		if (this.label == null)
		{
			markers.add(Markers.semanticError(this.position, "fallthrough.invalid"));
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("continue");
		if (this.name != null)
		{
			buffer.append(' ').append(this.name);
		}
	}
}
