package dyvilx.tools.compiler.ast.type.generic;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.check.ModifierChecks;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public class ResolvedGenericType extends ClassGenericType
{
	protected SourcePosition position;

	public ResolvedGenericType(SourcePosition position)
	{
		super();
		this.position = position;
	}

	public ResolvedGenericType(SourcePosition position, IClass iclass)
	{
		super(iclass);
		this.position = position;
	}

	public ResolvedGenericType(SourcePosition position, IClass iclass, IType... arguments)
	{
		super(iclass, arguments);
		this.position = position;
	}

	public ResolvedGenericType(SourcePosition position, IClass iclass, TypeList arguments)
	{
		super(iclass, arguments);
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
	public IType atPosition(SourcePosition position)
	{
		this.position = position;
		return this;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		ModifierChecks.checkVisibility(this.theClass, this.position, markers, context);

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
