package dyvil.tools.compiler.ast.type.typevar;

import dyvil.annotation.Reified;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class ResolvedTypeVarType extends TypeVarType
{
	protected SourcePosition position;

	public ResolvedTypeVarType(SourcePosition position)
	{
		this.position = position;
	}

	public ResolvedTypeVarType(ITypeParameter typeParameter, SourcePosition position)
	{
		super(typeParameter);
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
		super.checkType(markers, context, position);
		final Reified.Type reifiedKind = this.typeParameter.getReifiedKind();

		switch (position)
		{
		// the constants in this switch are the ones that only conditionally allow type var types
		case TypePosition.CLASS:
		case TypePosition.TYPE:
			if (reifiedKind != null)
			{
				return;
			}
			// Fallthrough
			markers.add(Markers.semanticError(this.position, "type.var.class", this.typeParameter.getName()));
			return;
		case TypePosition.SUPER_TYPE:
			markers.add(Markers.semanticError(this.position, "type.var.super", this.typeParameter.getName()));
		}

		final Variance variance = this.typeParameter.getVariance();
		if ((position & TypePosition.NO_COVARIANT_FLAG) != 0 && variance == Variance.COVARIANT)
		{
			markers.add(Markers.semanticError(this.position, "type.var.covariant", this.typeParameter.getName()));
		}
		if ((position & TypePosition.NO_CONTRAVARIANT_FLAG) != 0 && variance == Variance.CONTRAVARIANT)
		{
			markers.add(Markers.semanticError(this.position, "type.var.contravariant", this.typeParameter.getName()));
		}
	}
}
