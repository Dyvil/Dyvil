package dyvil.tools.compiler.ast.generic.type;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ResolvedGenericType extends ClassGenericType
{
	protected ICodePosition position;

	public ResolvedGenericType(ICodePosition position)
	{
		super();
		this.position = position;
	}

	public ResolvedGenericType(ICodePosition position, IClass iclass)
	{
		super(iclass);
		this.position = position;
	}

	public ResolvedGenericType(ICodePosition position, IClass iclass, IType[] typeArguments, int typeArgumentCount)
	{
		super(iclass, typeArguments, typeArgumentCount);
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
		if (position == TypePosition.CLASS)
		{
			markers.add(Markers.semanticError(this.position, "type.class.generic"));
		}

		// If the position is a SUPER_TYPE position
		if (position == TypePosition.SUPER_TYPE || position == TypePosition.SUPER_TYPE_ARGUMENT)
		{
			position = TypePosition.SUPER_TYPE_ARGUMENT;
		}
		else
		{
			// Otherwise, resolve the type arguments with a GENERIC_ARGUMENT position
			position = TypePosition.GENERIC_ARGUMENT;
		}

		super.checkType(markers, context, position);
	}
}
