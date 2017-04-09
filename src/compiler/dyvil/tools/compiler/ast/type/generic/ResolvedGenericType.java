package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.TypeParameterList;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
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

	public ResolvedGenericType(ICodePosition position, IClass iclass, IType... arguments)
	{
		super(iclass, arguments);
		this.position = position;
	}

	public ResolvedGenericType(ICodePosition position, IClass iclass, TypeList arguments)
	{
		super(iclass, arguments);
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
	public IType atPosition(ICodePosition position)
	{
		this.position = position;
		return this;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		ModifierUtil.checkVisibility(this.theClass, this.position, markers, context);

		// Check if the Type Variable Bounds accept the supplied Type Arguments
		final int count = Math.min(this.arguments.size(), this.theClass.typeArity());
		final TypeParameterList classTypeParams = this.theClass.getTypeParameters();

		for (int i = 0; i < count; i++)
		{
			final ITypeParameter typeParameter = classTypeParams.get(i);
			final IType typeArgument = this.resolveType(typeParameter);

			if (typeArgument.isResolved() && !typeParameter.isAssignableFrom(typeArgument, null))
			{
				final Marker marker = Markers.semanticError(typeArgument.getPosition(), "type.generic.incompatible",
				                                            typeParameter.getName().qualified,
				                                            this.theClass.getFullName());
				marker.addInfo(Markers.getSemantic("type.generic.argument", typeArgument));
				marker.addInfo(Markers.getSemantic("type_parameter.declaration", typeParameter));
				markers.add(marker);
			}
		}

		if ((position & TypePosition.GENERIC_FLAG) == 0)
		{
			markers.add(Markers.semanticError(this.position, "type.generic.class"));
		}

		super.checkType(markers, context, position);
	}

	@Override
	protected GenericType withArguments(TypeList arguments)
	{
		return new ResolvedGenericType(this.position, this.theClass, arguments);
	}
}
