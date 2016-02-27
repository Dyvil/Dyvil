package dyvil.tools.compiler.ast.generic.type;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ResolvedTypeVarType extends TypeVarType
{
	protected ICodePosition position;

	public ResolvedTypeVarType(ICodePosition position)
	{
		this.position = position;
	}

	public ResolvedTypeVarType(ITypeParameter typeParameter, ICodePosition position)
	{
		super(typeParameter);
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
		switch (position)
		{
		case CLASS:
			if (this.typeParameter.getReifiedKind() != ITypeParameter.ReifiedKind.NOT_REIFIED)
			{
				return;
			}
		case TYPE:
			if (this.typeParameter.getReifiedKind() == ITypeParameter.ReifiedKind.REIFIED_TYPE)
			{
				return;
			}
			markers.add(Markers.semantic(this.position, "type.class.typevar"));
			return;
		case SUPER_TYPE:
			markers.add(Markers.semantic(this.position, "type.super.typevar"));
			return;
		default:
			return;
		}
	}
}
