package dyvil.tools.compiler.transform;

import dyvil.array.ObjectArray;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class TypeChecker
{
	public interface MarkerSupplier
	{
		Marker createMarker(ICodePosition position, IType expected, IType actual);
	}

	private TypeChecker()
	{
		// no instances
	}

	public static MarkerSupplier markerSupplier(String error)
	{
		return markerSupplier(error, ObjectArray.EMPTY);
	}

	public static MarkerSupplier markerSupplier(String error, String expectedError, String actualError)
	{
		return (position, expectedType, actualType) -> typeError(position, expectedType, actualType, error,
		                                                         expectedError, actualError);
	}

	public static MarkerSupplier markerSupplier(String error, Object... args)
	{
		return (position, expected, actual) -> typeError(position, expected, actual, error, args);
	}

	public static IValue convertValue(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context, MarkerSupplier markerSupplier)
	{
		if (type.hasTypeVariables())
		{
			type = type.getConcreteType(typeContext);
		}

		final IValue typedValue = type.convertValue(value, typeContext, markers, context);
		if (typedValue != null)
		{
			return typedValue;
		}

		markers.add(markerSupplier.createMarker(value.getPosition(), type, value.getType()));
		return value;
	}

	public static Marker typeError(IValue value, IType type, ITypeContext typeContext, String key, Object... args)
	{
		return typeError(value.getPosition(), type.getConcreteType(typeContext), value.getType(), key, args);
	}

	public static Marker typeError(ICodePosition position, IType expected, IType actual, String key, Object... args)
	{
		final Marker marker = Markers.semanticError(position, key, args);
		marker.addInfo(Markers.getSemantic("type.expected", expected));
		marker.addInfo(Markers.getSemantic("value.type", actual));
		return marker;
	}

	public static Marker typeError(ICodePosition position, IType expectedType, IType actualType, String error, String expectedError, String actualError)
	{
		final Marker marker = Markers.semanticError(position, error);
		marker.addInfo(Markers.getSemantic(expectedError, expectedType));
		marker.addInfo(Markers.getSemantic(actualError, actualType));
		return marker;
	}
}
