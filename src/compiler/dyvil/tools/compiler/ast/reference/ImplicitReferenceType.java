package dyvil.tools.compiler.ast.reference;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.parsing.marker.MarkerList;

public class ImplicitReferenceType extends ReferenceType
{
	public ImplicitReferenceType(IType type)
	{
		super(type);
	}

	public ImplicitReferenceType(IClass iclass, IType type)
	{
		super(iclass, type);
	}

	@Override
	public IValue convertValue(IValue value, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IType valueType = value.getType();
		if (super.isSuperTypeOf(valueType))
		{
			return value;
		}

		IValue typedValue = value.withType(this.type, typeContext, markers, context);
		if (typedValue == null)
		{
			return null;
		}

		if (!typedValue.getType().isSameType(this.type))
		{
			return null;
		}

		final IReference ref = value.toReference();
		if (ref != null)
		{
			return new ReferenceValue(value, ref);
		}

		markers.add(MarkerMessages.createMarker(value.getPosition(), "reference.expression.invalid"));
		return typedValue;
	}
}
