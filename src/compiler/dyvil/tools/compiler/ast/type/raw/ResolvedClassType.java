package dyvil.tools.compiler.ast.type.raw;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ResolvedClassType extends ClassType
{
	protected ICodePosition position;

	public ResolvedClassType()
	{
	}

	public ResolvedClassType(IClass theClass)
	{
		this.theClass = theClass;
	}

	public ResolvedClassType(ICodePosition position)
	{
		this.position = position;
	}

	public ResolvedClassType(IClass theClass, ICodePosition position)
	{
		this.theClass = theClass;
		this.position = position;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		final IClass iclass = this.theClass;
		if (iclass != null)
		{
			ModifierUtil.checkVisibility(iclass, this.position, markers, context);
		}

		super.checkType(markers, context, position);
	}
}
