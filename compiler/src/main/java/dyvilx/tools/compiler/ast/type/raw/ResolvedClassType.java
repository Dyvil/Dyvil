package dyvilx.tools.compiler.ast.type.raw;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.check.ModifierChecks;
import dyvilx.tools.parsing.marker.MarkerList;

public class ResolvedClassType extends ClassType
{
	protected SourcePosition position;

	public ResolvedClassType()
	{
	}

	public ResolvedClassType(IClass theClass)
	{
		this.theClass = theClass;
	}

	public ResolvedClassType(SourcePosition position)
	{
		this.position = position;
	}

	public ResolvedClassType(IClass theClass, SourcePosition position)
	{
		this.theClass = theClass;
		this.position = position;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		final IClass iclass = this.theClass;
		if (iclass != null)
		{
			ModifierChecks.checkVisibility(iclass, this.position, markers, context);
		}

		super.checkType(markers, context, position);
	}
}
