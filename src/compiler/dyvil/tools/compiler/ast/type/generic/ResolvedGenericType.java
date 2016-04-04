package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.Marker;
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
		ModifierUtil.checkVisibility(this.theClass, this.position, markers, context);

		// Check if the Type Variable Bounds accept the supplied Type Arguments
		final int count = Math.min(this.typeArgumentCount, this.theClass.typeParameterCount());
		for (int i = 0; i < count; i++)
		{
			final ITypeParameter typeVariable = this.theClass.getTypeParameter(i);
			final IType type = this.typeArguments[i];

			if (type.isResolved() && !typeVariable.isSuperTypeOf(type))
			{
				final Marker marker = Markers.semantic(type.getPosition(), "generic.type.incompatible",
				                                       typeVariable.getName().qualified, this.theClass.getFullName());
				marker.addInfo(Markers.getSemantic("generic.type", type));
				marker.addInfo(Markers.getSemantic("typeparameter.declaration", typeVariable));
				markers.add(marker);
			}
		}

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
