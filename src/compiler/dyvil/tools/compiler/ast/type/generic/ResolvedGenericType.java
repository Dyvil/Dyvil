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
	public void checkType(MarkerList markers, IContext context, int position)
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
				final Marker marker = Markers.semantic(type.getPosition(), "type.generic.incompatible",
				                                       typeVariable.getName().qualified, this.theClass.getFullName());
				marker.addInfo(Markers.getSemantic("type.generic", type));
				marker.addInfo(Markers.getSemantic("type_parameter.declaration", typeVariable));
				markers.add(marker);
			}
		}

		if ((position & TypePosition.GENERIC_FLAG) == 0)
		{
			markers.add(Markers.semanticError(this.position, "type.generic.class"));
		}

		super.checkType(markers, context, position);
	}
}
